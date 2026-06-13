package entity.Weapon;

import entity.Player;
import entity.Monster;
import entity.Trash;
import system.WeaponSystem;
import java.util.List;
import system.ProjectileSystem;

public abstract class Weapon {
    protected int damage;
    protected long cooldown;
    protected long lastAttacktTime;
    protected int level;
    protected WeaponSystem.WeaponType type;

    public Weapon(int damage, long cooldown, WeaponSystem.WeaponType type) {
        this.damage = damage;
        this.cooldown = cooldown;
        this.lastAttacktTime = 0;
        this.level = 1;
        this.type = type;
    }

    // 子類別實作各自的攻擊方式
    public abstract void update(Player player, float targetX, float targetY,
            ProjectileSystem projectileSystem, List<Monster> monsters, List<Trash> trashes);

    // 共同的冷卻時間檢查
    protected boolean canAttack() {
        long currentTime = System.nanoTime();
        if (currentTime - this.lastAttacktTime >= this.cooldown) {
            this.lastAttacktTime = currentTime;
            return true;
        }
        return false;
    }

    public void upgrade() {
        this.level++;
        applyLevelUpEffects();
    }

    // 子類別實作各自的升級效果
    protected abstract void applyLevelUpEffects();

    public WeaponSystem.WeaponType getType() {
        return this.type;
    }

    public boolean isMaxLevel() {
        return this.level >= 5;
    }

    // 取得下一級的文字描述，讓子類別自己實作
    public abstract String getNextLevelDescription();
}