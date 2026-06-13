package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import system.MonsterData;
import system.MapSystem;

public class MonsterGallery {

    private boolean active = false;
    private int currentIndex = 0;
    private int totalMonsters;
    private List<MonsterData.MonsterInfo> monsterList;

    private int slideOffset = 0;
    private int targetOffset = 0;
    private Timer slideTimer;
    private boolean isSliding = false;
    private static final int SLIDE_DURATION = 300;
    private static final int SLIDE_STEPS = 20;
    private int currentStep = 0;
    private int startOffset = 0;
    private int endOffset = 0;

    private BufferedImage[][] monsterImages;
    private BufferedImage leftArrowImage;
    private BufferedImage rightArrowImage;
    private BufferedImage confirmImage;
    private BufferedImage panelImage;

    private Rectangle leftArrowRect;
    private Rectangle rightArrowRect;
    private Rectangle confirmRect;
    private int panelX, panelY, panelWidth, panelHeight;
    private int screenWidth;
    private Runnable onClose;

    public MonsterGallery(Runnable onClose) {
        this.onClose = onClose;
        loadMonsterData();
        loadImages();
        initSlideTimer();
    }

    private void loadMonsterData() {
        monsterList = new ArrayList<>();
        for (int i = 0; i < MonsterData.getMonsterCount(); i++) {
            monsterList.add(MonsterData.getMonster(i));
        }
        totalMonsters = monsterList.size();
        monsterImages = new BufferedImage[totalMonsters][2];
    }

    private void loadImages() {
        panelImage = loadImage("gallery_panel.png");
        leftArrowImage = loadImage("arrow_left.png");
        rightArrowImage = loadImage("arrow_right.png");
        confirmImage = loadImage("btn_confirm.png");

        if (totalMonsters > 0) {
            monsterImages[0][0] = loadImage("jellyfish_1.png");
            monsterImages[0][1] = loadImage("jellyfish_2.png");
        }
        if (totalMonsters > 1) {
            monsterImages[1][0] = loadImage("slime_1.png");
            monsterImages[1][1] = loadImage("slime_2.png");
        }
        if (totalMonsters > 2) {
            monsterImages[2][0] = loadImage("mosquito.png");
            monsterImages[2][1] = loadImage("mosquito.png");
        }
        if (totalMonsters > 3) {
            monsterImages[3][0] = loadImage("smoKing_1.png");
            monsterImages[3][1] = loadImage("smoKing_2.png");
        }
        if (totalMonsters > 4) {
            monsterImages[4][0] = loadImage("trashball.png");
            monsterImages[4][1] = loadImage("trashball.png");
        }
    }

