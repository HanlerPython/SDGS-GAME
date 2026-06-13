package ui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import system.MapSystem;
import system.VolumeManager;

public class MainMenu {

    private Rectangle startButton;
    private Rectangle monsterButton;
    private Rectangle exitButton;

    private boolean startHover = false;
    private boolean monsterHover = false;
    private boolean exitHover = false;

    private BufferedImage startImage;
    private BufferedImage startHoverImage;
    private BufferedImage monsterImage;
    private BufferedImage monsterHoverImage;
    private BufferedImage exitImage;
    private BufferedImage exitHoverImage;
    private BufferedImage volumeIconImage;
    private BufferedImage volumeIconHoverImage;
    private boolean volumeIconHover = false;
    private Rectangle volumeIconRect;

    private boolean draggingVolume = false;
    private Rectangle volumeBar;
    private Rectangle volumeKnob;

    private MapSystem mapSystem;
    private float bgOffsetX = 0, bgOffsetY = 0;

    private float targetOffsetX, targetOffsetY;
    private float currentOffsetX, currentOffsetY;
    private float moveSpeed = 0.005f;
    private float totalMoveTime = 5000;
    private Random random = new Random();

    private float centerX, centerY;
    private float minOffsetX = -400;
    private float maxOffsetX = 400;
    private float minOffsetY = -300;
    private float maxOffsetY = 300;

    private BufferedImage titleImage;
    private int screenWidth, screenHeight;

    public interface MenuCallback {
        void onStartGame();

        void onShowMonsterList();

        void onExitGame();
    }

    private MenuCallback callback;

    public MainMenu(int screenWidth, int screenHeight, MenuCallback callback) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.callback = callback;

        mapSystem = new MapSystem();
        mapSystem.generateRandomMap();

        findLandCenter();

        setRandomTarget();
        currentOffsetX = targetOffsetX;
        currentOffsetY = targetOffsetY;
        bgOffsetX = currentOffsetX;
        bgOffsetY = currentOffsetY;

