package entity.Weapon;

import entity.Player;
import entity.Monster;
import entity.Trash;
import system.ProjectileSystem;
import system.WeaponSystem;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class UVLaser extends Weapon {

    private float length;
    
    // 雷射圖片
    private static BufferedImage laserImage;
    
    // 射擊特效相關
    private boolean isShooting = false;
    private long shootStartTime = 0;
    private float shootEndX = 0, shootEndY = 0;
    private static final long SHOOT_VISUAL_DURATION = 150_000_000;  // 0.15 秒顯示

    public UVLaser() {
        super(15, 2_000_000_000L, WeaponSystem.WeaponType.UVLASER);
        this.length = 400;
        
        if (laserImage == null) {
            loadImage();
        }
    }
    
    private void loadImage() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("laser.png");
            if (url != null) {
                laserImage = ImageIO.read(url);
                System.out.println("【UVLaser】laser.png 載入成功");
                return;
            }
            
            File file = new File("res/laser.png");
            if (file.exists()) {
                laserImage = ImageIO.read(file);
                System.out.println("【UVLaser】從檔案載入 laser.png 成功");
                return;
            }
            
            System.err.println("【UVLaser】找不到 laser.png");
        } catch (IOException e) {
            System.err.println("【UVLaser】載入圖片失敗");
        }
    }

    @Override
    public void update(Player player, float targetX, float targetY,
            ProjectileSystem projectileSystem, List<Monster> monsters, List<Trash> trashes) {
        
        // 更新射擊特效狀態
        if (isShooting) {
            long currentTime = System.nanoTime();
            if (currentTime - shootStartTime >= SHOOT_VISUAL_DURATION) {
                isShooting = false;
            }
        }
        
        if (canAttack()) {
            // 記錄射擊瞬間
            isShooting = true;
            shootStartTime = System.nanoTime();
            
            float dx = targetX - player.getX();
            float dy = targetY - player.getY();
            double dist = Math.hypot(dx, dy);

            if (dist == 0) return;

            float dirX = (float) (dx / dist);
            float dirY = (float) (dy / dist);

            float endX = player.getX() + dirX * this.length;
            float endY = player.getY() + dirY * this.length;
            
            shootEndX = endX;
            shootEndY = endY;

            int totalDamage = this.damage + player.getAttackDamage();

            for (Monster m : monsters) {
                if (m.isDead) continue;

                float px = m.getX();
                float py = m.getY();

                float abX = endX - player.getX();
                float abY = endY - player.getY();
                float apX = px - player.getX();
                float apY = py - player.getY();

                float ab_ab = abX * abX + abY * abY;
                float ap_ab = apX * abX + apY * abY;

                float t = Math.max(0, Math.min(1, ap_ab / ab_ab));

                float cx = player.getX() + t * abX;
                float cy = player.getY() + t * abY;

                float distSq = (px - cx) * (px - cx) + (py - cy) * (py - cy);

                if (distSq <= 900) {
                    m.takeDamage(totalDamage);
                    if (m.isDead) {
                        trashes.add(new Trash(m.getX(), m.getY(), 1));
                    }
                }
            }
        }
    }
    
    /**
     * 繪製雷射特效
     */
    public void draw(Graphics2D g, ui.Camera camera, Player player) {
        if (!isShooting) return;
        if (laserImage == null) return;
        
        float startX = camera.worldToScreenX(player.getX());
        float startY = camera.worldToScreenY(player.getY());
        float endX = camera.worldToScreenX(shootEndX);
        float endY = camera.worldToScreenY(shootEndY);
        
        float dx = endX - startX;
        float dy = endY - startY;
        float distance = (float)Math.hypot(dx, dy);
        if (distance < 1) return;
        
        float angle = (float)Math.atan2(dy, dx);
        
        java.awt.geom.AffineTransform old = g.getTransform();
        g.translate(startX, startY);
        g.rotate(angle);
        
        // 雷射粗度 = 攻擊力（100 時粗度 100）
        int laserThickness = 7*(this.damage + player.getAttackDamage());
        if (laserThickness > 150) laserThickness = 150;
        
        g.drawImage(laserImage, 0, -laserThickness/2, (int)distance, laserThickness, null);
        g.setTransform(old);
    }

    @Override
    protected void applyLevelUpEffects() {
        switch (this.level) {
            case 2:
                this.damage += 10;
                break;
            case 3:
                this.cooldown /= 2;
                break;
            case 4:
                this.damage += 10;
                break;
            case 5:
                this.length += 200;
                break;
        }
    }

    @Override
    public String getNextLevelDescription() {
        switch (this.level) {
            case 1:
                return "UV Laser Lv2: Damage +10";
            case 2:
                return "UV Laser Lv3: Cooldown -50%";
            case 3:
                return "UV Laser Lv4: Damage +10";
            case 4:
                return "UV Laser Lv5: Range +200";
            default:
                return "UV Laser MAX";
        }
    }
}