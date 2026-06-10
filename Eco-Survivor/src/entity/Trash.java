package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Trash {
    
    private float x, y;
    private int type;
    private int value;
    
    // 💡 妳（美術與D負責人）新增的顯示寬高常數（單位：像素）
    // 依據原本舊代碼的半徑 10 (寬高20)，這裡設 20 到 24 之間在畫面上最合適
    public final int WIDTH = 80;  
    public final int HEIGHT = 80;
    
    // 💡 新增：靜態圖片變數，讓畫面上幾百個能量水晶共用同一張圖，節省效能與記憶體
    private static BufferedImage energyImage;
    
    public Trash(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = 10;
        
        // 💡 建立第一個掉落物時，自動安全載入美術圖片資源
        if (energyImage == null) {
            loadResources();
        }
    }
    
    /**
     * 💡 新增：安全巡檢並載入能量圖片
     */
    private void loadResources() {
        try {
            // 第一重搜查：ClassLoader
        	java.net.URL url = getClass().getClassLoader().getResource("energy.png");
            if (url != null) {
                energyImage = ImageIO.read(url);
                System.out.println("【Trash】成功透過 ClassLoader 載入 energy.png");
                return;
            }

            // 第二重搜查：src/res/ (本機測試)
            File fileSrc = new File("src/res/energy.png");
            if (fileSrc.exists()) {
                energyImage = ImageIO.read(fileSrc);
                System.out.println("【Trash】成功透過 src/res/ 載入 energy.png");
                return;
            }

            System.err.println("【Trash 嚴重警告】找不到能量圖片，請確認 res/energy.png 是否存在。");
        } catch (IOException e) {
            System.err.println("【Trash 錯誤】讀取能量圖片時發生異常！");
            e.printStackTrace();
        }
    }

    /**
     * 💡 新增：能量掉落物的繪製邏輯（徹底拔除黃色圓圈）
     */
    public void draw(Graphics2D g, ui.Camera camera) {
        // 世界座標轉螢幕顯示座標
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);

        if (energyImage != null) {
            // 1. 圖片載入成功：渲染精美的 energy.png，減去寬高的一半能讓圖片中心精確對準判定點
            g.drawImage(energyImage, screenX - WIDTH / 2, screenY - HEIGHT / 2, WIDTH, HEIGHT, null);
        } else {
            // 2. 淡淡的陰影防呆：若圖片還沒準備好，不畫黃圈圈，改畫黑色小影子，不破壞遊戲畫面
            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(screenX - 8, screenY + 4, 16, 6);
        }
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getType() { return type; }
    public int getValue() { return value; }
}