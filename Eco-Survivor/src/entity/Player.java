package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player {
    
    private float x, y;
    private int health, maxHealth;
    private int level;
    private int experience;
    private int expToNext;
    private int attackDamage;
    private boolean isInvincible = false;
    private long invincibleEndTime = 0;
    private float moveSpeed;
    
    private Runnable onLevelUpCallback;  // ← 用 callback 代替直接依賴
    
    public final int WIDTH = 64;
    public final int HEIGHT = 64;
    private BufferedImage playerImage;
    
    public Player(float startX, float startY) {
        this(startX, startY, null);
    }
    
    public Player(float startX, float startY, Runnable onLevelUpCallback) {
        this.x = startX;
        this.y = startY;
        this.health = 100;
        this.maxHealth = 100;
        this.level = 1;
        this.experience = 0;
        this.expToNext = 50;
        this.attackDamage = 10;
        this.moveSpeed = 5.0f;
        this.onLevelUpCallback = onLevelUpCallback;
        
        loadResources();
    }
    
    private void loadResources() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("player.png");
            if (url != null) {
                System.out.println("找到 player.png URL: " + url);
                
                // 檢查檔案是否存在
                java.io.File file = new java.io.File(url.toURI());
                System.out.println("檔案路徑: " + file.getAbsolutePath());
                System.out.println("檔案是否存在: " + file.exists());
                System.out.println("檔案大小: " + file.length() + " bytes");
                
                // 嘗試讀取圖片
                playerImage = ImageIO.read(url);
                if (playerImage == null) {
                    System.err.println("【Player】ImageIO.read 回傳 null！圖片格式可能有問題");
                    // 嘗試用檔案方式讀取
                    playerImage = ImageIO.read(file);
                    if (playerImage == null) {
                        System.err.println("【Player】檔案方式也讀取失敗");
                    }
                } else {
                    System.out.println("【Player】圖片載入成功！寬度: " + playerImage.getWidth() + ", 高度: " + playerImage.getHeight());
                }
                return;
            } else {
                System.err.println("【Player】找不到 player.png 資源");
            }
        } catch (Exception e) {
            System.err.println("【Player】載入圖片時發生異常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 如果都失敗，使用備用繪製
        System.out.println("【Player】使用預設綠色方塊");
    }

    public void draw(Graphics2D g, ui.Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);

        int drawX = screenX - WIDTH / 2;
        int drawY = screenY - HEIGHT / 2;

        // 保存原本的透明度繪圖狀態 (Composite)
        Composite originalComposite = g.getComposite();

        // 利用時間差製造閃爍效果
        if (isInvincible) {
            // 每 150 毫秒切換一次
            if ((System.currentTimeMillis() / 150) % 2 == 0) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // 30% 透明度
            }
        }

        // 開始繪製玩家
        if (playerImage != null) {
            // 讓圖片中心對齊玩家座標
            g.drawImage(playerImage, drawX, drawY, WIDTH, HEIGHT, null);
        }

        //把透明度還原
        g.setComposite(originalComposite);
    }
    
    public void updateMovement(boolean up, boolean down, boolean left, boolean right) {
        float dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;
        
        if (dx != 0 || dy != 0) {
            double len = Math.hypot(dx, dy);
            dx /= len;
            dy /= len;
        }
        
        x += dx * moveSpeed;
        y += dy * moveSpeed;

        if (isInvincible && System.currentTimeMillis() > invincibleEndTime) {
            isInvincible = false;
    }
    }
    
    public void setMaxHealth(int maxHealth, boolean fullHP) {
        this.maxHealth = maxHealth;
        if(fullHP) {
        	this.health = this.maxHealth;
        }
    }
    
    public int getExpToNext() {
        return this.expToNext;
    }
    
    public boolean isDead() {
    	return this.health <= 0;
    }
    
    private long lastHitTime = 0;
    
    public void triggerInvincibility(int durationMs) {
        this.isInvincible = true;
        
        this.invincibleEndTime = System.currentTimeMillis() + durationMs; 
    }

    public void takeDamage(int damage) {
        long currentTime = System.nanoTime();
        if (isInvincible) {
            return; 
        }
        else if (currentTime - lastHitTime >= 100_000_000) {
            this.health = Math.max(0, this.health - damage);
            this.lastHitTime = currentTime;
        }
    }
    

    public void addExperience(int exp) {
        this.experience += exp;
        while (this.experience >= expToNext) {
            levelUp();
        }
    }
    
    private void levelUp() {
        this.experience -= expToNext;
        this.level++;
        this.expToNext = this.level * 50;
        this.attackDamage += 5;
        this.health = Math.min(maxHealth, this.health + 10);
        
        if (onLevelUpCallback != null) {
            onLevelUpCallback.run();
        }
    }
    
    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.health = maxHealth;
        this.level = 1;
        this.experience = 0;
        this.expToNext = 50;
        this.attackDamage = 10;
        this.moveSpeed = 5.0f;
    }
    
    public void heal(int amount) {
        this.health = Math.min(maxHealth, this.health + amount);
        System.out.println("【Player】回血 " + amount + "，目前血量: " + health + "/" + maxHealth);
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getAttackDamage() { return attackDamage; }
    public float getMoveSpeed() { return moveSpeed; }
    
    public void setAttackDamage(int damage) { this.attackDamage = damage; }
    public void setMoveSpeed(float speed) { this.moveSpeed = speed; }
}