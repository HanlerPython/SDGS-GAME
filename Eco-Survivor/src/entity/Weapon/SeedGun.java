package entity.Weapon;

import entity.Player;
import entity.Monster;
import entity.Trash;
import system.ProjectileSystem;
import system.WeaponSystem;
import java.util.List;

public class SeedGun extends Weapon {
    public SeedGun() {
        super(10, 500_000_000, WeaponSystem.WeaponType.SEEDGUN);
    }

    @Override
    public void update(Player player, float targetX, float targetY,
            ProjectileSystem projectileSystem, List<Monster> monsters, List<Trash> trashes) {
        if (canAttack()) {
            projectileSystem.spawnProjectile(player.getX(), player.getY(), targetX, targetY,
                    this.damage + player.getAttackDamage(), true);
        }
    }

    @Override
    public void applyLevelUpEffects() {
        switch (this.level) {
            case 2:
                this.damage += 20;
                break;
            case 3:
                this.cooldown /= 1.5;
                break;
            case 4:
                this.damage += 20;
                break;
            case 5:
                this.cooldown /= 1.5;
                break;
        }
    }

    @Override
    public String getNextLevelDescription() {
        switch (this.level) {
            case 1:
                return "Seed Gun Lv2: Damage +20";
            case 2:
                return "Seed Gun Lv3: Cooldown -33%";
            case 3:
                return "Seed Gun Lv4: Damage +20";
            case 4:
                return "Seed Gun Lv5: Cooldown -33%";
            default:
                return "Seed Gun MAX";
        }
    }
}