    private BufferedImage loadImage(String fileName) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(fileName);
            if (url != null)
                return ImageIO.read(url);
            File file = new File("res/" + fileName);
            if (file.exists())
                return ImageIO.read(file);
        } catch (IOException ignored) {
        }
        return null;
    }

    private void initSlideTimer() {
        slideTimer = new Timer(SLIDE_DURATION / SLIDE_STEPS, e -> {
            currentStep++;
            float progress = (float) currentStep / SLIDE_STEPS;
            float easeProgress = 1 - (float) Math.pow(1 - progress, 2);
            slideOffset = startOffset + (int) ((endOffset - startOffset) * easeProgress);

            if (currentStep >= SLIDE_STEPS) {
                slideTimer.stop();
                isSliding = false;
                slideOffset = targetOffset;
            }
        });
        slideTimer.setRepeats(true);
    }

    public void start() {
        this.active = true;
        this.currentIndex = 0;
        this.slideOffset = 0;
        this.targetOffset = 0;
        this.isSliding = false;
        leftArrowRect = null;
        rightArrowRect = null;
        confirmRect = null;
    }

    public void stop() {
        this.active = false;
        if (slideTimer != null && slideTimer.isRunning())
            slideTimer.stop();
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, Camera camera, MapSystem mapSystem) {
        if (!active)
            return;
        this.screenWidth = screenWidth;

        java.awt.geom.AffineTransform oldTransform = g.getTransform();
        mapSystem.drawMap(g, camera, screenWidth, screenHeight);
        g.setTransform(oldTransform);

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, screenWidth, screenHeight);

        panelWidth = 600;
        panelHeight = 500;
        panelX = (screenWidth - panelWidth) / 2 + slideOffset;
        panelY = (screenHeight - panelHeight) / 2;

        if (panelImage != null) {
            g.drawImage(panelImage, panelX, panelY, panelWidth, panelHeight, null);
        } else {
            g.setColor(new Color(30, 60, 30, 240));
            g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
            g.setColor(new Color(100, 200, 100));
            g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        }

        drawMonsterImage(g);
        drawMonsterInfo(g);
        drawArrows(g);
        drawConfirmButton(g);
    }

    private void drawMonsterImage(Graphics2D g) {
        if (currentIndex >= totalMonsters)
            return;

        int imageSize = 180;
        int imageX = panelX + (panelWidth - imageSize) / 2;
        int imageY = panelY + 70;

        BufferedImage currentImage = null;
        if (monsterImages[currentIndex] != null) {
            int frameIndex = (int) ((System.currentTimeMillis() / 500) % 2);
            currentImage = monsterImages[currentIndex][frameIndex];
        }

        if (currentImage != null) {
            g.drawImage(currentImage, imageX, imageY, imageSize, imageSize, null);
        } else {
            g.setColor(new Color(150, 100, 150));
            g.fillOval(imageX, imageY, imageSize, imageSize);
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("?", imageX + (imageSize - fm.stringWidth("?")) / 2,
                    imageY + (imageSize + fm.getAscent()) / 2 - 4);
        }
    }

    private void drawMonsterInfo(Graphics2D g) {
        if (currentIndex >= totalMonsters)
            return;

        MonsterData.MonsterInfo info = monsterList.get(currentIndex);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 28));
        g.setColor(new Color(255, 200, 100));
        g.drawString(info.name, panelX + (panelWidth - g.getFontMetrics().stringWidth(info.name)) / 2, panelY + 300);

        g.setColor(new Color(100, 150, 100));
        g.drawLine(panelX + 50, panelY + 320, panelX + panelWidth - 50, panelY + 320);

        g.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int descMaxWidth = panelWidth - 100;
        int lineY = panelY + 360;

        StringBuilder line = new StringBuilder();
        for (char c : info.desc.toCharArray()) {
            if (fm.stringWidth(line.toString() + c) < descMaxWidth) {
                line.append(c);
            } else {
                g.drawString(line.toString(), panelX + 50, lineY);
                lineY += 28;
                line = new StringBuilder(String.valueOf(c));
            }
        }
        if (line.length() > 0)
            g.drawString(line.toString(), panelX + 50, lineY);

        g.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        g.setColor(new Color(255, 150, 150));
        g.drawString("環境危害: " + info.harm, panelX + 49, panelY + 415);
    }

    private void drawArrows(Graphics2D g) {
        int arrowSize = 100;
        int arrowY = panelY + panelHeight / 2 - arrowSize / 2;

        if (currentIndex > 0) {
            int leftX = panelX - arrowSize - 20;
            leftArrowRect = new Rectangle(leftX, arrowY, arrowSize, arrowSize);
            drawButtonElement(g, leftArrowImage, leftX, arrowY, arrowSize, "<");
        } else
            leftArrowRect = null;

        if (currentIndex < totalMonsters - 1) {
            int rightX = panelX + panelWidth + 20;
            rightArrowRect = new Rectangle(rightX, arrowY, arrowSize, arrowSize);
            drawButtonElement(g, rightArrowImage, rightX, arrowY, arrowSize, ">");
        } else
            rightArrowRect = null;
    }

    private void drawButtonElement(Graphics2D g, BufferedImage img, int x, int y, int size, String fallbackText) {
        if (img != null) {
            g.drawImage(img, x, y, size, size, null);
        } else {
            g.setColor(new Color(200, 200, 200));
            g.fillRoundRect(x, y, size, size, 10, 10);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString(fallbackText, x + 15, y + 38);
        }
    }

    private void drawConfirmButton(Graphics2D g) {
        int btnWidth = 120;
        int btnHeight = 50;
        int btnX = (screenWidth - panelWidth) / 2 + (panelWidth - btnWidth) / 2;
        int btnY = panelY + panelHeight + 10;
        confirmRect = new Rectangle(btnX, btnY, btnWidth, btnHeight);

        if (confirmImage != null)
            g.drawImage(confirmImage, btnX, btnY, btnWidth, btnHeight, null);
        else {
            g.setColor(new Color(0, 150, 0));
            g.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 15, 15);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("確認", btnX + (btnWidth - fm.stringWidth("確認")) / 2, btnY + (btnHeight + fm.getAscent()) / 2 - 4);
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        if (!active)
            return;
        if (leftArrowRect != null && leftArrowRect.contains(mouseX, mouseY))
            switchToPrevious();
        else if (rightArrowRect != null && rightArrowRect.contains(mouseX, mouseY))
            switchToNext();
        else if (confirmRect != null && confirmRect.contains(mouseX, mouseY))
            close();
    }

    private void switchToPrevious() {
        if (isSliding || currentIndex <= 0)
            return;
        currentIndex--;
        startSlide();
    }

    private void switchToNext() {
        if (isSliding || currentIndex >= totalMonsters - 1)
            return;
        currentIndex++;
        startSlide();
    }

    private void startSlide() {
        startOffset = slideOffset;
        endOffset = 0;
        targetOffset = 0;
        currentStep = 0;
        isSliding = true;
        if (slideTimer != null)
            slideTimer.stop();
        slideTimer.start();
    }

    private void close() {
        active = false;
        if (onClose != null)
            onClose.run();
    }

    public boolean isActive() {
        return active;
    }
}