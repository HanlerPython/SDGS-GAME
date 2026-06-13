package entity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import system.MonsterData;
import system.ProjectileSystem;
import java.awt.Color;
import java.awt.Graphics2D;

public class Monster {

    private float x, y;
    private int health;
    private int maxHealth;
    private MonsterData.MonsterInfo template;
    public boolean isDead;
    private long lastSkillTime;

    // 技能狀態
    public int buffDurationFrames = 0;
    public float currentSpeed;
    public boolean isInvincible;
    public int skillStateTracker;

    // 水母圖片（Type 0）
    private static BufferedImage[] jellyfishFrames;

    // 史萊姆圖片（Type 1）
    private static BufferedImage[] slimeFrames;
    private static BufferedImage slimeDashImage;
    // 蚊子圖片(Type 2)
    private static BufferedImage mosquitoImage;
    // 菸鬼圖片（Type 3）
    private static BufferedImage[] smoKingFrames;
    // 塑膠團怪圖片（Type 4）
    private static BufferedImage trashballImage;

    // 通用備用圖片
    private static BufferedImage defaultImage;

    private static boolean resourcesLoaded = false;

    private static ProjectileSystem monsterProjectileSystem;

    public Monster() {
        this.isDead = true;
        if (!resourcesLoaded) {
            loadResources();
            resourcesLoaded = true;
        }
    }

    public void init(float startX, float startY, MonsterData.MonsterInfo info) {
        this.x = startX;
        this.y = startY;
        this.template = info;

        this.health = info.baseHealth;
        this.maxHealth = health;
        this.isDead = false;
        this.lastSkillTime = System.currentTimeMillis();
        this.skillStateTracker = 0;

        this.currentSpeed = template.baseSpeed;
        this.isInvincible = false;
        this.buffDurationFrames = 0;
    }

    public static void initMonsterProjectileSystem(ProjectileSystem system) {
        if (monsterProjectileSystem == null)
            monsterProjectileSystem = system;
    }

