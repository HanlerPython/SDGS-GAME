package ui;

import java.awt.*;

public class PopupManager {
    
    private boolean isShowing = false;
    private String monsterName = "";
    private String monsterDesc = "";
    private String monsterHarm = "";
    
    private int screenWidth = 1920;
    private int screenHeight = 1080;
    
    public void showMonsterPopup(String name, String desc, String harm) {
        this.isShowing = true;
        this.monsterName = name;
        this.monsterDesc = desc;
        this.monsterHarm = harm;
    }
    
    public void closePopup() {
        this.isShowing = false;
    }
    
    public boolean isShowing() {
        return isShowing;
    }
    
    public void draw(Graphics2D g) {
        if (!isShowing) return;
        
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        int popupW = 600;
        int popupH = 400;
        int popupX = (screenWidth - popupW) / 2;
        int popupY = (screenHeight - popupH) / 2;
        
        g.setColor(new Color(30, 60, 30, 240));
        g.fillRoundRect(popupX, popupY, popupW, popupH, 20, 20);
        g.setColor(new Color(100, 200, 100));
        g.drawRoundRect(popupX, popupY, popupW, popupH, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String title = "⚠ NEW THREAT ⚠";
        int titleX = popupX + (popupW - g.getFontMetrics().stringWidth(title)) / 2;
        g.drawString(title, titleX, popupY + 50);
        
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(new Color(255, 150, 150));
        int nameX = popupX + (popupW - g.getFontMetrics().stringWidth(monsterName)) / 2;
        g.drawString(monsterName, nameX, popupY + 110);
        
        g.setColor(new Color(100, 150, 100));
        g.drawLine(popupX + 40, popupY + 130, popupX + popupW - 40, popupY + 130);
        
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        int descX = popupX + (popupW - g.getFontMetrics().stringWidth(monsterDesc)) / 2;
        g.drawString(monsterDesc, descX, popupY + 170);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(255, 200, 100));
        g.drawString("🌍 Environmental Impact:", popupX + 50, popupY + 220);
        
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        drawWrappedText(g, monsterHarm, popupX + 50, popupY + 250, popupW - 100, 25);
        
        int btnW = 140;
        int btnH = 40;
        int btnX = popupX + (popupW - btnW) / 2;
        int btnY = popupY + popupH - 60;
        
        g.setColor(new Color(0, 150, 0));
        g.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String btnText = "UNDERSTAND";
        int btnTextX = btnX + (btnW - g.getFontMetrics().stringWidth(btnText)) / 2;
        g.drawString(btnText, btnTextX, btnY + 27);
    }
    
    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            if (fm.stringWidth(line.toString() + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g.drawString(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(word + " ");
            }
        }
        g.drawString(line.toString(), x, y);
    }
    
    public boolean isCloseButtonClicked(int mouseX, int mouseY) {
        if (!isShowing) return false;
        
        int popupW = 600;
        int popupH = 400;
        int popupX = (screenWidth - popupW) / 2;
        int popupY = (screenHeight - popupH) / 2;
        
        int btnW = 140;
        int btnH = 40;
        int btnX = popupX + (popupW - btnW) / 2;
        int btnY = popupY + popupH - 60;
        
        return (mouseX >= btnX && mouseX <= btnX + btnW &&
                mouseY >= btnY && mouseY <= btnY + btnH);
    }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}