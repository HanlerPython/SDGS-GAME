package system;

import entity.Player;
import entity.Projectile;
import java.util.List;

public class WeaponSystem {
    
    private long lastShootTime;
    private final long SHOOT_DELAY_MS = 100;
    
    public WeaponSystem() {
        this.lastShootTime = 0;
    }
    
    public void update(Player player, float targetX, float targetY, List<Projectile> bullets) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= SHOOT_DELAY_MS) {
            forceShoot(player, targetX, targetY, bullets);
            lastShootTime = currentTime;
        }
    }
    
    public void forceShoot(Player player, float targetX, float targetY, List<Projectile> bullets) {
        Projectile bullet = new Projectile(player.getX(), player.getY(), 
                                           targetX, targetY, player.getAttackDamage());
        bullets.add(bullet);
    }
    
    public void upgrade(Player player) {
        player.setAttackDamage(player.getAttackDamage() + 5);
    }
}