package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Projectile {

    private float x, y;
    private float vx, vy;
    private int damage;
    private boolean active;
    private boolean isPlayer;

    public final int WIDTH = 32;
    public final int HEIGHT = 32;

    private static BufferedImage bulletImage;
    private static BufferedImage enemyBulletImage;

    public Projectile() {
        if (bulletImage == null || enemyBulletImage == null) {
            loadResources();
        }
    }

    public void init(float x, float y, float targetX, float targetY, int damage, boolean isPlayer) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.active = true;
        this.isPlayer = isPlayer;

        float dx = targetX - x;
        float dy = targetY - y;
        double len = Math.hypot(dx, dy);
        if (len > 0) {
            if (isPlayer) {
                this.vx = (float) (dx / len * 12);
                this.vy = (float) (dy / len * 12);
            } else {
                this.vx = (float) (dx / len * 3);
                this.vy = (float) (dy / len * 3);
            }
        } else {
            this.vx = 0;
            this.vy = 0;
        }
    }

    private void loadResources() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("bullet.png");
            if (url != null) {
                bulletImage = ImageIO.read(url);
                System.out.println("【Projectile】載入 bullet.png 成功");
            }
            url = getClass().getClassLoader().getResource("cigarette.png");
            if (url != null) {
                enemyBulletImage = ImageIO.read(url);
                System.out.println("【Projectile】載入 cigarette.png 成功");
                return;
            }
        } catch (IOException e) {
            System.err.println("【Projectile】載入圖片失敗");
        }
    }

    public void draw(Graphics2D g, ui.Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);

        if (isPlayer) {
            if (bulletImage != null) {
                g.drawImage(bulletImage, screenX - WIDTH / 2, screenY - HEIGHT / 2, WIDTH, HEIGHT, null);
            } else {
                g.setColor(Color.CYAN);
                g.fillOval(screenX - 5, screenY - 5, 10, 10);
            }
        } else {
            if (enemyBulletImage != null) {
                g.drawImage(enemyBulletImage, screenX - WIDTH / 2, screenY - HEIGHT / 2, WIDTH, HEIGHT, null);
            } else {
                g.setColor(Color.CYAN);
                g.fillOval(screenX - 5, screenY - 5, 10, 10);
            }
        }
    }

    public void update() {
        x += vx;
        y += vy;
    }

    public void setDestroyed(boolean isDestroyed) {
        this.active = !isDestroyed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isDestroyed() {
        return !this.active;
    }
}