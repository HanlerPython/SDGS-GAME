package system;

import entity.Player;
import entity.Projectile;
import entity.Monster;
import entity.Trash;
import entity.Portal;
import java.util.List;

public class CollisionSystem {

    public void checkBulletMonsterCollision(ProjectileSystem projectileSystem, List<Monster> monsters,
            List<Trash> trashes) {
        List<Projectile> bullets = projectileSystem.getProjectile(true);
        outerloop: for (Projectile bullet : bullets) {
            for (Monster monster : monsters) {
                if (monster.isDead)
                    continue;
                if (Math.hypot(bullet.getX() - monster.getX(), bullet.getY() - monster.getY()) < 25) {
                    monster.takeDamage(bullet.getDamage());
                    bullet.setDestroyed(true);
                    if (monster.isDead) {
                        trashes.add(new Trash(monster.getX(), monster.getY(), 1));
                        // 🔥 擊殺音效
                        GameAudio.playSound("kill.wav");
                    }
                    continue outerloop;
                }
            }
        }
        projectileSystem.cleanupDeadProjectiles();
    }

    public void checkBulletPlayerCollision(ProjectileSystem projectileSystem, Player player) {
        List<Projectile> bullets = projectileSystem.getProjectile(false);
        for (Projectile bullet : bullets) {
            if (Math.hypot(bullet.getX() - player.getX(), bullet.getY() - player.getY()) < 25) {
                player.takeDamage(bullet.getDamage());
                bullet.setDestroyed(true);
                if (player.isDead())
                    break;
            }
        }
        projectileSystem.cleanupDeadProjectiles();
    }

    public void checkMonsterPlayerCollision(List<Monster> monsters, Player player) {
        for (Monster m : monsters) {
            if (m.isDead)
                continue;

            if (Math.hypot(player.getX() - m.getX(), player.getY() - m.getY()) < 30) {
                player.takeDamage(10);
            }
        }
    }

    public void checkPlayerTrashCollision(Player player, List<Trash> trashes, SDGStats sdgStats) {
        for (int i = 0; i < trashes.size(); i++) {
            Trash t = trashes.get(i);
            if (Math.hypot(player.getX() - t.getX(), player.getY() - t.getY()) < 30) {
                player.addExperience(t.getValue());
                sdgStats.addRecycled(t.getType());
                trashes.remove(i);
                i--;
            }
        }
    }

    public Portal checkPlayerPortalCollision(Player player, List<Portal> portals) {
        if (portals == null)
            return null;
        for (Portal p : portals) {
            if (Math.hypot(player.getX() - p.getX(), player.getY() - p.getY()) < 40) {
                return p;
            }
        }
        return null;
    }

}