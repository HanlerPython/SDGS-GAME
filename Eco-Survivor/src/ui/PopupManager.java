package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import system.VolumeManager;

public class PopupManager {
    
    public enum PopupType {
        MONSTER, UPGRADE, PAUSE
    }
    
    private PopupType currentType = null;
    private boolean isShowing = false;
    private String monsterName = "";
    private String monsterDesc = "";
    private String monsterHarm = "";
    private String[] upgradeOptions;
    
    private int screenWidth = 1280;
    private int screenHeight = 720;
    
    // 暫停選單圖片
    private static BufferedImage pausePanelImage;
    private static BufferedImage resumeImage;
    private static BufferedImage restartImage;
    private static BufferedImage mainMenuImage;
    
    // 升級選單圖片
    private static BufferedImage upgradePanelImage;
    private static BufferedImage upgradeIconBgImage;
    private static BufferedImage upgradeTextBgImage;
    private BufferedImage[] upgradeIcons;
    
    // 暫停選單回調
    private Runnable onResumeCallback;
    private Runnable onRestartCallback;
    private Runnable onMainMenuCallback;
    
    // 升級選單回調
    private Runnable[] onUpgradeCallbacks;
    
    // 按鈕區域
    private Rectangle resumeRect;
    private Rectangle restartRect;
    private Rectangle mainMenuRect;
    private PauseButton hoveredButton = null;
    
    // 升級選單按鈕區域
    private List<Rectangle> upgradeRects;
    private int hoveredUpgradeIndex = -1;
    
    // 🔥 暫停選單音量控制
    private Rectangle pauseVolumeBar;
    private Rectangle pauseVolumeKnob;
    private boolean pauseDraggingVolume = false;
    private Rectangle pauseVolumeIconRect;
    private boolean pauseVolumeIconHover = false;
    private BufferedImage pauseVolumeIconImage;
    private BufferedImage pauseVolumeIconHoverImage;
    
    private enum PauseButton {
        RESUME, RESTART, MAIN_MENU
    }
    
    public PopupManager() {
        loadPauseMenuImages();
        loadUpgradeMenuImages();
        loadPauseVolumeIcon();
        upgradeRects = new ArrayList<>();
    }
    
    private void loadPauseVolumeIcon() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("volume_icon.png");
            if (url != null) {
                pauseVolumeIconImage = ImageIO.read(url);
            } else {
                File file = new File("res/volume_icon.png");
                if (file.exists()) {
                    pauseVolumeIconImage = ImageIO.read(file);
                }
            }
            