        loadTitleImage();
        loadButtonImages();
        loadVolumeIcon();
        calculateButtonPositions();
        initVolumeControl();
    }

    private void loadVolumeIcon() {
        volumeIconImage = loadImage("volume_icon.png");
        volumeIconHoverImage = loadImage("volume_icon_hover.png");
        if (volumeIconHoverImage == null)
            volumeIconHoverImage = volumeIconImage;
    }

    private void loadButtonImages() {
        startImage = loadImage("btn_start.png");
        startHoverImage = loadImage("btn_start_hover.png");
        if (startHoverImage == null)
            startHoverImage = startImage;

        monsterImage = loadImage("btn_monster.png");
        monsterHoverImage = loadImage("btn_monster_hover.png");
        if (monsterHoverImage == null)
            monsterHoverImage = monsterImage;

        exitImage = loadImage("btn_exit.png");
        exitHoverImage = loadImage("btn_exit_hover.png");
        if (exitHoverImage == null)
            exitHoverImage = exitImage;
    }

    private BufferedImage loadImage(String fileName) {
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
            System.err.println("【MainMenu】載入圖片失敗: " + fileName);
        }
        return null;
    }

    private void findLandCenter() {
        int worldWidth = mapSystem.WORLD_TILES_X * mapSystem.TILE_SIZE;
        int worldHeight = mapSystem.WORLD_TILES_Y * mapSystem.TILE_SIZE;
        centerX = worldWidth / 2;
        centerY = worldHeight / 2;
    }

    private void setRandomTarget() {
        targetOffsetX = minOffsetX + random.nextFloat() * (maxOffsetX - minOffsetX);
        targetOffsetY = minOffsetY + random.nextFloat() * (maxOffsetY - minOffsetY);

        float deltaX = targetOffsetX - currentOffsetX;
        float deltaY = targetOffsetY - currentOffsetY;
        float distance = (float) Math.hypot(deltaX, deltaY);
        if (distance > 0) {
            moveSpeed = distance / totalMoveTime;
        } else {
            moveSpeed = 0.05f;
        }
    }

    private void updateRandomMovement(long deltaTime) {
        if (deltaTime <= 0 || deltaTime > 100) {
            deltaTime = 16;
        }

        float deltaX = targetOffsetX - currentOffsetX;
        float deltaY = targetOffsetY - currentOffsetY;
        float distance = (float) Math.hypot(deltaX, deltaY);

        if (distance < 5) {
            setRandomTarget();
        } else {
            float step = moveSpeed * deltaTime;
            if (step > distance) {
                currentOffsetX = targetOffsetX;
                currentOffsetY = targetOffsetY;
            } else {
                float ratio = step / distance;
                currentOffsetX += deltaX * ratio;
                currentOffsetY += deltaY * ratio;
            }
        }

        bgOffsetX = currentOffsetX;
        bgOffsetY = currentOffsetY;
    }

    private void calculateButtonPositions() {
        int buttonWidth = 200;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        int totalWidth = buttonWidth * 3 + buttonSpacing * 2;
        int startX = (screenWidth - totalWidth) / 2;
        int buttonY = screenHeight / 2 + 50;

        startButton = new Rectangle(startX, buttonY, buttonWidth, buttonHeight);
        monsterButton = new Rectangle(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight);
        exitButton = new Rectangle(startX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, buttonHeight);
    }

    private void initVolumeControl() {
        int barWidth = 200;
        int barHeight = 8;
        int barX = 80; // 留給圖示空間
        int barY = screenHeight - 30;
        volumeBar = new Rectangle(barX, barY, barWidth, barHeight);

        int currentVolume = VolumeManager.getVolume();
        volumeKnob = new Rectangle(barX + (currentVolume * barWidth / 100) - 8, barY - 6, 16, 20);

        // 音量圖示區域
        volumeIconRect = new Rectangle(30, barY - 5, 40, 40);
        System.out.println("音量圖示位置: Y=" + (barY - 100)); // 🔥 加這行看輸出
    }

    private void loadTitleImage() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("title.png");
            if (url != null) {
                titleImage = ImageIO.read(url);
                System.out.println("【MainMenu】標題圖片載入成功");
            } else {
                File file = new File("res/title.png");
                if (file.exists()) {
                    titleImage = ImageIO.read(file);
                    System.out.println("【MainMenu】從檔案載入標題圖片成功");
                }
            }
        } catch (IOException e) {
            System.err.println("【MainMenu】載入標題圖片失敗");
        }
    }

    public void updateBackground(int screenWidth, int screenHeight, long deltaTime) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updateRandomMovement(deltaTime);
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        drawDarkenedBackground(g);

        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, 0, screenWidth, screenHeight);

        drawTitle(g);
        drawButtons(g);
        drawVolumeControl(g);
    }

    private void drawDarkenedBackground(Graphics2D g) {
        ui.Camera tempCamera = new ui.Camera(screenWidth, screenHeight);

        try {
            java.lang.reflect.Field xField = ui.Camera.class.getDeclaredField("x");
            java.lang.reflect.Field yField = ui.Camera.class.getDeclaredField("y");
            xField.setAccessible(true);
            yField.setAccessible(true);

            float camX = centerX - screenWidth / 2 + bgOffsetX;
            float camY = centerY - screenHeight / 2 + bgOffsetY;

            int worldWidth = mapSystem.WORLD_TILES_X * mapSystem.TILE_SIZE;
            int worldHeight = mapSystem.WORLD_TILES_Y * mapSystem.TILE_SIZE;
            camX = Math.max(0, Math.min(worldWidth - screenWidth, camX));
            camY = Math.max(0, Math.min(worldHeight - screenHeight, camY));

            xField.setFloat(tempCamera, camX);
            yField.setFloat(tempCamera, camY);
        } catch (Exception e) {
            System.err.println("【MainMenu】無法設定 Camera 位置: " + e.getMessage());
        }

        AffineTransform oldTransform = g.getTransform();
        mapSystem.drawMap(g, tempCamera, screenWidth, screenHeight);
        g.setTransform(oldTransform);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void drawTitle(Graphics2D g) {
        int titleCenterX = screenWidth / 2;
        int titleY = screenHeight / 4;

        if (titleImage != null) {
            int titleWidth = titleImage.getWidth();
            int titleHeight = titleImage.getHeight();

            titleWidth = 700;
            titleHeight = (int) (titleImage.getHeight() * (700.0 / titleImage.getWidth()));

            g.drawImage(titleImage, titleCenterX - titleWidth / 2, titleY, titleWidth, titleHeight, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.BOLD, 56));
            String title = "ECO SURVIVOR";
            FontMetrics fm = g.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g.drawString(title, titleCenterX - titleWidth / 2, titleY + 50);

            g.setFont(new Font("微軟正黑體", Font.PLAIN, 24));
            g.setColor(new Color(150, 255, 150));
            String subtitle = "Clean the Planet";
            int subWidth = fm.stringWidth(subtitle);
            g.drawString(subtitle, titleCenterX - subWidth / 2, titleY + 100);
        }
    }

    private void drawButtons(Graphics2D g) {
        drawImageButton(g, startButton, startImage, startHoverImage, startHover, "開始遊戲");
        drawImageButton(g, monsterButton, monsterImage, monsterHoverImage, monsterHover, "環境的危害");
        drawImageButton(g, exitButton, exitImage, exitHoverImage, exitHover, "離開遊戲");
    }

    private void drawImageButton(Graphics2D g, Rectangle rect, BufferedImage normalImg, BufferedImage hoverImg,
            boolean hover, String text) {
        if (rect == null)
            return;

        BufferedImage img = hover ? hoverImg : normalImg;

        if (img != null) {
            g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
        } else {
            g.setColor(hover ? new Color(100, 200, 100, 220) : new Color(30, 80, 30, 200));
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2 - 4;
        g.drawString(text, textX, textY);

        if (hover) {
            g.setColor(new Color(255, 255, 100));
            g.setStroke(new BasicStroke(4));
            g.drawRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 12, 12);
            g.setColor(new Color(255, 255, 100, 80));
            g.setStroke(new BasicStroke(6));
            g.drawRoundRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, 14, 14);
            g.setStroke(new BasicStroke(1));
        }
    }

    private void drawVolumeControl(Graphics2D g) {
        if (volumeBar == null)
            return;

        volumeBar.y = screenHeight - 30;
        volumeIconRect.y = volumeBar.y - 5;
        volumeKnob.y = volumeBar.y - 6;

        BufferedImage icon = volumeIconHover ? volumeIconHoverImage : volumeIconImage;
        if (icon != null) {
            int drawY = volumeIconRect.y - 10;
            g.drawImage(icon, volumeIconRect.x, drawY, volumeIconRect.width, volumeIconRect.height, null);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
            g.drawString("🔊", volumeIconRect.x, volumeIconRect.y + 24);
        }

        // 音量條背景
        g.setColor(new Color(60, 60, 60, 200));
        g.fillRect(volumeBar.x, volumeBar.y, volumeBar.width, volumeBar.height);

        // 音量條進度
        g.setColor(new Color(100, 200, 100));
        int currentVolume = VolumeManager.getVolume();
        int filledWidth = (currentVolume * volumeBar.width / 100);
        g.fillRect(volumeBar.x, volumeBar.y, filledWidth, volumeBar.height);

        // 音量條邊框
        g.setColor(Color.WHITE);
        g.drawRect(volumeBar.x, volumeBar.y, volumeBar.width, volumeBar.height);

        // 音量旋鈕
        int knobX = volumeBar.x + (currentVolume * volumeBar.width / 100) - 8;
        volumeKnob.setLocation(knobX, volumeBar.y - 6);
        g.setColor(new Color(100, 200, 100));
        g.fillRoundRect(volumeKnob.x, volumeKnob.y, 16, 20, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(volumeKnob.x, volumeKnob.y, 16, 20, 8, 8);

        // 音量百分比
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        g.drawString(currentVolume + "%", volumeBar.x + volumeBar.width + 10, volumeBar.y + 10);
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        startHover = startButton != null && startButton.contains(mouseX, mouseY);
        monsterHover = monsterButton != null && monsterButton.contains(mouseX, mouseY);
        exitHover = exitButton != null && exitButton.contains(mouseX, mouseY);
        volumeIconHover = volumeIconRect != null && volumeIconRect.contains(mouseX, mouseY);

        if (draggingVolume && volumeBar != null) {
            int newVolume = (mouseX - volumeBar.x) * 100 / volumeBar.width;
            newVolume = Math.max(0, Math.min(100, newVolume));
            VolumeManager.setVolume(newVolume);
        }
    }

    public void handleMousePress(int mouseX, int mouseY) {
        if (volumeBar != null && (volumeBar.contains(mouseX, mouseY) ||
                (volumeKnob != null && volumeKnob.contains(mouseX, mouseY)))) {
            draggingVolume = true;
            int newVolume = (mouseX - volumeBar.x) * 100 / volumeBar.width;
            newVolume = Math.max(0, Math.min(100, newVolume));
            VolumeManager.setVolume(newVolume);
            return;
        }

        if (startButton != null && startButton.contains(mouseX, mouseY) && callback != null) {
            callback.onStartGame();
        } else if (monsterButton != null && monsterButton.contains(mouseX, mouseY) && callback != null) {
            callback.onShowMonsterList();
        } else if (exitButton != null && exitButton.contains(mouseX, mouseY) && callback != null) {
            callback.onExitGame();
        }
    }

    public void handleMouseRelease(int mouseX, int mouseY) {
        draggingVolume = false;
    }

    public int getVolume() {
        return VolumeManager.getVolume();
    }
}