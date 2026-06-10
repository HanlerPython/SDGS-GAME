import javax.swing.*;
import minigame.FaucetGame;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import ui.Camera;
import ui.GameUI;
import ui.PopupManager;
import entity.Player;
import entity.Projectile;
import entity.Monster;
import entity.Trash;
import entity.Portal;
import system.WeaponSystem;
import system.CollisionSystem;
import system.Spawner;
import system.UpgradeSystem;
import system.SDGStats;
import system.ProjectileSystem;
import minigame.MinigameInterface;
import minigame.TrashSortGame;
import minigame.PowerCableGame;
import system.MapSystem;
import java.io.File;
import ui.MainMenu;
import system.GameAudio;
import system.VolumeManager;
import entity.PurificationDevice;
import ui.DirectionArrow;
public class GameMVP extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 720;
    private javax.swing.Timer timer;
    private String gameState = "playing";

    private Camera camera;
    private GameUI gameUI;
    private PopupManager popupManager;
    private MapSystem mapSystem;
    private ProjectileSystem projectileSystem;

    private Player player;
    private WeaponSystem weaponSystem;
    private CollisionSystem collisionSystem;

    private List<Monster> monsters;
    private Spawner spawner;

    private List<Trash> trashes;
    private List<Portal> portals;
    private UpgradeSystem upgradeSystem;
    private SDGStats sdgStats;

    private Random random = new Random();

    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    private float mouseWorldX = 0, mouseWorldY = 0;
    
    private Map<String, MinigameInterface> minigameRegistry = new HashMap<>();
    private MinigameInterface currentMinigame = null;
    
    private int mouseScreenX = 0, mouseScreenY = 0;

    private ui.WaveAnnouncement waveAnnouncement;
    
    private MainMenu mainMenu;
    private boolean inMainMenu = true;
    private long lastFrameTime = 0;
    
 // 最終淨化任務
    private PurificationDevice purificationDevice;
    private DirectionArrow directionArrow;
    private long purificationTimeAccumulated = 0;  // 已累積的淨化時間（毫秒）
    private long lastPurificationTime = 0;         // 上次在範圍內的時間點
    private boolean isPurifying = false;           // 是否正在淨化中
    private boolean isGameCleared = false;         // 遊戲是否已通關
    private static final long REQUIRED_PURIFICATION_TIME = 60000;  // 需要 60 秒
    private boolean finalMissionActive = false;
    
    
    public GameMVP() {
        
        System.out.println("\n=== 測試水母圖片 ===");
        File jelly1 = new File("res/jellyfish_1.png");
        System.out.println("res/jellyfish_1.png 存在: " + jelly1.exists());
        if (jelly1.exists()) {
            System.out.println("  路徑: " + jelly1.getAbsolutePath());
        }

        File jelly2 = new File("res/jellyfish_2.png");
        System.out.println("res/jellyfish_2.png 存在: " + jelly2.exists());
        if (jelly2.exists()) {
            System.out.println("  路徑: " + jelly2.getAbsolutePath());
        }

        File resDir = new File("res");
        if (resDir.exists() && resDir.isDirectory()) {
            System.out.println("\n=== res 資料夾完整內容 ===");
            for (String filename : resDir.list()) {
                System.out.println("  - " + filename);
            }
        }
        
        System.out.println("=== 圖片路徑測試 ===");
        System.out.println("user.dir: " + System.getProperty("user.dir"));

        File testFile = new File("res/player.png");
        System.out.println("new File(\"res/player.png\") exists: " + testFile.exists());
        if (testFile.exists()) {
            System.out.println("Absolute path: " + testFile.getAbsolutePath());
        }

        System.out.println("getResource(\"player.png\"): " + getClass().getClassLoader().getResource("player.png"));
        System.out.println("getResource(\"/player.png\"): " + getClass().getClassLoader().getResource("/player.png"));
        System.out.println("getResource(\"res/player.png\"): " + getClass().getClassLoader().getResource("res/player.png"));

        java.net.URL testUrl = getClass().getClassLoader().getResource("res/player.png");
        System.out.println("player.png URL: " + testUrl);
        
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
        gameUI = new GameUI();
        popupManager = new PopupManager();
        popupManager.setScreenSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        mapSystem = new MapSystem();
        mapSystem.generateRandomMap();

        upgradeSystem = new UpgradeSystem();

        player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(), () -> {
            upgradeSystem.triggerLevelUp();
        });
        
        projectileSystem = new ProjectileSystem();
        weaponSystem = new WeaponSystem();
        collisionSystem = new CollisionSystem();

        monsters = new ArrayList<>();
        portals = new ArrayList<>();
        trashes = new ArrayList<>();
        sdgStats = new SDGStats();
        
        minigameRegistry.put("trashsortgame", new TrashSortGame());
        minigameRegistry.put("powercablegame", new PowerCableGame());
        minigameRegistry.put("faucetgame", new FaucetGame());
        
        spawner = new Spawner(monsters, portals, trashes, player);

        // 🔥 設定波次監聽器
        waveAnnouncement = new ui.WaveAnnouncement();
        
        spawner.setWaveListener(waveNumber -> {
            if (waveAnnouncement != null) {
                // 🔥 檢查是否為最終波次（Wave 5）
                if (waveNumber == 5) {
                    // 不顯示 Wave 5，改由 finalWaveListener 觸發任務廣播
                    System.out.println("【WaveAnnouncement】進入最終波次，等待淨化任務");
                } else {
                    waveAnnouncement.showWave(waveNumber);
                }
            }
        });

        // 🔥 設定最終波次監聽器
        spawner.setFinalWaveListener(() -> {
            if (waveAnnouncement != null) {
                waveAnnouncement.showCustomMessage("請前往標記地點，啟動終極淨化裝置！");
            }
            startFinalMission();
        });

        for (int i = 0; i < 3; i++) {
            spawner.spawnMonster();
        }
        
        timer = new javax.swing.Timer(11, this);
        timer.start();
        
        VolumeManager.setListener(volume -> {
            System.out.println("音量變更為: " + volume + "%");
        });

        GameAudio.playBackgroundMusic("bgm.wav", VolumeManager.getVolume());
        
        mainMenu = new MainMenu(SCREEN_WIDTH, SCREEN_HEIGHT, new MainMenu.MenuCallback() {
            @Override
            public void onStartGame() {
                System.out.println("【主畫面】開始遊戲");
                inMainMenu = false;
                
                if (player == null) {
                    player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(), () -> {
                        upgradeSystem.triggerLevelUp();
                    });
                }
                
                resetGame();
                repaint();
            }
            
            @Override
            public void onShowMonsterList() {
                System.out.println("【主畫面】顯示怪物清單");
            }
            
            @Override
            public void onExitGame() {
                System.out.println("【主畫面】離開遊戲");
                System.exit(0);
            }
        });
        
        Monster.initMonsterProjectileSystem(projectileSystem);
    }
    
    private void update() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;
        
        // 🔥 統一處理 deltaTime 限制
        if (deltaTime > 50) {
            deltaTime = 16;  // 超過 50ms 當作正常幀處理
        }
        if (deltaTime < 5) {
            return;  // 太小的時間差跳過，節省 CPU
        }
        
        if (inMainMenu) {
            if (mainMenu != null) {
                mainMenu.updateBackground(SCREEN_WIDTH, SCREEN_HEIGHT, deltaTime);
            }
            return;
        }
        
        if (player == null) {
            System.err.println("【錯誤】player 為 null，無法更新遊戲");
            return;
        }
        
        if (popupManager.isShowing()) {
            return;
        }

        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.update();
            return;
        }

        if (gameState.equals("gameover")) {
            return;
        }

        player.updateMovement(upPressed, downPressed, leftPressed, rightPressed);

        mouseWorldX = camera.screenToWorldX(mouseScreenX);
        mouseWorldY = camera.screenToWorldY(mouseScreenY);

        if (mouseWorldX != 0 || mouseWorldY != 0) {
            weaponSystem.update(player, mouseWorldX, mouseWorldY, projectileSystem, monsters, trashes);
        }

        projectileSystem.update(player);
        spawner.update();
        
        // 最終淨化任務更新
        if (finalMissionActive && !isGameCleared && purificationDevice != null && purificationDevice.isActive()) {
            updatePurification();
        }
        
        // 更新波次廣播
        if (waveAnnouncement != null) {
            waveAnnouncement.update();
        }
        
        for (Monster m : monsters) {
            m.update(player.getX(), player.getY());
        }

        collisionSystem.checkBulletMonsterCollision(projectileSystem, monsters, trashes);
        collisionSystem.checkBulletPlayerCollision(projectileSystem, player);
        collisionSystem.checkMonsterPlayerCollision(monsters, player);
        collisionSystem.checkPlayerTrashCollision(player, trashes, sdgStats);
        
        Portal triggeredPortal = collisionSystem.checkPlayerPortalCollision(player, portals);
        if (triggeredPortal != null) {
            triggeredPortal.close();
            portals.remove(triggeredPortal);
            
            String type = triggeredPortal.getGameType();
            MinigameInterface targetGame = minigameRegistry.get(type);
            
            if (targetGame != null) {
                gameState = "in_minigame";
                currentMinigame = targetGame;
                currentMinigame.start(SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        }

        if (player.getHealth() <= 0) {
            gameState = "gameover";
        }
        
        camera.follow(player);

        gameUI.updateStats(
            player.getHealth(), player.getMaxHealth(),
            player.getExperience(), player.getExpToNext(),
            player.getLevel(), player.getAttackDamage(),
            sdgStats.getTotalRecycled()
        );

        upgradeSystem.update(popupManager, weaponSystem, player);
        spawner.cleanupDeadMonsters();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (inMainMenu) {
            mainMenu.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
            return;
        }
        
        drawWorldBackground(g2);
        drawWorldObjects(g2);
        gameUI.draw(g2);

        // 🔥 繪製淨化倒數（右上角）
        if (finalMissionActive && !isGameCleared && purificationDevice != null) {
            drawPurificationTimer(g2);
        }
        
        // 🔥 繪製方向箭頭（在遊戲物件之上，UI 之下）
        if (finalMissionActive && !isGameCleared && purificationDevice != null && directionArrow != null) {
            directionArrow.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT, player, purificationDevice);
        }
        
        // 🔥 波次廣播（最上層）
        if (waveAnnouncement != null) {
            waveAnnouncement.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
        
        if (popupManager.isShowing()) {
            popupManager.draw(g2);
        }

        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        if (gameState.equals("gameover")) {
            gameUI.drawGameOver(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    /**
     * 繪製淨化倒數計時器（右上角）
     */
    private void drawPurificationTimer(Graphics2D g) {
        long remaining = REQUIRED_PURIFICATION_TIME - purificationTimeAccumulated;
        if (remaining < 0) remaining = 0;
        long seconds = remaining / 1000;
        long tenths = (remaining % 1000) / 100;
        
        String timeText = String.format("淨化剩餘: %02d.%01d 秒", seconds, tenths);
        
        // 黑框白字
        g.setFont(new Font("微軟正黑體", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(timeText);
        int textX = SCREEN_WIDTH - textWidth - 20;
        int textY = 50;
        
        // 黑色外框
        g.setColor(Color.BLACK);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(timeText, textX + dx, textY + dy);
            }
        }
        
        // 白色文字
        g.setColor(Color.WHITE);
        g.drawString(timeText, textX, textY);
    }
    
    private void drawWorldBackground(Graphics2D g2) {
        mapSystem.drawMap(g2, camera, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void drawWorldObjects(Graphics2D g2) {
        for (Portal p : portals) {
            float screenX = camera.worldToScreenX(p.getX());
            if (screenX > -50 && screenX < SCREEN_WIDTH + 50) {
                p.draw(g2, camera);
            }
        }
        
        if (finalMissionActive && purificationDevice != null && purificationDevice.isActive()) {
            purificationDevice.draw(g2, camera);
        }
        
        for (Trash t : trashes) {
            float screenX = camera.worldToScreenX(t.getX());
            float screenY = camera.worldToScreenY(t.getY());
            if (screenX > -50 && screenX < SCREEN_WIDTH + 50) {
                t.draw(g2, camera);
            }
        }

        for (Monster m : monsters) {
            float screenX = camera.worldToScreenX(m.getX());
            float screenY = camera.worldToScreenY(m.getY());
            if (screenX > -100 && screenX < SCREEN_WIDTH + 100) {
                m.draw(g2, camera);
            }
        }

        projectileSystem.draw(g2, camera);

        for (entity.Weapon.Weapon weapon : weaponSystem.getWeapons()) {
            if (weapon instanceof entity.Weapon.SolarZone) {
                ((entity.Weapon.SolarZone) weapon).draw(g2, camera, player);
            }
        }
        
        for (entity.Weapon.Weapon weapon : weaponSystem.getWeapons()) {
            if (weapon instanceof entity.Weapon.UVLaser) {
                ((entity.Weapon.UVLaser) weapon).draw(g2, camera, player);
            }
        }
        
        player.draw(g2, camera);

        float playerScreenX = camera.worldToScreenX(player.getX());
        float playerScreenY = camera.worldToScreenY(player.getY());
        float mouseScreenX = camera.worldToScreenX(mouseWorldX);
        float mouseScreenY = camera.worldToScreenY(mouseWorldY);
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) playerScreenX, (int) playerScreenY, (int) mouseScreenX, (int) mouseScreenY);
    }

    private void returnToMainMenu() {
        inMainMenu = true;
        gameState = "playing";
        
        if (monsters != null) monsters.clear();
        if (trashes != null) trashes.clear();
        if (portals != null) portals.clear();
        if (projectileSystem != null) projectileSystem.reset();
        if (spawner != null) spawner.reset();
        if (upgradeSystem != null) upgradeSystem.reset();
        if (sdgStats != null) sdgStats.reset();
        if (gameUI != null) gameUI.reset();
        
        if (popupManager != null && popupManager.isShowing()) {
            popupManager.closePopup();
        }
        
        if (currentMinigame != null) {
            currentMinigame.stop();
            currentMinigame = null;
        }
        
        player = null;
        
        if (mainMenu != null) {
            mainMenu = new MainMenu(SCREEN_WIDTH, SCREEN_HEIGHT, new MainMenu.MenuCallback() {
                @Override
                public void onStartGame() {
                    System.out.println("【主畫面】開始遊戲");
                    inMainMenu = false;
                    if (player == null) {
                        player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(), () -> {
                            upgradeSystem.triggerLevelUp();
                        });
                    }
                    resetGame();
                    repaint();
                }
                
                @Override
                public void onShowMonsterList() {
                    System.out.println("【主畫面】顯示怪物清單");
                }
                
                @Override
                public void onExitGame() {
                    System.out.println("【主畫面】離開遊戲");
                    System.exit(0);
                }
            });
        }
        
        repaint();
        System.out.println("已返回主選單");
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (popupManager.isShowing()) {
            if (popupManager.getCurrentType() == PopupManager.PopupType.MONSTER) {
                if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
                    popupManager.closePopup();
                    spawner.resumeSpawning();
                }
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.UPGRADE) {
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_3) {
                    int choice = key - KeyEvent.VK_1;
                    upgradeSystem.applyUpgrade(choice, player, weaponSystem);
                    popupManager.closePopup();
                    player.triggerInvincibility(500);
                }
            }
            return;
        }

        if (key == KeyEvent.VK_ESCAPE && !popupManager.isShowing() && gameState.equals("playing")) {
            popupManager.showPauseMenu(
                () -> {
                    System.out.println("繼續遊戲");
                },
                () -> { 
                    System.out.println("重新開始遊戲");
                    resetGame();
                },
                () -> { 
                    System.out.println("回到主選單");
                    returnToMainMenu();
                }
            );
            return;
        }
        
        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.onKeyPress(key);
            if (key == KeyEvent.VK_ESCAPE) {
                // 給予獎勵
                if (currentMinigame instanceof TrashSortGame) {
                    TrashSortGame trashGame = (TrashSortGame) currentMinigame;
                    if (trashGame.isWin()) {
                        int bonus = trashGame.getRewardAttackBonus();
                        if (bonus > 0) {
                            player.setAttackDamage(player.getAttackDamage() + bonus);
                            trashGame.resetReward();
                            System.out.println("【垃圾分類】獲得獎勵 +" + bonus + " 攻擊力");
                        }
                    }
                } else if (currentMinigame instanceof PowerCableGame) {
                    PowerCableGame cableGame = (PowerCableGame) currentMinigame;
                    if (cableGame.isWin()) {
                        // 直接升一級，保留當前經驗值
                        int remainingExp = player.getExperience();
                        player.addExperience(player.getExpToNext() - remainingExp);
                        System.out.println("【電纜連接】獲得獎勵：等級提升");
                    }
                }else if (currentMinigame instanceof FaucetGame) {
                    FaucetGame faucetGame = (FaucetGame) currentMinigame;
                    if (faucetGame.isWin()) {
                        int healAmount = faucetGame.getRewardHealAmount();
                        if (healAmount > 0) {
                        	player.setMaxHealth(player.getMaxHealth() + 10, false);
                            player.heal(healAmount);
                            System.out.println("【水龍頭維修】獲得獎勵 +" + healAmount + " HP");
                        }
                    }
                }
                currentMinigame.stop();
                player.triggerInvincibility(500);
                currentMinigame = null;
                gameState = "playing";
            }
            return;
        }

        if (key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_D) rightPressed = true;

        if (key == KeyEvent.VK_R && gameState.equals("gameover")) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_S) downPressed = false;
        if (key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_D) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
        
        if (inMainMenu) {
            mainMenu.handleMouseMove(e.getX(), e.getY());
            repaint();
            return;
        }
        
        popupManager.updateHover(e.getX(), e.getY());
        
        if (gameState.equals("in_minigame") && currentMinigame instanceof PowerCableGame) {
            ((PowerCableGame) currentMinigame).handleMouseMove(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
        
        popupManager.updateHover(e.getX(), e.getY());
        
        if (gameState.equals("in_minigame") && currentMinigame instanceof PowerCableGame) {
            ((PowerCableGame) currentMinigame).handleMouseMove(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    	// 死亡畫面的按鈕處理（優先處理）
        if (gameState.equals("gameover")) {
            Rectangle restartRect = gameUI.getGameOverRestartRect();
            Rectangle menuRect = gameUI.getGameOverMenuRect();
            
            if (restartRect != null && restartRect.contains(e.getX(), e.getY())) {
                resetGame();
                return;
            } else if (menuRect != null && menuRect.contains(e.getX(), e.getY())) {
                returnToMainMenu();
                return;
            }
        }
        
        if (inMainMenu) {
            mainMenu.handleMousePress(e.getX(), e.getY());
            repaint();
            return;
        }
        
        if (popupManager.isShowing()) {
            if (popupManager.getCurrentType() == PopupManager.PopupType.MONSTER) {
                if (popupManager.isCloseButtonClicked(e.getX(), e.getY())) {
                    popupManager.closePopup();
                    spawner.resumeSpawning();
                }
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.PAUSE) {
                popupManager.handlePauseMenuClick(e.getX(), e.getY());
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.UPGRADE) {
                popupManager.handleUpgradeClick(e.getX(), e.getY());
                player.triggerInvincibility(500);
            }
            return;
        }
        
        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.handleMouseClick(e.getX(), e.getY());
            repaint();
            return;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (inMainMenu && mainMenu != null) {
            mainMenu.handleMouseRelease(e.getX(), e.getY());
            return;
        }
        
        if (popupManager.isShowing() && popupManager.getCurrentType() == PopupManager.PopupType.PAUSE) {
            popupManager.handlePauseMenuRelease();
        }
        
        if (popupManager.isShowing()) {
            return;
        }
        
        if (gameState.equals("in_minigame") && currentMinigame != null) {
            return;
        }
    }
    
    /**
     * 啟動最終淨化任務
     */
    private void startFinalMission() {
        finalMissionActive = true;
        
        /* 停止生成怪物
        if (spawner != null) {
            spawner.stopSpawningForFinalWave();
        }*/
        
        // 清除現有傳送門
        if (portals != null) {
            portals.clear();
        }
        
        // 在地圖上隨機生成淨化裝置（距離玩家 400-800 像素）
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 400 + random.nextDouble() * 400;
        float deviceX = player.getX() + (float)(Math.cos(angle) * distance);
        float deviceY = player.getY() + (float)(Math.sin(angle) * distance);
        
        // 確保裝置在地圖範圍內
        int worldWidth = mapSystem.WORLD_TILES_X * mapSystem.TILE_SIZE;
        int worldHeight = mapSystem.WORLD_TILES_Y * mapSystem.TILE_SIZE;
        deviceX = Math.max(100, Math.min(worldWidth - 100, deviceX));
        deviceY = Math.max(100, Math.min(worldHeight - 100, deviceY));
        
        purificationDevice = new PurificationDevice(deviceX, deviceY);
        directionArrow = new DirectionArrow();
        
        purificationTimeAccumulated = 0;
        isPurifying = false;
        
        // 顯示任務廣播
        showMissionAnnouncement();
    }

    /**
     * 顯示任務廣播
     */
    private void showMissionAnnouncement() {
        // 可以使用 WaveAnnouncement 或另外製作
        if (waveAnnouncement != null) {
            waveAnnouncement.showCustomMessage("請前往標記地點，啟動終極淨化裝置！");
        }
    }

    /**
     * 更新淨化進度
     */
    private void updatePurification() {
        boolean inRange = purificationDevice.isPlayerInRange(player.getX(), player.getY());
        long currentTime = System.currentTimeMillis();
        
        if (inRange) {
            if (!isPurifying) {
                // 剛進入範圍
                isPurifying = true;
                lastPurificationTime = currentTime;
                System.out.println("【淨化任務】進入淨化範圍，開始累積時間");
            } else {
                // 持續在範圍內，累積時間
                long delta = currentTime - lastPurificationTime;
                purificationTimeAccumulated += delta;
                lastPurificationTime = currentTime;
                
                // 更新 UI 顯示
                updatePurificationUI();
                
                System.out.println("【淨化任務】累積時間: " + (purificationTimeAccumulated / 1000) + "/" + (REQUIRED_PURIFICATION_TIME / 1000) + " 秒");
                
                if (purificationTimeAccumulated >= REQUIRED_PURIFICATION_TIME) {
                    completeGame();
                }
            }
        } else {
            if (isPurifying) {
                // 離開範圍
                isPurifying = false;
                System.out.println("【淨化任務】離開淨化範圍，時間暫停 (已累積: " + (purificationTimeAccumulated / 1000) + " 秒)");
            }
        }
    }

    /**
     * 更新淨化進度 UI
     */
    private void updatePurificationUI() {
        // 可在畫面上方顯示進度條
    }

    /**
     * 通關遊戲
     */
    private void completeGame() {
        isGameCleared = true;
        finalMissionActive = false;
        
        // 停止所有遊戲動作
        if (spawner != null) {
            spawner.setGameCleared(true);
        }
        
        // 播放所有怪物爆炸動畫
        startExplosionAnimation();
    }

    /**
     * 開始爆炸動畫
     */
    private void startExplosionAnimation() {
        // TODO: 播放爆炸動畫，然後顯示勝利畫面
        showVictoryScreen();
    }

    /**
     * 顯示勝利畫面
     */
    private void showVictoryScreen() {
        // TODO: 顯示勝利畫面與統計資訊
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    private void resetGame() {
        // 確保退出主畫面模式
        inMainMenu = false;
        
        // 確保 player 存在
        if (player == null) {
            player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(), () -> {
                upgradeSystem.triggerLevelUp();
            });
        } else {
            player.reset(mapSystem.getMapCenterX(), mapSystem.getMapCenterY());
        }
        
        // 重置其他系統
        if (monsters != null) {
            monsters.clear();
        }
        if (trashes != null) {
            trashes.clear();
        }
        if (portals != null) {
            portals.clear();
        }
        if (spawner != null) {
            spawner.reset();
        }
        if (projectileSystem != null) {
            projectileSystem.reset();
        }
        if (upgradeSystem != null) {
            upgradeSystem.reset();
        }
        if (weaponSystem != null) {
            weaponSystem.reset();
        }
        if (sdgStats != null) {
            sdgStats.reset();
        }
        if (gameUI != null) {
            gameUI.reset();
        }
        
        gameState = "playing";
        
        if (currentMinigame != null) {
            currentMinigame.stop();
            currentMinigame = null;
        }
        
        // 🔥 重新設定波次監聽器（確保監聽器還在）
        if (spawner != null) {
            spawner.setWaveListener(waveNumber -> {
                if (waveAnnouncement != null) {
                    waveAnnouncement.showWave(waveNumber);
                }
            });
        }
        
        // 初始生成 3 隻怪物
        for (int i = 0; i < 3; i++) {
            if (spawner != null) {
                spawner.spawnMonster();
            }
        }
        
        // 🔥 重置並顯示第 1 波廣播
        if (waveAnnouncement != null) {
            waveAnnouncement.showWave(1);
        }
        
     // 重置最終淨化任務狀態
        finalMissionActive = false;
        isGameCleared = false;
        purificationDevice = null;
        directionArrow = null;
        purificationTimeAccumulated = 0;
        isPurifying = false;
        
        System.out.println("遊戲已重置");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Eco Survivor - SDGs");
            GameMVP game = new GameMVP();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}