            url = getClass().getClassLoader().getResource("volume_icon_hover.png");
            if (url != null) {
                pauseVolumeIconHoverImage = ImageIO.read(url);
            } else {
                File file = new File("res/volume_icon_hover.png");
                if (file.exists()) {
                    pauseVolumeIconHoverImage = ImageIO.read(file);
                }
            }
            if (pauseVolumeIconHoverImage == null) pauseVolumeIconHoverImage = pauseVolumeIconImage;
        } catch (IOException e) {
            System.err.println("【PopupManager】載入音量圖示失敗");
        }
    }
    
    private void loadPauseMenuImages() {
        try {
            java.net.URL panelUrl = getClass().getClassLoader().getResource("pause_panel.png");
            if (panelUrl != null) {
                pausePanelImage = ImageIO.read(panelUrl);
            } else {
                File panelFile = new File("res/pause_panel.png");
                if (panelFile.exists()) {
                    pausePanelImage = ImageIO.read(panelFile);
                }
            }
            
            java.net.URL resumeUrl = getClass().getClassLoader().getResource("btn_resume.png");
            if (resumeUrl != null) {
                resumeImage = ImageIO.read(resumeUrl);
            } else {
                File resumeFile = new File("res/btn_resume.png");
                if (resumeFile.exists()) {
                    resumeImage = ImageIO.read(resumeFile);
                }
            }
            
            java.net.URL restartUrl = getClass().getClassLoader().getResource("btn_restart.png");
            if (restartUrl != null) {
                restartImage = ImageIO.read(restartUrl);
            } else {
                File restartFile = new File("res/btn_restart.png");
                if (restartFile.exists()) {
                    restartImage = ImageIO.read(restartFile);
                }
            }
            
            java.net.URL menuUrl = getClass().getClassLoader().getResource("btn_mainmenu.png");
            if (menuUrl != null) {
                mainMenuImage = ImageIO.read(menuUrl);
            } else {
                File menuFile = new File("res/btn_mainmenu.png");
                if (menuFile.exists()) {
                    mainMenuImage = ImageIO.read(menuFile);
                }
            }
        } catch (IOException e) {
            System.err.println("【PopupManager】載入暫停選單圖片失敗");
        }
    }
    
    private void loadUpgradeMenuImages() {
        try {
            java.net.URL panelUrl = getClass().getClassLoader().getResource("upgrade_panel.png");
            if (panelUrl != null) {
                upgradePanelImage = ImageIO.read(panelUrl);
            } else {
                File panelFile = new File("res/upgrade_panel.png");
                if (panelFile.exists()) {
                    upgradePanelImage = ImageIO.read(panelFile);
                }
            }
            
            java.net.URL iconBgUrl = getClass().getClassLoader().getResource("upgrade_icon_bg.png");
            if (iconBgUrl != null) {
                upgradeIconBgImage = ImageIO.read(iconBgUrl);
            } else {
                File iconBgFile = new File("res/upgrade_icon_bg.png");
                if (iconBgFile.exists()) {
                    upgradeIconBgImage = ImageIO.read(iconBgFile);
                }
            }

            java.net.URL textBgUrl = getClass().getClassLoader().getResource("upgrade_text_bg.png");
            if (textBgUrl != null) {
                upgradeTextBgImage = ImageIO.read(textBgUrl);
            } else {
                File textBgFile = new File("res/upgrade_text_bg.png");
                if (textBgFile.exists()) {
                    upgradeTextBgImage = ImageIO.read(textBgFile);
                }
            }
        } catch (IOException e) {
            System.err.println("【PopupManager】載入升級選單圖片失敗");
        }
    }
    
    public void showMonsterPopup(String name, String desc, String harm) {
        this.isShowing = true;
        this.currentType = PopupType.MONSTER;
        this.monsterName = name;
        this.monsterDesc = desc;
        this.monsterHarm = harm;
    }
    
    public void showUpgradePopup(String[] options, Runnable[] callbacks, String[] iconNames) {
        this.isShowing = true;
        this.currentType = PopupType.UPGRADE;
        this.upgradeOptions = options;
        this.onUpgradeCallbacks = callbacks;
        this.hoveredUpgradeIndex = -1;
        this.upgradeRects.clear();
        
        if (iconNames != null && iconNames.length > 0) {
            this.upgradeIcons = new BufferedImage[iconNames.length];
            for (int i = 0; i < iconNames.length; i++) {
                this.upgradeIcons[i] = loadUpgradeIcon(iconNames[i]);
            }
        }
    }
    
    private BufferedImage loadUpgradeIcon(String iconName) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(iconName);
            if (url != null) {
                return ImageIO.read(url);
            }
            File file = new File("res/" + iconName);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
            System.err.println("【PopupManager】載入圖案失敗: " + iconName);
        }
        return null;
    }
    
    public void showPauseMenu(Runnable onResume, Runnable onRestart, Runnable onMainMenu) {
        this.isShowing = true;
        this.currentType = PopupType.PAUSE;
        this.onResumeCallback = onResume;
        this.onRestartCallback = onRestart;
        this.onMainMenuCallback = onMainMenu;
        this.hoveredButton = null;
        this.pauseDraggingVolume = false;
    }
    
    public void closePopup() {
        this.isShowing = false;
        this.currentType = null;
        this.upgradeOptions = null;
        this.hoveredButton = null;
        this.hoveredUpgradeIndex = -1;
        this.pauseDraggingVolume = false;
    }
    
    public boolean isShowing() {
        return isShowing;
    }
    
    public PopupType getCurrentType() {
        return currentType;
    }
    
    public void draw(Graphics2D g) {
        if (!isShowing) return;
        
        if (currentType == PopupType.MONSTER) {
            drawMonsterPopup(g);
        } else if (currentType == PopupType.UPGRADE) {
            drawUpgradePopup(g);
        } else if (currentType == PopupType.PAUSE) {
            drawPauseMenu(g);
            drawPauseVolumeControl(g);  // 左下角音量控制
        }
    }
    
    private void drawMonsterPopup(Graphics2D g) {
        int popupW = 600;
        int popupH = 400;
        int popupX = (screenWidth - popupW) / 2;
        int popupY = (screenHeight - popupH) / 2;
        
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        g.setColor(new Color(30, 60, 30, 240));
        g.fillRoundRect(popupX, popupY, popupW, popupH, 20, 20);
        g.setColor(new Color(100, 200, 100));
        g.drawRoundRect(popupX, popupY, popupW, popupH, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 24));
        String title = "⚠ NEW THREAT ⚠";
        int titleX = popupX + (popupW - g.getFontMetrics().stringWidth(title)) / 2;
        g.drawString(title, titleX, popupY + 50);
        
        g.setFont(new Font("微軟正黑體", Font.BOLD, 22));
        g.setColor(new Color(255, 150, 150));
        int nameX = popupX + (popupW - g.getFontMetrics().stringWidth(monsterName)) / 2;
        g.drawString(monsterName, nameX, popupY + 110);
        
        g.setColor(new Color(100, 150, 100));
        g.drawLine(popupX + 40, popupY + 130, popupX + popupW - 40, popupY + 130);
        
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        int descX = popupX + (popupW - g.getFontMetrics().stringWidth(monsterDesc)) / 2;
        g.drawString(monsterDesc, descX, popupY + 170);
        
        g.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        g.setColor(new Color(255, 200, 100));
        g.drawString("🌍 Environmental Impact:", popupX + 50, popupY + 220);
        
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
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
        g.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        String btnText = "UNDERSTAND";
        int btnTextX = btnX + (btnW - g.getFontMetrics().stringWidth(btnText)) / 2;
        g.drawString(btnText, btnTextX, btnY + 27);
    }
    
    private void drawUpgradePopup(Graphics2D g) {
        if (upgradeOptions == null) return;
        
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        int panelW = 600;
        int panelH = 550;
        int panelX = centerX - panelW / 2;
        int panelY = centerY - panelH / 2;
        
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        if (upgradePanelImage != null) {
            g.drawImage(upgradePanelImage, panelX, panelY, panelW, panelH, null);
        } else {
            g.setColor(new Color(30, 60, 30));
            g.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
            g.setColor(Color.WHITE);
            g.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 36));
        String title = "LEVEL UP!";
        int titleX = centerX - g.getFontMetrics().stringWidth(title) / 2;
        g.drawString(title, titleX, panelY + 60);
        
        int optionHeight = 110;
        int optionSpacing = 20;
        int optionWidth = panelW - 100;
        int startY = panelY + 100;
        
        int iconBoxSize = 100;
        int textBoxWidth = optionWidth - iconBoxSize - 30;
        int iconBoxX = panelX + 55;
        int textBoxX = iconBoxX + iconBoxSize + 20;
        
        upgradeRects.clear();
        
        for (int i = 0; i < upgradeOptions.length; i++) {
            int optionY = startY + i * (optionHeight + optionSpacing);
            Rectangle rect = new Rectangle(panelX + 50, optionY, optionWidth, optionHeight);
            upgradeRects.add(rect);
            
            boolean isHovered = (hoveredUpgradeIndex == i);
            
            if (upgradeIconBgImage != null) {
                g.drawImage(upgradeIconBgImage, iconBoxX, optionY + (optionHeight - iconBoxSize) / 2, iconBoxSize, iconBoxSize, null);
            } else {
                g.setColor(isHovered ? new Color(80, 80, 50, 200) : new Color(50, 50, 30, 200));
                g.fillRoundRect(iconBoxX, optionY + (optionHeight - iconBoxSize) / 2, iconBoxSize, iconBoxSize, 10, 10);
                g.setColor(Color.WHITE);
                g.drawRoundRect(iconBoxX, optionY + (optionHeight - iconBoxSize) / 2, iconBoxSize, iconBoxSize, 10, 10);
            }
            
            if (upgradeTextBgImage != null) {
                g.drawImage(upgradeTextBgImage, textBoxX, optionY, textBoxWidth, optionHeight, null);
            } else {
                g.setColor(isHovered ? new Color(80, 80, 50, 200) : new Color(50, 50, 30, 200));
                g.fillRoundRect(textBoxX, optionY, textBoxWidth, optionHeight, 10, 10);
                g.setColor(Color.WHITE);
                g.drawRoundRect(textBoxX, optionY, textBoxWidth, optionHeight, 10, 10);
            }
            
            BufferedImage icon = null;
            if (upgradeIcons != null && i < upgradeIcons.length) {
                icon = upgradeIcons[i];
            }
            
            int iconSize = 140;
            int iconDrawX = iconBoxX + (iconBoxSize - iconSize) / 2;
            int iconDrawY = optionY + (optionHeight - iconSize) / 2;
            
            if (icon != null) {
                g.drawImage(icon, iconDrawX, iconDrawY, iconSize, iconSize, null);
            } else {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("微軟正黑體", Font.BOLD, 28));
                String num = String.valueOf(i + 1);
                int numX = iconBoxX + (iconBoxSize - g.getFontMetrics().stringWidth(num)) / 2;
                int numY = optionY + optionHeight / 2 + 10;
                g.drawString(num, numX, numY);
            }
            
            g.setColor(Color.BLACK);
            g.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
            int textStartY = optionY + 45;
            drawWrappedText(g, upgradeOptions[i], textBoxX + 15, textStartY, textBoxWidth - 30, 28);
            
            if (isHovered) {
                g.setColor(new Color(255, 255, 0, 200));
                g.setStroke(new BasicStroke(4));
                g.drawRoundRect(panelX + 50, optionY, optionWidth, optionHeight, 15, 15);
                g.setStroke(new BasicStroke(1));
            }
        }
    }
    
    private void drawPauseMenu(Graphics2D g) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        // 🔥 只有畫面變暗，沒有底板
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        // 按鈕（橫向排列）
        int buttonWidth = 100;
        int buttonHeight = 100;
        int spacing = 40;
        int totalWidth = buttonWidth * 3 + spacing * 2;
        int startX = centerX - totalWidth / 2;
        int buttonY = centerY - buttonHeight / 2;
        
        resumeRect = new Rectangle(startX, buttonY, buttonWidth, buttonHeight);
        restartRect = new Rectangle(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight);
        mainMenuRect = new Rectangle(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, buttonHeight);
        
        drawButton(g, resumeImage, resumeRect.x, resumeRect.y, buttonWidth, buttonHeight, 
                   hoveredButton == PauseButton.RESUME, "▶");
        drawButton(g, restartImage, restartRect.x, restartRect.y, buttonWidth, buttonHeight,
                   hoveredButton == PauseButton.RESTART, "⟳");
        drawButton(g, mainMenuImage, mainMenuRect.x, mainMenuRect.y, buttonWidth, buttonHeight,
                   hoveredButton == PauseButton.MAIN_MENU, "🏠");
    }

    

    private void drawPauseVolumeControl(Graphics2D g) {
        int volume = VolumeManager.getVolume();
        
        int barWidth = 200;
        int barHeight = 8;
        int barX = 80;
        int barY = screenHeight - 40;
        
        pauseVolumeBar = new Rectangle(barX, barY, barWidth, barHeight);
        pauseVolumeKnob = new Rectangle(barX + (volume * barWidth / 100) - 8, barY - 6, 16, 20);
        pauseVolumeIconRect = new Rectangle(30, barY - 5, 32, 32);
        
        // 音量圖示
        BufferedImage icon = pauseVolumeIconHover ? pauseVolumeIconHoverImage : pauseVolumeIconImage;
        if (icon != null) {
            // 調整這個 drawY 的數字（越大圖示越往下）
            int drawY = pauseVolumeIconRect.y - 10;  // 🔥 改這個數字
            g.drawImage(icon, pauseVolumeIconRect.x, drawY, pauseVolumeIconRect.width, pauseVolumeIconRect.height, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
            g.drawString("🔊", pauseVolumeIconRect.x, pauseVolumeIconRect.y + 24);
        }
        
        // 音量條背景
        g.setColor(new Color(60, 60, 60, 200));
        g.fillRect(pauseVolumeBar.x, pauseVolumeBar.y, pauseVolumeBar.width, pauseVolumeBar.height);
        
        // 音量條進度
        g.setColor(new Color(100, 200, 100));
        int filledWidth = (volume * pauseVolumeBar.width / 100);
        g.fillRect(pauseVolumeBar.x, pauseVolumeBar.y, filledWidth, pauseVolumeBar.height);
        
        // 音量條邊框
        g.setColor(Color.WHITE);
        g.drawRect(pauseVolumeBar.x, pauseVolumeBar.y, pauseVolumeBar.width, pauseVolumeBar.height);
        
        // 音量旋鈕
        int knobX = pauseVolumeBar.x + (volume * pauseVolumeBar.width / 100) - 8;
        pauseVolumeKnob.setLocation(knobX, pauseVolumeBar.y - 6);
        g.setColor(new Color(100, 200, 100));
        g.fillRoundRect(pauseVolumeKnob.x, pauseVolumeKnob.y, 16, 20, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(pauseVolumeKnob.x, pauseVolumeKnob.y, 16, 20, 8, 8);
        
        // 音量百分比
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        g.drawString(volume + "%", pauseVolumeBar.x + pauseVolumeBar.width + 10, pauseVolumeBar.y + 10);
    }
    
    private void drawPauseVolumeControl(Graphics2D g, int startX, int startY) {
        int volume = VolumeManager.getVolume();
        
        int barWidth = 200;
        int barHeight = 8;
        int barX = startX + 80;
        int barY = startY;
        
        pauseVolumeBar = new Rectangle(barX, barY, barWidth, barHeight);
        pauseVolumeKnob = new Rectangle(barX + (volume * barWidth / 100) - 8, barY - 6, 16, 20);
        pauseVolumeIconRect = new Rectangle(startX + 40, barY - 5, 40, 40);
        
        BufferedImage icon = pauseVolumeIconHover ? pauseVolumeIconHoverImage : pauseVolumeIconImage;
        if (icon != null) {
            g.drawImage(icon, pauseVolumeIconRect.x, pauseVolumeIconRect.y, 32, 32, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
            g.drawString("🔊", pauseVolumeIconRect.x, pauseVolumeIconRect.y + 24);
        }
        
        g.setColor(new Color(60, 60, 60, 200));
        g.fillRect(pauseVolumeBar.x, pauseVolumeBar.y, pauseVolumeBar.width, pauseVolumeBar.height);
        
        g.setColor(new Color(100, 200, 100));
        int filledWidth = (volume * pauseVolumeBar.width / 100);
        g.fillRect(pauseVolumeBar.x, pauseVolumeBar.y, filledWidth, pauseVolumeBar.height);
        
        g.setColor(Color.WHITE);
        g.drawRect(pauseVolumeBar.x, pauseVolumeBar.y, pauseVolumeBar.width, pauseVolumeBar.height);
        
        int knobX = pauseVolumeBar.x + (volume * pauseVolumeBar.width / 100) - 8;
        pauseVolumeKnob.setLocation(knobX, pauseVolumeBar.y - 6);
        g.setColor(new Color(100, 200, 100));
        g.fillRoundRect(pauseVolumeKnob.x, pauseVolumeKnob.y, 16, 20, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(pauseVolumeKnob.x, pauseVolumeKnob.y, 16, 20, 8, 8);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        g.drawString(volume + "%", pauseVolumeBar.x + pauseVolumeBar.width + 10, pauseVolumeBar.y + 10);
        
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("音量", startX + 5, barY + 12);
    }
    
    private void drawButton(Graphics2D g, BufferedImage image, int x, int y, int width, int height, 
                            boolean isHovered, String fallbackText) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(isHovered ? new Color(80, 80, 80) : new Color(60, 60, 60));
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.BOLD, 24));
            int textX = x + (width - g.getFontMetrics().stringWidth(fallbackText)) / 2;
            int textY = y + height / 2 + 8;
            g.drawString(fallbackText, textX, textY);
        }
        
        if (isHovered) {
            g.setColor(new Color(255, 255, 0, 200));
            g.setStroke(new BasicStroke(4));
            g.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 10, 10);
            g.setColor(new Color(255, 255, 0, 80));
            g.setStroke(new BasicStroke(6));
            g.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 12, 12);
            g.setStroke(new BasicStroke(1));
        }
    }
    
    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        
        for (String word : words) {
            if (fm.stringWidth(line.toString() + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g.drawString(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word + " ");
            }
        }
        g.drawString(line.toString(), x, currentY);
    }
    
    public boolean isCloseButtonClicked(int mouseX, int mouseY) {
        if (!isShowing) return false;
        
        if (currentType == PopupType.MONSTER) {
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
        return false;
    }
    
    public void updateHover(int mouseX, int mouseY) {
        if (!isShowing) return;
        
        if (currentType == PopupType.PAUSE) {
            if (resumeRect != null && resumeRect.contains(mouseX, mouseY)) {
                hoveredButton = PauseButton.RESUME;
            } else if (restartRect != null && restartRect.contains(mouseX, mouseY)) {
                hoveredButton = PauseButton.RESTART;
            } else if (mainMenuRect != null && mainMenuRect.contains(mouseX, mouseY)) {
                hoveredButton = PauseButton.MAIN_MENU;
            } else {
                hoveredButton = null;
            }
            
            pauseVolumeIconHover = pauseVolumeIconRect != null && pauseVolumeIconRect.contains(mouseX, mouseY);
            
            if (pauseDraggingVolume && pauseVolumeBar != null) {
                int newVolume = (mouseX - pauseVolumeBar.x) * 100 / pauseVolumeBar.width;
                newVolume = Math.max(0, Math.min(100, newVolume));
                VolumeManager.setVolume(newVolume);
            }
        } else if (currentType == PopupType.UPGRADE) {
            hoveredUpgradeIndex = -1;
            for (int i = 0; i < upgradeRects.size(); i++) {
                if (upgradeRects.get(i).contains(mouseX, mouseY)) {
                    hoveredUpgradeIndex = i;
                    break;
                }
            }
        }
    }
    
    public void handlePauseMenuClick(int mouseX, int mouseY) {
        if (!isShowing || currentType != PopupType.PAUSE) return;
        
        if (pauseVolumeBar != null && (pauseVolumeBar.contains(mouseX, mouseY) || 
            (pauseVolumeKnob != null && pauseVolumeKnob.contains(mouseX, mouseY)))) {
            pauseDraggingVolume = true;
            int newVolume = (mouseX - pauseVolumeBar.x) * 100 / pauseVolumeBar.width;
            newVolume = Math.max(0, Math.min(100, newVolume));
            VolumeManager.setVolume(newVolume);
            return;
        }
        
        if (resumeRect != null && resumeRect.contains(mouseX, mouseY)) {
            if (onResumeCallback != null) onResumeCallback.run();
            closePopup();
            return;
        }
        
        if (restartRect != null && restartRect.contains(mouseX, mouseY)) {
            if (onRestartCallback != null) onRestartCallback.run();
            closePopup();
            return;
        }
        
        if (mainMenuRect != null && mainMenuRect.contains(mouseX, mouseY)) {
            if (onMainMenuCallback != null) onMainMenuCallback.run();
            closePopup();
            return;
        }
    }
    
    public void handlePauseMenuRelease() {
        pauseDraggingVolume = false;
    }
    
    public void handleUpgradeClick(int mouseX, int mouseY) {
        if (!isShowing || currentType != PopupType.UPGRADE) return;
        
        for (int i = 0; i < upgradeRects.size(); i++) {
            if (upgradeRects.get(i).contains(mouseX, mouseY)) {
                if (onUpgradeCallbacks != null && i < onUpgradeCallbacks.length && onUpgradeCallbacks[i] != null) {
                    onUpgradeCallbacks[i].run();
                }
                closePopup();
                break;
            }
        }
    }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}