package system;

import entity.Player;
import entity.Projectile;
import entity.Monster;
import entity.Trash;
import java.util.List;

public class CollisionSystem {
    
    public void checkBulletMonsterCollision(List<Projectile> bullets, List<Monster> monsters,
                                             List<Trash> trashes, SDGStats sdgStats) {
        for (int i = 0; i < bullets.size(); i++) {
            Projectile b = bullets.get(i);
            for (int j = 0; j < monsters.size(); j++) {
                Monster m = monsters.get(j);
                if (Math.hypot(b.getX() - m.getX(), b.getY() - m.getY()) < 25) {
                    m.takeDamage(b.getDamage());
                    bullets.remove(i);
                    i--;
                    if (m.isDead()) {
                        monsters.remove(j);
                        trashes.add(new Trash(m.getX(), m.getY(), 1));
                        j--;
                    }
                    break;
                }
            }
        }
    }
    
    public void checkMonsterPlayerCollision(List<Monster> monsters, Player player) {
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            if (Math.hypot(player.getX() - m.getX(), player.getY() - m.getY()) < 30) {
                player.takeDamage(10);
                monsters.remove(i);
                i--;
            }
        }
    }
    
    public void checkPlayerTrashCollision(Player player, List<Trash> trashes,
                                          UpgradeSystem upgradeSystem, SDGStats sdgStats) {
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
}