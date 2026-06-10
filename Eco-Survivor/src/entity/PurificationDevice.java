package entity;

import ui.Camera;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class PurificationDevice {
    
    private float x, y;
    private boolean active = true;
    private float radius = 400;
    
    private static BufferedImage deviceImage;
    
    public PurificationDevice(float x, float y) {
        this.x = x;
        this.y = y;
        loadImage();
    }
    
    private void loadImage() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("purification_device.png");
            if (url != null) {
                deviceImage = ImageIO.read(url);
            } else {
                File file = new File("res/purification_device.png");
                if (file.exists()) {
                    deviceImage = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.err.println("【PurificationDevice】載入圖片失敗");
        }
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    
    public void draw(Graphics2D g, Camera camera) {
        if (!active) return;
        
        float screenX = camera.worldToScreenX(x);
        float screenY = camera.worldToScreenY(y);
        
        // 繪製裝置圖片
        if (deviceImage != null) {
            int size = 80;
            g.drawImage(deviceImage, (int)screenX - size/2, (int)screenY - size/2, size, size, null);
        } else {
            g.setColor(new Color(0, 200, 255));
            g.fillOval((int)screenX - 25, (int)screenY - 25, 50, 50);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("⚡", (int)screenX - 8, (int)screenY + 8);
        }
        
        // 🔥 繪製範圍圈（半透明，更容易看見）
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 64, 0, 200));
        g.drawOval((int)screenX - (int)radius, (int)screenY - (int)radius, (int)radius * 2, (int)radius * 2);
        
        g.setColor(new Color(0, 255, 0, 100));
        g.fillOval((int)screenX - (int)radius, (int)screenY - (int)radius, (int)radius * 2, (int)radius * 2);
        
        g.setStroke(new BasicStroke(1));
    }
    
    public boolean isPlayerInRange(float playerX, float playerY) {
        double dist = Math.hypot(playerX - x, playerY - y);
        return dist < radius;
    }
}