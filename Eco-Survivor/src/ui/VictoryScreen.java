package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import entity.Monster;
import system.GameAudio;

public class VictoryScreen {

    private boolean active = false;
    private float alpha = 0f;
    private int stage = 0;
    private long stageStartTime = 0;
    private boolean victorySoundPlayed = false;

    private BufferedImage[] explosionFrames = new BufferedImage[3];
    private float[] explosionX;
    private float[] explosionY;
    private int[] explosionProgress;
    private int totalMonsters = 0;
    private int completedExplosions = 0;
    private long lastFrameTime = 0;
    private boolean[] explosionSoundPlayed;

    private int killCount;
    private int finalLevel;
    private int totalRecycled;

    private Runnable onRestart;
    private Runnable onMainMenu;

    private Rectangle restartRect;
    private Rectangle mainMenuRect;
    private boolean restartHover = false;
    private boolean mainMenuHover = false;

    private BufferedImage restartImage;
    private BufferedImage mainMenuImage;

    public VictoryScreen() {
        loadImages();
        loadButtonImages();
    }

    private void loadImages() {
        for (int i = 0; i < 3; i++) {
            String fileName = "explosion_" + (i + 1) + ".png";
            try {
                java.net.URL url = getClass().getClassLoader().getResource(fileName);
                if (url != null)
                    explosionFrames[i] = ImageIO.read(url);
                else {
                    File file = new File("res/" + fileName);
                    if (file.exists())
                        explosionFrames[i] = ImageIO.read(file);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void loadButtonImages() {
        try {
            java.net.URL restartUrl = getClass().getClassLoader().getResource("btn_restart.png");
            if (restartUrl != null)
                restartImage = ImageIO.read(restartUrl);
            else {
                File file = new File("res/btn_restart.png");
                if (file.exists())
                    restartImage = ImageIO.read(file);
            }

            java.net.URL menuUrl = getClass().getClassLoader().getResource("btn_mainmenu.png");
            if (menuUrl != null)
                mainMenuImage = ImageIO.read(menuUrl);
            else {
                File file = new File("res/btn_mainmenu.png");
                if (file.exists())
                    mainMenuImage = ImageIO.read(file);
            }
        } catch (IOException ignored) {
        }
    }

    public void start(int killCount, int level, int recycled, List<Monster> monsters, Runnable onRestart,
            Runnable onMainMenu) {
        this.killCount = killCount;
        this.finalLevel = level;
        this.totalRecycled = recycled;
        this.onRestart = onRestart;
        this.onMainMenu = onMainMenu;
        this.victorySoundPlayed = false;

        if (monsters != null) {
            totalMonsters = monsters.size();
            explosionX = new float[totalMonsters];
            explosionY = new float[totalMonsters];
            explosionProgress = new int[totalMonsters];
            explosionSoundPlayed = new boolean[totalMonsters];
            for (int i = 0; i < totalMonsters; i++) {
                Monster m = monsters.get(i);
                explosionX[i] = m.getX();
                explosionY[i] = m.getY();
                explosionProgress[i] = 0;
                explosionSoundPlayed[i] = false;
            }
        } else {
            totalMonsters = 0;
        }

        completedExplosions = 0;
        this.active = true;
        this.stage = 0;
        this.alpha = 0f;
        this.stageStartTime = System.currentTimeMillis();
        this.lastFrameTime = System.currentTimeMillis();
        GameAudio.stopBackgroundMusic();
    }

    public void stop() {
        this.active = false;
    }

    public void update() {
        if (!active)
            return;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - stageStartTime;

        switch (stage) {
            case 0:
                if (completedExplosions < totalMonsters) {
                    if (currentTime - lastFrameTime >= 150) {
                        lastFrameTime = currentTime;
                        for (int i = 0; i < totalMonsters; i++) {
                            if (explosionProgress[i] < 9) {
                                if (explosionProgress[i] == 0 && !explosionSoundPlayed[i]) {
                                    explosionSoundPlayed[i] = true;
                                    GameAudio.playSound("explosion.wav", 70);
                                }
                                explosionProgress[i]++;
                                if (explosionProgress[i] >= 9)
                                    completedExplosions++;
                            }
                        }
                    }
                } else {
                    stage = 1;
                    stageStartTime = currentTime;
                }
                break;
            case 1:
                alpha = Math.min(1f, (float) elapsed / 1000f);
                if (elapsed >= 1000) {
                    stage = 2;
                    stageStartTime = currentTime;
                }
                break;
            case 2:
                if (elapsed >= 1000) {
                    stage = 3;
                    stageStartTime = currentTime;
                }
                break;
            case 3:
                if (elapsed >= 5000) {
                    stage = 4;
                    stageStartTime = currentTime;
                    alpha = 0f;
                }
                break;
            case 4:
                if (!victorySoundPlayed) {
                    victorySoundPlayed = true;
                    GameAudio.playVictorySound();
                }
                alpha = Math.min(1f, (float) elapsed / 800f);
                break;
        }
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, ui.Camera camera) {
        if (!active)
            return;

        if (stage == 0) {
            for (int i = 0; i < totalMonsters; i++) {
                if (explosionProgress[i] > 0 && explosionProgress[i] <= 9) {
                    int frameIdx = (explosionProgress[i] - 1) / 3;
                    if (frameIdx >= 0 && frameIdx < 3 && explosionFrames[frameIdx] != null) {
                        float screenX = camera.worldToScreenX(explosionX[i]);
                        float screenY = camera.worldToScreenY(explosionY[i]);
                        g.drawImage(explosionFrames[frameIdx], (int) screenX - 60, (int) screenY - 60, 120, 120, null);
                    }
                }
            }
        } else if (stage == 1) {
            g.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
            g.fillRect(0, 0, screenWidth, screenHeight);
        } else if (stage == 2) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, screenWidth, screenHeight);
            drawCenteredString(g, "CLEAR!", screenWidth, screenHeight / 2, new Font("微軟正黑體", Font.BOLD, 64),
                    new Color(100, 255, 100));
        } else if (stage == 3) {
            drawCredits(g, screenWidth, screenHeight);
        } else if (stage == 4) {
            drawStatsScreen(g, screenWidth, screenHeight);
        }
    }

    private void drawCredits(Graphics2D g, int screenWidth, int screenHeight) {
        long elapsed = System.currentTimeMillis() - stageStartTime;
        int yOffset = (int) (elapsed * 0.04f);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        drawCenteredString(g, "EARTH SAVED!", screenWidth, 100 - yOffset, new Font("微軟正黑體", Font.BOLD, 48),
                new Color(100, 255, 100));
        drawCenteredString(g, "生態系統正在恢復...", screenWidth, 180 - yOffset, new Font("微軟正黑體", Font.PLAIN, 28),
                Color.WHITE);

        Font titleFont = new Font("微軟正黑體", Font.BOLD, 28);
        Color titleColor = new Color(255, 200, 100);
        drawCenteredString(g, "製作人員: 廖宏閔 何政輝 李翰俊 王昱棋 林冠宇", screenWidth, 350 - yOffset, titleFont, titleColor);
        drawCenteredString(g, "程式設計: 廖宏閔 何政輝 李翰俊 王昱棋", screenWidth, 400 - yOffset, titleFont, titleColor);
        drawCenteredString(g, "美術設計: 廖宏閔", screenWidth, 450 - yOffset, titleFont, titleColor);
        drawCenteredString(g, "音樂音效: 廖宏閔 林冠宇", screenWidth, 500 - yOffset, titleFont, titleColor);

        drawCenteredString(g, "特別感謝: 所有為地球環境付出的人們", screenWidth, 560 - yOffset, new Font("微軟正黑體", Font.PLAIN, 18),
                new Color(200, 200, 200));

        drawCenteredString(g, "Thanks for playing!", screenWidth, 650 - yOffset, new Font("微軟正黑體", Font.BOLD, 36),
                new Color(255, 255, 100));
        drawCenteredString(g, "You saved the earth", screenWidth, 700 - yOffset, new Font("微軟正黑體", Font.PLAIN, 24),
                Color.WHITE);
    }

    private void drawStatsScreen(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        drawCenteredString(g, "CLEAR!", screenWidth, 100, new Font("微軟正黑體", Font.BOLD, 56), new Color(100, 255, 100));
        drawCenteredString(g, "恭喜你完成了遊戲！", screenWidth, 170, new Font("微軟正黑體", Font.PLAIN, 28), Color.WHITE);

        Font statFont = new Font("微軟正黑體", Font.PLAIN, 24);
        drawCenteredString(g, "擊殺怪物數: " + killCount, screenWidth, 250, statFont, Color.YELLOW);
        drawCenteredString(g, "最終等級: " + finalLevel, screenWidth, 300, statFont, Color.YELLOW);
        drawCenteredString(g, "回收垃圾數: " + totalRecycled, screenWidth, 400, statFont, Color.YELLOW);

        int btnW = 100, btnH = 100, spacing = 80;
        int startX = (screenWidth - (btnW * 2 + spacing)) / 2;
        int btnY = screenHeight - 150;

        restartRect = new Rectangle(startX, btnY, btnW, btnH);
        mainMenuRect = new Rectangle(startX + btnW + spacing, btnY, btnW, btnH);

        drawButton(g, restartImage, restartRect.x, restartRect.y, btnW, btnH, restartHover);
        drawButton(g, mainMenuImage, mainMenuRect.x, mainMenuRect.y, btnW, btnH, mainMenuHover);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawButton(Graphics2D g, BufferedImage image, int x, int y, int w, int h, boolean hover) {
        if (image != null)
            g.drawImage(image, x, y, w, h, null);
        else {
            g.setColor(hover ? new Color(80, 80, 80) : new Color(60, 60, 60));
            g.fillRoundRect(x, y, w, h, 15, 15);
            g.setColor(Color.WHITE);
            g.drawRoundRect(x, y, w, h, 15, 15);
        }

        if (hover) {
            g.setColor(new Color(255, 255, 0, 150));
            g.setStroke(new BasicStroke(4));
            g.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 15, 15);
            g.setStroke(new BasicStroke(1));
        }
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        if (!active || stage != 4)
            return;
        restartHover = restartRect != null && restartRect.contains(mouseX, mouseY);
        mainMenuHover = mainMenuRect != null && mainMenuRect.contains(mouseX, mouseY);
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        if (!active || stage != 4)
            return;
        if (restartRect != null && restartRect.contains(mouseX, mouseY) && onRestart != null)
            onRestart.run();
        else if (mainMenuRect != null && mainMenuRect.contains(mouseX, mouseY) && onMainMenu != null)
            onMainMenu.run();
    }

    public boolean isActive() {
        return active;
    }

    private void drawCenteredString(Graphics2D g, String text, int screenWidth, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, (screenWidth - g.getFontMetrics().stringWidth(text)) / 2, y);
    }
}