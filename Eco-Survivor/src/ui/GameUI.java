package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameUI {

    private int panelX = 0, panelY = -30, panelW = 320, panelH = 250;
    private int hpBarX = 35, hpBarY = 35, hpBarW = 250, hpBarH = 22;
    private int expBarX = 35, expBarY = 65, expBarW = 250, expBarH = 16;

    private Color bgPanel = new Color(0, 0, 0, 180);
    private Color hpColor = new Color(220, 50, 50);
    private Color expColor = new Color(50, 200, 50);
    private Color textColor = Color.WHITE;

    private Font normalFont = new Font("", Font.PLAIN, 16);
    private Font boldFont = new Font("", Font.BOLD, 16);

    private int currentHP, maxHP;
    private int currentExp, expToNext;
    private int currentLevel;
    private int currentAttack;
    private int totalRecycled;

    private static BufferedImage restartButtonImage;
    private static BufferedImage mainMenuButtonImage;
    private Rectangle gameOverRestartRect;
    private Rectangle gameOverMenuRect;

    private void loadGameOverButtons() {
        if (restartButtonImage == null) {
            restartButtonImage = loadButtonImage("btn_restart.png");
        }
        if (mainMenuButtonImage == null) {
            mainMenuButtonImage = loadButtonImage("btn_mainmenu.png");
        }
    }

    private BufferedImage loadButtonImage(String fileName) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(fileName);
            if (url != null) {
                return ImageIO.read(url);
            }
            File file = new File("res/" + fileName);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
        }
        return null;
    }

    public Rectangle getGameOverRestartRect() {
        return gameOverRestartRect;
    }

    public Rectangle getGameOverMenuRect() {
        return gameOverMenuRect;
    }

    private static BufferedImage panelImage;

    public GameUI() {
        loadPanelImage();
    }

    private void loadPanelImage() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("info_panel.png");
            if (url != null) {
                panelImage = ImageIO.read(url);
                return;
            }
            File file = new File("res/info_panel.png");
            if (file.exists()) {
                panelImage = ImageIO.read(file);
            }
        } catch (IOException e) {
        }
    }

    public void updateStats(int hp, int maxHp, int exp, int expToNext,
            int level, int attack, int recycled) {
        this.currentHP = hp;
        this.maxHP = maxHp;
        this.currentExp = exp;
        this.expToNext = expToNext;
        this.currentLevel = level;
        this.currentAttack = attack;
        this.totalRecycled = recycled;
    }

    public void draw(Graphics2D g) {
        drawPanel(g);
        drawHealthBar(g);
        drawExpBar(g);
        drawStats(g);
    }

    private void drawPanel(Graphics2D g) {
        if (panelImage != null) {
            g.drawImage(panelImage, panelX, panelY, panelW, panelH, null);
        } else {
            g.setColor(bgPanel);
            g.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        }
    }

    private void drawHealthBar(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(hpBarX, hpBarY, hpBarW, hpBarH);

        g.setColor(hpColor);
        int hpWidth = (int) (hpBarW * (double) currentHP / maxHP);
        g.fillRect(hpBarX, hpBarY, hpWidth, hpBarH);

        g.setColor(Color.BLACK);
        g.drawRect(hpBarX, hpBarY, hpBarW, hpBarH);

        g.setColor(textColor);
        g.setFont(normalFont);
        g.drawString("❤ " + currentHP + "/" + maxHP, hpBarX + 8, hpBarY + 16);
    }

    private void drawExpBar(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(expBarX, expBarY, expBarW, expBarH);

        g.setColor(expColor);
        int expWidth = (int) (expBarW * (double) currentExp / expToNext);
        g.fillRect(expBarX, expBarY, expWidth, expBarH);

        g.setColor(Color.BLACK);
        g.drawRect(expBarX, expBarY, expBarW, expBarH);

        g.setColor(textColor);
        g.setFont(normalFont);
        g.drawString("⭐ EXP: " + currentExp + "/" + expToNext, expBarX + 8, expBarY + 12);
    }

    private void drawStats(Graphics2D g) {
        g.setColor(textColor);
        g.setFont(normalFont);
        g.drawString("🎚 Lv." + currentLevel, panelX + 25, expBarY + 38);
        g.drawString("⚔ ATK: " + currentAttack, panelX + 25, expBarY + 58);
        g.drawString("♻ RECYCLED: " + totalRecycled, panelX + 25, expBarY + 78);
    }

    public void drawGameOver(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 72));
        String title = "失敗(悲)";
        int titleX = (screenWidth - g.getFontMetrics().stringWidth(title)) / 2;
        g.drawString(title, titleX, screenHeight / 2 - 80);

        g.setFont(new Font("微軟正黑體", Font.PLAIN, 24));
        g.setColor(new Color(200, 200, 200));
        String subtitle = "GAME OVER";
        int subtitleX = (screenWidth - g.getFontMetrics().stringWidth(subtitle)) / 2;
        g.drawString(subtitle, subtitleX, screenHeight / 2 - 30);

        loadGameOverButtons();

        int btnW = 80;
        int btnH = 80;
        int spacing = 60;
        int totalWidth = btnW * 2 + spacing;
        int startX = (screenWidth - totalWidth) / 2;
        int btnY = screenHeight / 2 + 50;

        int restartX = startX;
        if (restartButtonImage != null) {
            g.drawImage(restartButtonImage, restartX, btnY, btnW, btnH, null);
        } else {
            g.setColor(new Color(200, 100, 0));
            g.fillRoundRect(restartX, btnY, btnW, btnH, 15, 15);
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.BOLD, 20));
            String restartText = "重來";
            int restartTextX = restartX + (btnW - g.getFontMetrics().stringWidth(restartText)) / 2;
            g.drawString(restartText, restartTextX, btnY + 65);
        }

        int menuX = startX + btnW + spacing;
        if (mainMenuButtonImage != null) {
            g.drawImage(mainMenuButtonImage, menuX, btnY, btnW, btnH, null);
        } else {
            g.setColor(new Color(100, 100, 100));
            g.fillRoundRect(menuX, btnY, btnW, btnH, 15, 15);
            g.setColor(Color.WHITE);
            String menuText = "主畫面";
            int menuTextX = menuX + (btnW - g.getFontMetrics().stringWidth(menuText)) / 2;
            g.drawString(menuText, menuTextX, btnY + 65);
        }

        gameOverRestartRect = new Rectangle(restartX, btnY, btnW, btnH);
        gameOverMenuRect = new Rectangle(menuX, btnY, btnW, btnH);
    }

    public void drawUpgradeMenu(Graphics2D g, int screenWidth, int screenHeight, String[] options) {
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, screenWidth, screenHeight);

        int menuW = 500;
        int menuH = 350;
        int menuX = (screenWidth - menuW) / 2;
        int menuY = (screenHeight - menuH) / 2;

        g.setColor(new Color(30, 60, 30));
        g.fillRoundRect(menuX, menuY, menuW, menuH, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(menuX, menuY, menuW, menuH, 20, 20);

        g.setFont(boldFont);
        g.drawString("LEVEL UP!", menuX + menuW / 2 - 50, menuY + 50);

        g.setFont(normalFont);
        for (int i = 0; i < options.length; i++) {
            g.drawString((i + 1) + ". " + options[i], menuX + 50, menuY + 120 + i * 60);
        }

        g.drawString("Press 1, 2, 3 to choose", menuX + menuW / 2 - 100, menuY + menuH - 40);
    }

    public void reset() {
        currentHP = maxHP;
        currentExp = 0;
        totalRecycled = 0;
    }
}