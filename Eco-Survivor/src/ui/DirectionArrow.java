package ui;

import entity.Player;
import entity.PurificationDevice;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class DirectionArrow {
    
    private static BufferedImage arrowUp;
    private static BufferedImage arrowDown;
    private static BufferedImage arrowLeft;
    private static BufferedImage arrowRight;
    private static boolean imagesLoaded = false;
    
    public DirectionArrow() {
        if (!imagesLoaded) {
            loadImages();
            imagesLoaded = true;
        }
    }
    
    private void loadImages() {
        arrowUp = loadImage("arrow_up.png");
        arrowDown = loadImage("arrow_down.png");
        arrowLeft = loadImage("arrow_left.png");
        arrowRight = loadImage("arrow_right.png");
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
            System.err.println("【DirectionArrow】載入圖片失敗: " + fileName);
        }
        return null;
    }
    
    public void draw(Graphics2D g, int screenWidth, int screenHeight, Player player, PurificationDevice device) {
        if (device == null || !device.isActive()) return;
        
        float dx = device.getX() - player.getX();
        float dy = device.getY() - player.getY();
        double distance = Math.hypot(dx, dy);
        
        if (distance < device.getRadius()) return;
        
        BufferedImage arrow = null;
        int arrowSize = 64;
        int arrowX = 0;
        int arrowY = 0;
        int textX = 0;
        int textY = 0;
        
        // 判斷箭頭方向，箭頭和文字分開設定位置
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平方向（左右）
            if (dx > 0) {
                // 箭頭靠右
                arrow = arrowRight;
                arrowX = screenWidth - arrowSize - 60;
                arrowY = screenHeight / 2 - arrowSize / 2;
                // 文字放在箭頭左側
                textX = arrowX - 80;
                textY = arrowY + arrowSize / 2 + 8;
            } else {
                // 箭頭靠左
                arrow = arrowLeft;
                arrowX = 60;
                arrowY = screenHeight / 2 - arrowSize / 2;
                // 文字放在箭頭右側
                textX = arrowX + arrowSize + 20;
                textY = arrowY + arrowSize / 2 + 8;
            }
        } else {
            // 垂直方向（上下）
            if (dy > 0) {
                // 箭頭靠下
                arrow = arrowDown;
                arrowX = screenWidth / 2 - arrowSize / 2;
                arrowY = screenHeight - arrowSize - 30;
                // 文字放在箭頭上方
                textX = screenWidth / 2;
                textY = arrowY - 15;
            } else {
                // 箭頭靠上
                arrow = arrowUp;
                arrowX = screenWidth / 2 - arrowSize / 2;
                arrowY = 30;
                // 文字放在箭頭下方
                textX = screenWidth / 2;
                textY = arrowY + arrowSize + 25;
            }
        }
        
        // 繪製箭頭
        if (arrow != null) {
            g.drawImage(arrow, arrowX, arrowY, arrowSize, arrowSize, null);
        }
        
        // 繪製距離文字（與箭頭分開）
        g.setColor(Color.YELLOW);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 24));
        String distText = (int)distance + "m";
        FontMetrics fm = g.getFontMetrics();
        
        // 水平文字需要置中
        if (arrow == arrowUp || arrow == arrowDown) {
            textX = screenWidth / 2 - fm.stringWidth(distText) / 2;
        }
        
        g.drawString(distText, textX, textY);
    }
}