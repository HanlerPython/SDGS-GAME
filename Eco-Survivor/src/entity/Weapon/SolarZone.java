package entity.Weapon;

import entity.Player;
import entity.Projectile;
import entity.Monster;
import entity.Trash;
import system.WeaponSystem;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import system.ProjectileSystem;

public class SolarZone extends Weapon {

    private float radius;

    private static BufferedImage sunRingImage;
    private float currentAngle = 0;
    
    public SolarZone() {
        // 傷害: 10, 冷卻: 1秒 (500,000,000奈秒)
        super(10, 500_000_000L, WeaponSystem.WeaponType.SOLARZONE);
        this.radius = 300;
        
        if (sunRingImage == null) {
            loadImage();
        }
    }

    private void loadImage() {
        try {
            // 從 classpath 載入
            java.net.URL url = getClass().getClassLoader().getResource("sunring.png");
            if (url != null) {
                sunRingImage = ImageIO.read(url);
                System.out.println("【SolarZone】sunring.png 載入成功");
                return;
            }
            
            // 從檔案載入
            File file = new File("res/sunring.png");
            if (file.exists()) {
                sunRingImage = ImageIO.read(file);
                System.out.println("【SolarZone】從檔案載入 sunring.png 成功");
                return;
            }
            
            System.err.println("【SolarZone】找不到 sunring.png");
        } catch (IOException e) {
            System.err.println("【SolarZone】載入圖片失敗");
        }
    }
    
    @Override
    public void update(Player player, float targetX, float targetY,
            ProjectileSystem projectileSystem, List<Monster> monsters, List<Trash> trashes) {
    	
    	// 每幀都旋轉（與攻擊冷卻分離）
        currentAngle += 0.05f;  // 調整這個值控制速度
        if (currentAngle >= Math.PI * 2) {
            currentAngle -= Math.PI * 2;  // 保持角度在 0~2π 之間
        }
        
        if (!isActive)
            return;

        if (canAttack()) {
            float radiusSq = this.radius * this.radius;

            for (Monster m : monsters) {
                if (m.isDead)
                    continue;

                float dx = player.getX() - m.getX();
                float dy = player.getY() - m.getY();

                // 距離平方小於半徑平方即判定命中
                if (dx * dx + dy * dy <= radiusSq) {
                    m.takeDamage(this.damage);
                    if (m.isDead) {
                        trashes.add(new Trash(m.getX(), m.getY(), 1));
                    }
                }
            }
        }
    }

    public void draw(Graphics2D g, ui.Camera camera, Player player) {
        if (sunRingImage == null) return;
        
        // 計算玩家在螢幕上的位置
        float playerScreenX = camera.worldToScreenX(player.getX());
        float playerScreenY = camera.worldToScreenY(player.getY());
        
        // 計算繪製大小（半徑 * 2）
        int drawSize = (int)(this.radius * 2);
        
        // 儲存原始變換
        java.awt.geom.AffineTransform oldTransform = g.getTransform();
        
        // 旋轉並繪製
        g.rotate(currentAngle, playerScreenX, playerScreenY);
        g.drawImage(sunRingImage, 
                    (int)(playerScreenX - this.radius), 
                    (int)(playerScreenY - this.radius), 
                    drawSize, drawSize, null);
        
        // 還原變換
        g.setTransform(oldTransform);
    }
    
    public float getRadius() {
        return radius;
    }
    
    @Override
    protected void applyLevelUpEffects() {
        switch (this.level) {
            case 2:
                this.damage += 10;
                break;
            case 3:
                this.radius *= 1.2f;
                break;
            case 4:
                this.damage += 15;
                break;
            case 5:
                this.radius *= 1.2f;
                break;
        }
    }

    @Override
    public String getNextLevelDescription() {
        switch (this.level) {
            case 1:
                return "Solar Zone Lv2: Damage +5";
            case 2:
                return "Solar Zone Lv3: Range +20%";
            case 3:
                return "Solar Zone Lv4: Damage +10";
            case 4:
                return "Solar Zone Lv5: Range +20%";
            default:
                return "Solar Zone MAX";
        }
    }
}