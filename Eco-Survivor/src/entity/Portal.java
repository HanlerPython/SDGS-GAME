package entity;

import ui.Camera;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Portal {
    private float x, y;
    private boolean active = true;
    private String gameType;
    
    // 💡 傳送門圖片（靜態，所有傳送門共用）
    private static BufferedImage trashPortalImage;
    private static BufferedImage powerPortalImage;
    private static BufferedImage faucetPortalImage;
    
    // 💡 圖片尺寸
    private final int PORTAL_SIZE = 64;

    public Portal(float x, float y, String gameType){
        this.x = x;
        this.y = y;
        this.gameType = gameType;
        
        // 第一次建立時載入圖片
        if (trashPortalImage == null && powerPortalImage == null && faucetPortalImage == null) {
            loadImages();
        }
    }
    
    private void loadImages() {
        try {
            // 垃圾分類傳送門圖片
            java.net.URL trashUrl = getClass().getClassLoader().getResource("portal_trash.png");
            if (trashUrl != null) {
                trashPortalImage = ImageIO.read(trashUrl);
                System.out.println("【Portal】portal_trash.png 載入成功");
            } else {
                File trashFile = new File("res/portal_trash.png");
                if (trashFile.exists()) {
                    trashPortalImage = ImageIO.read(trashFile);
                    System.out.println("【Portal】從檔案載入 portal_trash.png 成功");
                }
            }
            
            // 電纜連接傳送門圖片
            java.net.URL powerUrl = getClass().getClassLoader().getResource("portal_power.png");
            if (powerUrl != null) {
                powerPortalImage = ImageIO.read(powerUrl);
                System.out.println("【Portal】portal_power.png 載入成功");
            } else {
                File powerFile = new File("res/portal_power.png");
                if (powerFile.exists()) {
                    powerPortalImage = ImageIO.read(powerFile);
                    System.out.println("【Portal】從檔案載入 portal_power.png 成功");
                }
            }
            
            // 水龍頭維修傳送門圖片
            java.net.URL faucetUrl = getClass().getClassLoader().getResource("portal_faucet.png");
            if (faucetUrl != null) {
                faucetPortalImage = ImageIO.read(faucetUrl);
                System.out.println("【Portal】portal_faucet.png 載入成功");
            } else {
                File faucetFile = new File("res/portal_faucet.png");
                if (faucetFile.exists()) {
                    faucetPortalImage = ImageIO.read(faucetFile);
                    System.out.println("【Portal】從檔案載入 portal_faucet.png 成功");
                }
            }
        } catch (IOException e) {
            System.err.println("【Portal】載入圖片失敗");
        }
    }

    public float getX(){return x;}
    public float getY(){return y;}
    public boolean isActive(){return active;}
    public String getGameType(){return gameType;}
    public void close(){this.active = false;}
    
    public void draw(Graphics2D g2, Camera camera) {
        // 將世界座標轉換為螢幕座標
        float screenX = camera.worldToScreenX(this.x);
        float screenY = camera.worldToScreenY(this.y);

        // 優先使用圖片繪製
        BufferedImage portalImage = null;
        if ("trashsortgame".equals(this.gameType)) {
            portalImage = trashPortalImage;
        } else if ("powercablegame".equals(this.gameType)) {
            portalImage = powerPortalImage;
        } else if ("faucetgame".equals(this.gameType)) {
            portalImage = faucetPortalImage;
        }
        
        if (portalImage != null) {
            // 使用圖片繪製（置中）
            int drawX = (int)screenX - PORTAL_SIZE / 2;
            int drawY = (int)screenY - PORTAL_SIZE / 2;
            g2.drawImage(portalImage, drawX, drawY, PORTAL_SIZE, PORTAL_SIZE, null);
        } else {
            // ===== 備用繪製（圖片不存在時使用）=====
            if ("trashsortgame".equals(this.gameType)) {
                g2.setColor(new Color(0, 191, 255, 180));
                g2.fillOval((int)screenX - 20, (int)screenY - 20, 40, 40);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("TRASH", (int)screenX - 20, (int)screenY + 5);
                
            } else if ("powercablegame".equals(this.gameType)) {
                g2.setColor(new Color(50, 205, 50, 180));
                g2.fillOval((int)screenX - 20, (int)screenY - 20, 40, 40);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11)); 
                g2.drawString("POWER", (int)screenX - 20, (int)screenY + 5);
            } else if ("faucetgame".equals(this.gameType)) {
                g2.setColor(new Color(255, 140, 0, 180));
                g2.fillOval((int)screenX - 20, (int)screenY - 20, 40, 40);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11)); 
                g2.drawString("WATER", (int)screenX - 20, (int)screenY + 5);
            }
        }
    }
}