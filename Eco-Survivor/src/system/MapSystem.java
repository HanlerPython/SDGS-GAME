package system;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import ui.Camera;

public class MapSystem {
    // 地圖大小與網格尺寸設定
    public final int WORLD_TILES_X = 100;
    public final int WORLD_TILES_Y = 100;
    public final int TILE_SIZE = 64;

    // 地形主編號
    public final int TILE_WATER = 0;
    public final int TILE_SAND = 1;

    // 上層獨立裝飾物件編號
    public final int OBJ_NONE = 0;
    public final int OBJ_GRASS = 1;
    public final int OBJ_PALM = 2;
    public final int OBJ_ROCK = 3;
    public final int OBJ_BUSH = 4;

    // 地圖資料矩陣
    private int[][] mapGrid = new int[WORLD_TILES_X][WORLD_TILES_Y];
    private int[][] mapVariantGrid = new int[WORLD_TILES_X][WORLD_TILES_Y];
    private int[][] mapVariantRotationGrid = new int[WORLD_TILES_X][WORLD_TILES_Y];
    private int[][] mapObjectGrid = new int[WORLD_TILES_X][WORLD_TILES_Y];
    private int[][] mapRotationGrid = new int[WORLD_TILES_X][WORLD_TILES_Y];

    private int[][] cachedTileIndex = new int[WORLD_TILES_X][WORLD_TILES_Y];
    private boolean tileIndexCacheValid = false;

    // 圖片資源陣列
    private BufferedImage[] autotileImages = new BufferedImage[16]; // 0 ~ 15 號自動拼接沙灘圖
    private BufferedImage[] bgTiles = new BufferedImage[1];          // bg.png 海洋背景
    private BufferedImage[] centerSandTiles = new BufferedImage[3];  // 15號中心塊延續的三種差分變體圖
    private BufferedImage imgGrass, imgPalm, imgRock, imgBush;       // 四大獨立裝飾物

    private Random random = new Random();

    public MapSystem() {
        loadResources();
    }

    private void loadResources() {
        try {
            for (int i = 0; i < 16; i++) {
                String fileName = "sand_" + i + ".png";
                java.net.URL urlTile = getClass().getClassLoader().getResource(fileName);
                if (urlTile != null) autotileImages[i] = ImageIO.read(urlTile);
                else {
                    File backupFile = new File("src/" + fileName);
                    if (backupFile.exists()) autotileImages[i] = ImageIO.read(backupFile);
                }
            }

            centerSandTiles[0] = tryLoadImage("sand_grass.png", "src/res/sand_grass.png");
            centerSandTiles[1] = tryLoadImage("sand_stone.png", "src/res/sand_stone.png");
            centerSandTiles[2] = tryLoadImage("sand.png", "src/res/sand.png"); // 微調變體

            java.net.URL urlBg = getClass().getClassLoader().getResource("res/bg.png");
            bgTiles[0] = (urlBg != null) ? ImageIO.read(urlBg) : ImageIO.read(new File("src/res/bg.png"));

            imgGrass = tryLoadImage("grass.png", "src/res/grass.png");
            imgPalm  = tryLoadImage("palm.png", "src/res/palm.png");
            imgRock  = tryLoadImage("rock.png", "src/res/rock.png");
            imgBush  = tryLoadImage("bush.png", "src/res/bush.png");

            System.out.println("【MapSystem】所有地形、差分與裝飾圖片巡檢完成！");
        } catch (IOException e) {
            System.err.println("【MapSystem 嚴重錯誤】基礎沙灘(0~15)或海洋圖片載入失敗，請檢查 res 內容！");
            e.printStackTrace();
        }
    }
    
    private BufferedImage tryLoadImage(String resPath, String filePath) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(resPath);
            if (url != null) return ImageIO.read(url);
            
            File file = new File(filePath);
            if (file.exists()) return ImageIO.read(file);
            
