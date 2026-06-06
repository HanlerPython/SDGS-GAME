package ui;

import java.awt.*;

public class GameUI {
    
    private int panelX = 20, panelY = 20, panelW = 280, panelH = 150;
    private int hpBarX = 35, hpBarY = 35, hpBarW = 250, hpBarH = 22;
    private int expBarX = 35, expBarY = 65, expBarW = 250, expBarH = 16;
    
    private Color bgPanel = new Color(0, 0, 0, 180);
    private Color hpColor = new Color(220, 50, 50);
    private Color expColor = new Color(50, 200, 50);
    private Color textColor = Color.WHITE;
    
    private Font normalFont = new Font("Arial", Font.PLAIN, 14);
    private Font boldFont = new Font("Arial", Font.BOLD, 16);
    private Font titleFont = new Font("Arial", Font.BOLD, 36);
    
    private int currentHP, maxHP;
    private int currentExp, expToNext;
    private int currentLevel;
    private int currentAttack;
    private int totalRecycled;
    
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
        g.setColor(bgPanel);
        g.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);
    }
    
    private void drawHealthBar(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(hpBarX, hpBarY, hpBarW, hpBarH);
        
        g.setColor(hpColor);
        int hpWidth = (int)(hpBarW * (double)currentHP / maxHP);
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
        int expWidth = (int)(expBarW * (double)currentExp / expToNext);
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
        g.drawString("🎚 Lv." + currentLevel, panelX + 15, expBarY + 38);
        g.drawString("⚔ ATK: " + currentAttack, panelX + 15, expBarY + 58);
        g.drawString("♻ RECYCLED: " + totalRecycled, panelX + 15, expBarY + 78);
    }
    
    public void drawGameOver(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        g.setColor(Color.WHITE);
        g.setFont(titleFont);
        String title = "GAME OVER";
        int titleX = (screenWidth - g.getFontMetrics().stringWidth(title)) / 2;
        g.drawString(title, titleX, screenHeight / 2 - 50);
        
        g.setFont(normalFont);
        String scoreText = "You recycled " + totalRecycled + " trash items";
        int scoreX = (screenWidth - g.getFontMetrics().stringWidth(scoreText)) / 2;
        g.drawString(scoreText, scoreX, screenHeight / 2);
        
        String restartText = "Press R to restart";
        int restartX = (screenWidth - g.getFontMetrics().stringWidth(restartText)) / 2;
        g.drawString(restartText, restartX, screenHeight / 2 + 50);
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
        g.drawString("LEVEL UP!", menuX + menuW/2 - 50, menuY + 50);
        
        g.setFont(normalFont);
        for (int i = 0; i < options.length; i++) {
            g.drawString((i+1) + ". " + options[i], menuX + 50, menuY + 120 + i * 60);
        }
        
        g.drawString("Press 1, 2, 3 to choose", menuX + menuW/2 - 100, menuY + menuH - 40);
    }
    
    public void reset() {
        currentHP = maxHP;
        currentExp = 0;
        totalRecycled = 0;
    }
}