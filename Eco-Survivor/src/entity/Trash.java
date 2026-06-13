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
    
    public final int WIDTH = 80;  
    public final int HEIGHT = 80;
    
    private static BufferedImage energyImage;
    
    public Trash(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = 10;
        
        if (energyImage == null) {
            loadResources();
        }
    }
    
    private void loadResources() {
        try {
        	java.net.URL url = getClass().getClassLoader().getResource("energy.png");
            if (url != null) {
                energyImage = ImageIO.read(url);
                System.out.println("【Trash】成功透過 ClassLoader 載入 energy.png");
                return;
            }

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

    public void draw(Graphics2D g, ui.Camera camera) {
        // 世界座標轉螢幕顯示座標
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);

        if (energyImage != null) {
            g.drawImage(energyImage, screenX - WIDTH / 2, screenY - HEIGHT / 2, WIDTH, HEIGHT, null);
        } else {
            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(screenX - 8, screenY + 4, 16, 6);
        }
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getType() { return type; }
    public int getValue() { return value; }
}