            System.err.println("【MapSystem 提示】未找到選配圖片: " + filePath + " (此項目暫不顯示，不影響系統運行)");
        } catch (IOException e) {
            System.err.println("【MapSystem 警告】讀取失敗: " + filePath);
        }
        return null;
    }
    
    private void updateTileIndexCache() {
        for (int x = 0; x < WORLD_TILES_X; x++) {
            for (int y = 0; y < WORLD_TILES_Y; y++) {
                if (mapGrid[x][y] == TILE_SAND) {
                    int up    = (mapGrid[x][Math.floorMod(y - 1, WORLD_TILES_Y)] == TILE_SAND) ? 1 : 0;
                    int left  = (mapGrid[Math.floorMod(x - 1, WORLD_TILES_X)][y] == TILE_SAND) ? 1 : 0;
                    int right = (mapGrid[Math.floorMod(x + 1, WORLD_TILES_X)][y] == TILE_SAND) ? 1 : 0;
                    int down  = (mapGrid[x][Math.floorMod(y + 1, WORLD_TILES_Y)] == TILE_SAND) ? 1 : 0;
                    cachedTileIndex[x][y] = up + left*2 + right*4 + down*8;
                } else {
                    cachedTileIndex[x][y] = -1;
                }
            }
        }
        tileIndexCacheValid = true;
    }

    /**
     * 群島生成演算法：中心塊判定、主圖/差分比例分配與各自的隨機旋轉生成
     */
    public void generateRandomMap() {
        // 初始化清空
        for (int x = 0; x < WORLD_TILES_X; x++) {
            for (int y = 0; y < WORLD_TILES_Y; y++) {
                mapGrid[x][y] = TILE_WATER;
                mapVariantGrid[x][y] = -1; 
                mapVariantRotationGrid[x][y] = 0; // 重置
                mapObjectGrid[x][y] = OBJ_NONE;
                mapRotationGrid[x][y] = 0;
            }
        }

        // 1. 隨機散落 7~10 個精緻小島種子（增加數量，縮小半徑，防止黏成大島）
        int islandCount = 7 + random.nextInt(4); // 7 到 10 座島
        int[] islandX = new int[islandCount];
        int[] islandY = new int[islandCount];
        int[] islandRadius = new int[islandCount];

        for (int i = 0; i < islandCount; i++) {
            // 讓島嶼隨機散佈在 100x100 的地圖各處
            islandX[i] = 15 + random.nextInt(WORLD_TILES_X - 30);
            islandY[i] = 15 + random.nextInt(WORLD_TILES_Y - 30);
            islandRadius[i] = 5 + random.nextInt(4); 
        }

        // 2. 第一遍巡檢：以距離場鋪設陸地
        for (int x = 0; x < WORLD_TILES_X; x++) {
            for (int y = 0; y < WORLD_TILES_Y; y++) {
                boolean isLand = false;
                for (int i = 0; i < islandCount; i++) {
                    double dist = Math.hypot(x - islandX[i], y - islandY[i]);
                    dist += random.nextDouble() * 3.5 - 1.75; 
                    if (dist < islandRadius[i]) {
                        isLand = true;
                        break;
                    }
                }
                if (isLand) mapGrid[x][y] = TILE_SAND;
            }
        }

        // 3. 第二遍巡檢：找出 15 號中心塊，按比例分發【預設主圖】與【外觀差分（配發旋轉）】
        for (int x = 0; x < WORLD_TILES_X; x++) {
            for (int y = 0; y < WORLD_TILES_Y; y++) {
                if (mapGrid[x][y] == TILE_SAND) {
                    
                    // 利用安全取模探查無縫上下左右鄰居
                    boolean up    = (mapGrid[x][Math.floorMod(y - 1, WORLD_TILES_Y)] == TILE_SAND);
                    boolean left  = (mapGrid[Math.floorMod(x - 1, WORLD_TILES_X)][y] == TILE_SAND);
                    boolean right = (mapGrid[Math.floorMod(x + 1, WORLD_TILES_X)][y] == TILE_SAND);
                    boolean down  = (mapGrid[x][Math.floorMod(y + 1, WORLD_TILES_Y)] == TILE_SAND);

                    if (up && left && right && down) {
                        // 100% 確定是 15 號中心塊
                        int randVal = random.nextInt(100);
                        
                        if (randVal < 75) {
                            mapVariantGrid[x][y] = -1; // 75% 面積保持原樣，繪製經典主要 sand_15.png
                        } else {
                            if (randVal < 85)       mapVariantGrid[x][y] = 0; // 10% 帶海草 (sand_grass.png)
                            else if (randVal < 95)  mapVariantGrid[x][y] = 1; // 10% 帶小碎石 (sand_stone.png)
                            else                    mapVariantGrid[x][y] = 2; // 5% 微調變體 (sand.png)
                            
                            mapVariantRotationGrid[x][y] = random.nextInt(4);
                        }

                        // 4. 在中心塊沙地上，有機率額外疊加獨立的高聳大裝飾物件（樹、大岩石）
                        int spawnRand = random.nextInt(100);
                        if (spawnRand < 5)       mapObjectGrid[x][y] = OBJ_PALM;  
                        else if (spawnRand < 10) mapObjectGrid[x][y] = OBJ_ROCK;  
                        else if (spawnRand < 15) mapObjectGrid[x][y] = OBJ_GRASS; 
                        else if (spawnRand < 19) mapObjectGrid[x][y] = OBJ_BUSH;  

                        // 物件配發獨立的隨機旋轉狀態 (與底圖差分分開，避免方向完全同步)
                        if (mapObjectGrid[x][y] != OBJ_NONE) {
                            mapRotationGrid[x][y] = random.nextInt(4);
                        }
                    } else {
                        // 屬於 0 ~ 14 號的過渡海岸線格子
                        mapVariantGrid[x][y] = -1;
                        mapVariantRotationGrid[x][y] = 0;
                        mapObjectGrid[x][y] = OBJ_NONE;
                    }
                }
            }
        }
        
        updateTileIndexCache();
    }

    public float getMapCenterX() { return (WORLD_TILES_X * TILE_SIZE) / 2.0f; }
    public float getMapCenterY() { return (WORLD_TILES_Y * TILE_SIZE) / 2.0f; }

    public void drawMap(Graphics2D g, Camera camera, int screenWidth, int screenHeight) {
        int startX = (int) Math.floor(camera.screenToWorldX(0) / TILE_SIZE);
        int endX = (int) Math.floor(camera.screenToWorldX(screenWidth) / TILE_SIZE) + 1;
        int startY = (int) Math.floor(camera.screenToWorldY(0) / TILE_SIZE);
        int endY = (int) Math.floor(camera.screenToWorldY(screenHeight) / TILE_SIZE) + 1;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                int gridX = Math.floorMod(x, WORLD_TILES_X);
                int gridY = Math.floorMod(y, WORLD_TILES_Y);

                int terrainType = mapGrid[gridX][gridY];
                int variant = mapVariantGrid[gridX][gridY];
                int varRotCode = mapVariantRotationGrid[gridX][gridY];
                int objectType = mapObjectGrid[gridX][gridY];
                int rotCode = mapRotationGrid[gridX][gridY];

                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                int screenX = (int) camera.worldToScreenX(worldX);
                int screenY = (int) camera.worldToScreenY(worldY);

                // 第一層：渲染背景海洋
                if (bgTiles[0] != null) {
                    g.drawImage(bgTiles[0], screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(20, 40, 80));
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                }

                // 第二層：渲染沙灘底圖
                if (terrainType == TILE_SAND) {
                    int tileIndex;
                    if (tileIndexCacheValid) {
                        tileIndex = cachedTileIndex[gridX][gridY];
                    } else {
                        // 備用：即時計算（保留原有邏輯）
                        tileIndex = 0;
                        if (mapGrid[gridX][Math.floorMod(y - 1, WORLD_TILES_Y)] == TILE_SAND) tileIndex += 1;
                        if (mapGrid[Math.floorMod(x - 1, WORLD_TILES_X)][gridY] == TILE_SAND) tileIndex += 2;
                        if (mapGrid[Math.floorMod(x + 1, WORLD_TILES_X)][gridY] == TILE_SAND) tileIndex += 4;
                        if (mapGrid[gridX][Math.floorMod(y + 1, WORLD_TILES_Y)] == TILE_SAND) tileIndex += 8;
                    }

                    if (tileIndex == 15) {
                        if (variant == -1) {
                            // 75% 機率：渲染最原汁原味的主要預設純沙地 sand_15.png (不需要旋轉)
                            if (autotileImages[15] != null) {
                                g.drawImage(autotileImages[15], screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                            }
                        } else {
                            // 25% 機率：渲染差分小細圖
                            if (centerSandTiles[variant] != null) {
                                if (varRotCode > 0) {
                                    double cX = screenX + TILE_SIZE / 2.0;
                                    double cY = screenY + TILE_SIZE / 2.0;
                                    double radians = varRotCode * (Math.PI / 2.0);

                                    java.awt.geom.AffineTransform oldTransform = g.getTransform();
                                    g.rotate(radians, cX, cY);
                                    g.drawImage(centerSandTiles[variant], screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                                    g.setTransform(oldTransform); // 立即還原
                                } else {
                                    g.drawImage(centerSandTiles[variant], screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                                }
                            }
                        }
                    } else {
                        // 0 ~ 14 號與海洋銜接的過渡邊緣圖（邊緣圖不可旋轉，否則會跟海洋錯位）
                        if (autotileImages[tileIndex] != null) {
                            g.drawImage(autotileImages[tileIndex], screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                        }
                    }

                    // 第三層：渲染獨立的隨機旋轉物件（椰子樹、大石頭、小灌木等）
                    if (objectType != OBJ_NONE) {
                        BufferedImage imgToDraw = null;
                        switch (objectType) {
                            case OBJ_GRASS: imgToDraw = imgGrass; break;
                            case OBJ_PALM:  imgToDraw = imgPalm;  break;
                            case OBJ_ROCK:  imgToDraw = imgRock;  break;
                            case OBJ_BUSH:  imgToDraw = imgBush;  break;
                        }

                        if (imgToDraw != null) {
                            if (rotCode > 0) {
                                // 執行上層物件正中央的畫布矩陣旋轉
                                double cX = screenX + TILE_SIZE / 2.0;
                                double cY = screenY + TILE_SIZE / 2.0;
                                double radians = rotCode * (Math.PI / 2.0);

                                java.awt.geom.AffineTransform oldTransform = g.getTransform();
                                g.rotate(radians, cX, cY);
                                g.drawImage(imgToDraw, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                                g.setTransform(oldTransform); // 畫完立刻還原畫布
                            } else {
                                g.drawImage(imgToDraw, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                            }
                        }
                    }
                }
            }
        }
    }
}