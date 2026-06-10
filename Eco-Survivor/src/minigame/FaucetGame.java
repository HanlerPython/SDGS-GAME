package minigame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FaucetGame implements MinigameInterface {

    private boolean active;
    private boolean isFinished;
    private boolean isVictory;
    private int rewardHealAmount; // 回血獎勵
    private int screenWidth;
    private int screenHeight;

    private long startTime;
    private final int GAME_DURATION_MS = 15000; // 撐過 15 秒即獲勝

    private double waterLevel; // 100.0 為滿，掉到 0 則失敗
    private long lastSpawnTime;
    private int currentSpawnInterval = 1000; // 初始漏水間隔 (ms)，會隨時間減少增加難度

    // 水龍頭/管線節點類別
    private static class Faucet {
        int x, y, radius;
        boolean isLeaking;
        long leakStartTime;
        boolean justFixed; // 用來顯示修好的綠光特效
        long fixedTime;

        Faucet(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.isLeaking = false;
        }
    }

    private List<Faucet> faucets;

    public FaucetGame() {
        this.active = false;
        this.faucets = new ArrayList<>();
    }

    @Override
    public void start(int screenWidth, int screenHeight) {
        this.active = true;
        this.isFinished = false;
        this.isVictory = false;
        this.rewardHealAmount = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.waterLevel = 100.0;
        this.startTime = System.currentTimeMillis();
        this.lastSpawnTime = startTime;
        this.currentSpawnInterval = 1100; // 初始難度 

        initGrid();
    }

    private void initGrid() {
        faucets.clear();
        int cols = 4;
        int rows = 3;
        int spacingX = screenWidth / (cols + 1);
        int spacingY = screenHeight / (rows + 1);

        //水龍頭生成
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                int fx = c * spacingX;
                int fy = r * spacingY + 30; // 稍微往下移避開上方 UI
                faucets.add(new Faucet(fx, fy, 35));
            }
        }
    }

    @Override 
    public void stop() {
        this.active = false;
    }

    @Override
    public void update() {
        if (!active || isFinished) return;

        long currentTime = System.currentTimeMillis();
        long elapsedGameTime = currentTime - startTime;

        // 1. 檢查勝利條件 (撐過 15 秒)
        if (elapsedGameTime >= GAME_DURATION_MS) {
            isFinished = true;
            isVictory = true;
            rewardHealAmount = 50; // 勝利獎勵
            return;
        }

        // 2. 隨機漏水邏輯 (隨時間加快生成速度)
        currentSpawnInterval = Math.max(450, 1100 - (int)(elapsedGameTime / 15)); // 隨時間減少間隔，增加難度，但不低於 400ms
        if (currentTime - lastSpawnTime > currentSpawnInterval) {
            spawnLeak();
            lastSpawnTime = currentTime;
        }

        // 3. 扣水邏輯與特效重置
        for (Faucet f : faucets) {
            if (f.isLeaking) {
                long leakDuration = currentTime - f.leakStartTime;
                if (leakDuration > 100) { // 漏超過 0.1 秒開始扣水
                    waterLevel -= 0.15; // 扣水速度 (可調整難度)
                }
            }
            if (f.justFixed && currentTime - f.fixedTime > 300) {
                f.justFixed = false; // 關閉修理成功的綠光特效
            }
        }

        // 4. 檢查失敗條件 (水漏光了)
        if (waterLevel <= 0) {
            waterLevel = 0;
            isFinished = true;
            isVictory = false;
        }
    }

    // 漏水機制
    private void spawnLeak() {
        // 找出沒有漏水的管線
        List<Faucet> available = new ArrayList<>();
        for (Faucet f : faucets) {
            if (!f.isLeaking) available.add(f);
        }

        if (!available.isEmpty()) {
            int idx = (int) (Math.random() * available.size());
            Faucet f = available.get(idx);
            f.isLeaking = true;
            f.leakStartTime = System.currentTimeMillis();
            f.justFixed = false;
        }
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY) {
        if (!active || isFinished) return;

        for (Faucet f : faucets) {
            if (f.isLeaking) {
                double dist = Math.hypot(mouseX - f.x, mouseY - f.y);
                // 點擊判定範圍稍微比外觀大一點點，手感較好
                if (dist <= f.radius + 15) {
                    f.isLeaking = false;
                    f.justFixed = true;
                    f.fixedTime = System.currentTimeMillis();
                    // 修好水管加一點點水回來鼓勵玩家
                    waterLevel = Math.min(100.0, waterLevel + 2.0); 
                    break; // 點到一個就跳出，避免一鍵多修
                }
            }
        }
    }

    @Override
    public void onKeyPress(int key) {
        // 關水生存戰失敗直接回戰場，不提供 R 重來
    }

    @Override
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        // 背景色
        g.setColor(new Color(15, 30, 45));
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isFinished) {
            drawEndScreen(g, screenWidth, screenHeight);
        } else {
            drawGameplay(g, screenWidth, screenHeight);
        }
    }

    private void drawGameplay(Graphics2D g, int screenWidth, int screenHeight) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        int timeLeft = Math.max(0, (GAME_DURATION_MS - (int)elapsed) / 1000);

        // 1. 背景升級：深海/科技感漸層背景 (取代原本的單色)
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(10, 20, 35), 0, screenHeight, new Color(20, 45, 65));
        g.setPaint(bgGradient);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // --- UI 繪製 ---
        // 標題與時間 (加入陰影增加立體感與可讀性)
        drawTextWithShadow(g, "SDG 6: LEAK CONTROL", 20, 40, new Font("Arial", Font.BOLD, 28), new Color(0, 200, 255));
        
        Color timeColor = timeLeft <= 3 ? new Color(255, 80, 80) : Color.WHITE;
        drawTextWithShadow(g, "Time: " + timeLeft + "s", screenWidth - 140, 40, new Font("Arial", Font.BOLD, 24), timeColor);

        // 2. 水資源條升級 (圓角與半透明科技感外框)
        int tankWidth = 300;
        int tankHeight = 24;
        int tankX = screenWidth / 2 - tankWidth / 2;
        int tankY = 20;

        // 水槽底色 (半透明黑)
        g.setColor(new Color(20, 20, 20, 180));
        g.fillRoundRect(tankX, tankY, tankWidth, tankHeight, 12, 12);
        
        // 水量顏色漸層 (健康=藍，危險=紅)
        Color waterColor = waterLevel > 40 ? new Color(0, 180, 255) : new Color(255, 60, 60);
        g.setColor(waterColor);
        int currentWaterWidth = (int)(tankWidth * (waterLevel / 100.0));
        g.fillRoundRect(tankX, tankY, currentWaterWidth, tankHeight, 12, 12);

        // 水槽高光外框
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(tankX, tankY, tankWidth, tankHeight, 12, 12);
        
        drawTextWithShadow(g, "RESERVE TANK", tankX + 90, tankY + 17, new Font("Arial", Font.BOLD, 14), Color.WHITE);

        // --- 繪製水管與水龍頭 ---
        for (Faucet f : faucets) {
            // 3. 水龍頭底座升級 (金屬漸層立體感)
            GradientPaint pipeGradient = new GradientPaint(
                f.x - f.radius, f.y - f.radius, new Color(90, 95, 100), 
                f.x + f.radius, f.y + f.radius, new Color(40, 45, 50)
            );
            g.setPaint(pipeGradient);
            g.fillOval(f.x - f.radius, f.y - f.radius, f.radius * 2, f.radius * 2);
            
            // 金屬高光邊框
            g.setColor(new Color(150, 150, 160));
            g.setStroke(new BasicStroke(2));
            g.drawOval(f.x - f.radius, f.y - f.radius, f.radius * 2, f.radius * 2);

            // 4. 狀態特效升級
            if (f.isLeaking) {
                // 噴水特效 (雙層動態擴張光環)
                int pulse = (int)(Math.sin(currentTime * 0.015) * 12); // 動態大小變化
                
                // 外層水暈
                g.setColor(new Color(0, 180, 255, 80));
                g.fillOval(f.x - f.radius - 5 - pulse/2, f.y - f.radius - 5 - pulse/2, (f.radius + 5 + pulse/2) * 2, (f.radius + 5 + pulse/2) * 2);
                
                // 內層水波紋
                g.setColor(new Color(0, 150, 255, 200));
                g.setStroke(new BasicStroke(3));
                g.drawOval(f.x - f.radius - pulse, f.y - f.radius - pulse, (f.radius + pulse) * 2, (f.radius + pulse) * 2);
                
                // 內部警示紅心 (動態閃爍透明度)
                int alpha = 100 + (int)(Math.abs(Math.sin(currentTime * 0.01)) * 155);
                g.setColor(new Color(255, 40, 40, alpha));
                g.fillOval(f.x - 12, f.y - 12, 24, 24);
                
            } else if (f.justFixed) {
                // 剛修好的綠光 (滿版亮綠覆蓋)
                g.setColor(new Color(50, 255, 80, 160));
                g.fillOval(f.x - f.radius, f.y - f.radius, f.radius * 2, f.radius * 2);
            } else {
                // 正常關閉狀態的閥門中心 (加上類似螺絲的細節)
                g.setColor(new Color(20, 25, 30));
                g.fillOval(f.x - 12, f.y - 12, 24, 24);
                
                g.setColor(new Color(120, 120, 130));
                g.setStroke(new BasicStroke(2));
                g.drawOval(f.x - 6, f.y - 6, 12, 12);
            }
        }
    }

    // --- 請把這個輔助方法加在類別的最後面 ---
    private void drawTextWithShadow(Graphics2D g, String text, int x, int y, Font font, Color textColor) {
        g.setFont(font);
        // 畫黑色半透明陰影
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(text, x + 2, y + 2);
        // 畫主文字
        g.setColor(textColor);
        g.drawString(text, x, y);
    }

    private void drawEndScreen(Graphics2D g, int screenWidth, int screenHeight) {
        if (isVictory) {
            drawCenteredString(g, "SDG 6: WATER CONSERVATION", screenWidth, 150, new Font("Arial", Font.BOLD, 36), new Color(100, 180, 255));
            drawCenteredString(g, "SUCCESS! LEAKS CONTAINED AND RESOLVED!", screenWidth, 250, new Font("Arial", Font.BOLD, 28), new Color(100, 200, 255));
            drawCenteredString(g, "Reward: Restored " + rewardHealAmount + " HP", screenWidth, 350, new Font("Arial", Font.BOLD, 24), Color.WHITE);
            drawCenteredString(g, "Press ESC to Return to Battlefield", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 18), Color.GRAY);
        } else {
            drawCenteredString(g, "SDG 6: WATER CONSERVATION", screenWidth, 150, new Font("Arial", Font.BOLD, 36), new Color(100, 180, 255));
            drawCenteredString(g, "CRITICAL FAILURE! RESERVOIR DEPLETED!", screenWidth, 250, new Font("Arial", Font.BOLD, 28), new Color(255, 100, 100));
            drawCenteredString(g, "Reward: None", screenWidth, 350, new Font("Arial", Font.BOLD, 24), Color.WHITE);
            drawCenteredString(g, "Press ESC to Return to Battlefield", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 18), Color.GRAY);
        }
    }

    private void drawCenteredString(Graphics2D g, String text, int screenWidth, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (screenWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public boolean isActive() { return active; }
    
    public int getRewardHealAmount() { return rewardHealAmount; }
    public boolean isWin() { return isVictory; }
}