    public void update(float playerX, float playerY) {
        if (this.isDead)
            return;

        if (this.buffDurationFrames > 0) {
            this.buffDurationFrames--;

            if (this.buffDurationFrames == 0) {
                this.currentSpeed = this.template.baseSpeed;
                this.isInvincible = false;
            }
        }

        if (this.template.moveStrategy != null) {
            this.template.moveStrategy.move(this, playerX, playerY);
        }

        if (this.template.skillStrategy != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastSkillTime >= this.template.skillCooldown) {
                boolean success = this.template.skillStrategy.executeSkill(this, playerX, playerY);
                if (success) {
                    this.lastSkillTime = currentTime;
                }
            }
        }
    }

    public void compensateTime(long pausedDuration) {
        if (!this.isDead) {
            this.lastSkillTime += pausedDuration;
        }
    }

    public void takeDamage(int damage) {
        if (this.isDead)
            return;
        if (this.isInvincible)
            return;

        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            this.isDead = true;
        }
    }

    // ========== Getter / Setter ==========
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return template.baseHealth;
    }

    public int getAttackDamage() {
        return template.baseDamage;
    }

    public int getType() {
        return template.type;
    }

    public int getRewardExp() {
        return template.rewardExp;
    }

    public float getSpeed() {
        return this.currentSpeed;
    }

    public float getBaseSpeed() {
        if (this.template == null)
            return 2.0f;
        return this.template.baseSpeed;
    }

    public ProjectileSystem getProjectileSystem() {
        return monsterProjectileSystem;
    }

    // ========== 資源載入 ==========
    private void loadResources() {
        // 載入水母圖片（Type 0）
        jellyfishFrames = new BufferedImage[2];

        // 載入史萊姆圖片（Type 1）
        slimeFrames = new BufferedImage[2];

        // 載入菸鬼圖片（Type 3）
        smoKingFrames = new BufferedImage[2];

        try {
            // ===== 水母圖片 =====
            java.net.URL url1 = getClass().getClassLoader().getResource("jellyfish_1.png");
            if (url1 != null) {
                jellyfishFrames[0] = ImageIO.read(url1);
            } else {
                File file1 = new File("res/jellyfish_1.png");
                if (file1.exists()) {
                    jellyfishFrames[0] = ImageIO.read(file1);
                }
            }

            java.net.URL url2 = getClass().getClassLoader().getResource("jellyfish_2.png");
            if (url2 != null) {
                jellyfishFrames[1] = ImageIO.read(url2);
            } else {
                File file2 = new File("res/jellyfish_2.png");
                if (file2.exists()) {
                    jellyfishFrames[1] = ImageIO.read(file2);
                }
            }

            // ===== 史萊姆普通圖片 =====
            java.net.URL slimeUrl1 = getClass().getClassLoader().getResource("slime_1.png");
            if (slimeUrl1 != null) {
                slimeFrames[0] = ImageIO.read(slimeUrl1);
            } else {
                File slimeFile1 = new File("res/slime_1.png");
                if (slimeFile1.exists()) {
                    slimeFrames[0] = ImageIO.read(slimeFile1);
                }
            }

            java.net.URL slimeUrl2 = getClass().getClassLoader().getResource("slime_2.png");
            if (slimeUrl2 != null) {
                slimeFrames[1] = ImageIO.read(slimeUrl2);
            } else {
                File slimeFile2 = new File("res/slime_2.png");
                if (slimeFile2.exists()) {
                    slimeFrames[1] = ImageIO.read(slimeFile2);
                }
            }

            // ===== 史萊姆衝刺圖片 =====
            java.net.URL dashUrl = getClass().getClassLoader().getResource("slime_dash.png");
            if (dashUrl != null) {
                slimeDashImage = ImageIO.read(dashUrl);
                System.out.println("【Monster】slime_dash.png 載入成功");
            } else {
                File dashFile = new File("res/slime_dash.png");
                if (dashFile.exists()) {
                    slimeDashImage = ImageIO.read(dashFile);
                    System.out.println("【Monster】從檔案載入 slime_dash.png 成功");
                }
            }

            java.net.URL mosquitoUrl = getClass().getClassLoader().getResource("mosquito.png");
            if (mosquitoUrl != null) {
                mosquitoImage = ImageIO.read(mosquitoUrl);
                System.out.println("【Monster】mosquito.png 載入成功");
            } else {
                File mosquitoFile = new File("res/mosquito.png");
                if (mosquitoFile.exists()) {
                    mosquitoImage = ImageIO.read(mosquitoFile);
                    System.out.println("【Monster】從檔案載入 mosquito.png 成功");
                }
            }

            // ===== 塑膠團怪圖片 =====
            java.net.URL trashballUrl = getClass().getClassLoader().getResource("trashball.png");
            if (trashballUrl != null) {
                trashballImage = ImageIO.read(trashballUrl);
                System.out.println("【Monster】trashball.png 載入成功");
            } else {
                File trashballFile = new File("res/trashball.png");
                if (trashballFile.exists()) {
                    trashballImage = ImageIO.read(trashballFile);
                    System.out.println("【Monster】從檔案載入 trashball.png 成功");
                }
            }
            // ===== 菸鬼圖片1 =====
            java.net.URL smoKingUrl1 = getClass().getClassLoader().getResource("smoKing_1.png");
            if (smoKingUrl1 != null) {
                smoKingFrames[0] = ImageIO.read(smoKingUrl1);
            } else {
                File smoKingFile1 = new File("res/smoKing_1.png");
                if (smoKingFile1.exists()) {
                    smoKingFrames[0] = ImageIO.read(smoKingFile1);
                }
            }
            // ===== 菸鬼圖片1 =====
            java.net.URL smoKingUrl2 = getClass().getClassLoader().getResource("smoKing_2.png");
            if (smoKingUrl2 != null) {
                smoKingFrames[1] = ImageIO.read(smoKingUrl2);
            } else {
                File smoKingFile2 = new File("res/smoKing_2.png");
                if (smoKingFile2.exists()) {
                    smoKingFrames[1] = ImageIO.read(smoKingFile2);
                }
            }

            // ===== 通用備用圖片 =====
            java.net.URL defaultUrl = getClass().getClassLoader().getResource("monster_default.png");
            if (defaultUrl != null) {
                defaultImage = ImageIO.read(defaultUrl);
            } else {
                File defaultFile = new File("res/monster_default.png");
                if (defaultFile.exists()) {
                    defaultImage = ImageIO.read(defaultFile);
                }
            }

            System.out.println("【Monster】圖片載入完成 - 水母: " + (jellyfishFrames[0] != null) +
                    ", 史萊姆: " + (slimeFrames[0] != null));

        } catch (IOException e) {
            System.err.println("【Monster】載入圖片失敗");
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g, ui.Camera camera) {
        int screenX = (int) camera.worldToScreenX(x);
        int screenY = (int) camera.worldToScreenY(y);

        int size = 80;

        // 無敵狀態：半透明效果
        if (isInvincible) {
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        // 根據怪物類型選擇圖片
        BufferedImage currentImage = null;
        int monsterType = getType();

        // 檢查是否在衝刺狀態
        boolean isDashing = (buffDurationFrames > 0 && currentSpeed > getBaseSpeed() * 1.5f);

        if (monsterType == 0) {
            // 水母怪（Type 0）- 只有普通動畫
            if (jellyfishFrames[0] != null && jellyfishFrames[1] != null) {
                long time = System.currentTimeMillis();
                int frameIndex = (int) ((time / 500) % 2);
                currentImage = jellyfishFrames[frameIndex];
            }
        } else if (monsterType == 1) {
            // 史萊姆怪（Type 1）- 普通動畫 + 衝刺圖片
            if (isDashing && slimeDashImage != null) {
                currentImage = slimeDashImage;
            } else if (slimeFrames[0] != null && slimeFrames[1] != null) {
                long time = System.currentTimeMillis();
                int frameIndex = (int) ((time / 500) % 2);
                currentImage = slimeFrames[frameIndex];
            }
        } else if (monsterType == 2) {
            // 病媒蚊（Type 2）
            if (mosquitoImage != null) {
                currentImage = mosquitoImage;
                size = 60;
            }
        } else if (monsterType == 3) {
            // 菸鬼（Type 3）
            if (smoKingFrames[0] != null && smoKingFrames[1] != null) {
                long time = System.currentTimeMillis();
                int frameIndex = (int) ((time / 500) % 2);
                currentImage = smoKingFrames[frameIndex];
            }
        } else if (monsterType == 4) {
            // 塑膠團怪（Type 4）
            if (trashballImage != null) {
                currentImage = trashballImage;
                size = 120;
            }
        }

        // 如果沒有專屬圖片，嘗試使用備用圖片
        if (currentImage == null && defaultImage != null) {
            currentImage = defaultImage;
        }

        if (currentImage != null) {
            g.drawImage(currentImage, screenX - size / 2, screenY - size / 2, size, size, null);
        } else {
            // ===== 備用繪製（保留原有邏輯）=====
            g.setColor(isInvincible ? new Color(200, 200, 255, 150) : new Color(150, 50, 150));
            g.fillOval(screenX - 20, screenY - 20, 40, 40);
            g.setColor(Color.WHITE);

            if (isDashing) {
                g.drawString("⚡", screenX - 12, screenY + 8);
            } else {
                g.drawString("👾", screenX - 12, screenY + 8);
            }
        }

        // 還原透明度
        if (isInvincible) {
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
        }

        // 繪製血條
        drawHealthBar(g, screenX, screenY);
    }

    private void drawHealthBar(Graphics2D g, int screenX, int screenY) {
        int barWidth = 60;
        int barHeight = 6;
        int barX = screenX - barWidth / 2;
        int barY = screenY - 45;

        float healthPercent = (float) health / maxHealth;

        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);

        g.setColor(new Color(220, 50, 50));
        g.fillRect(barX, barY, (int) (barWidth * healthPercent), barHeight);
    }
}