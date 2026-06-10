package system;

import entity.Projectile;
import entity.Player;
import ui.Camera;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

public class ProjectileSystem {
    private Deque<Projectile> projectilePool;
    private List<Projectile> activeProjectiles;
    private List<Projectile> activeEnemyProjectiles;

    public ProjectileSystem() {
        this.activeProjectiles = new ArrayList<>();
        this.activeEnemyProjectiles = new ArrayList<>();
        this.projectilePool = new ArrayDeque<>();
    }

    // 創建或從池中獲取投射物
    public void spawnProjectile(float startX, float startY, float targetX, float targetY,
            int damage, boolean isPlayer) {
        Projectile p;
        if (projectilePool.isEmpty()) {
            p = new Projectile();
        } else {
            p = projectilePool.pop();
        }

        p.init(startX, startY, targetX, targetY, damage, isPlayer);
        if (isPlayer)
            activeProjectiles.add(p);
        else
            activeEnemyProjectiles.add(p);
    }

    public void update(Player player) {
        for (Projectile p : activeProjectiles) {
            p.update();
            if (Math.abs(p.getX() - player.getX()) > 2000 ||
                    Math.abs(p.getY() - player.getY()) > 2000) {
                p.setDestroyed(true);
            }
        }
        for (Projectile p : activeEnemyProjectiles) {
            p.update();
            if (Math.abs(p.getX() - player.getX()) > 2000 ||
                    Math.abs(p.getY() - player.getY()) > 2000) {
                p.setDestroyed(true);
            }
        }
        cleanupDeadProjectiles();
    }

    public void draw(Graphics2D g, Camera camera) {
        for (Projectile p : activeProjectiles) {
            p.draw(g, camera);
        }
        for (Projectile p : activeEnemyProjectiles) {
            p.draw(g, camera);
        }
    }

    // 回收已被標記為死亡的投射物至對象池
    public void cleanupDeadProjectiles() {
        for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);

            if (p.isDestroyed()) {
                int lastIndex = activeProjectiles.size() - 1;

                // O(1) 移除法：將陣列最後一個元素覆蓋當前要刪除的元素
                if (i != lastIndex) {
                    Projectile lastProjectile = activeProjectiles.get(lastIndex);
                    activeProjectiles.set(i, lastProjectile);
                }

                activeProjectiles.remove(lastIndex);
                projectilePool.push(p);
            }
        }
        for (int i = activeEnemyProjectiles.size() - 1; i >= 0; i--) {
            Projectile p = activeEnemyProjectiles.get(i);

            if (p.isDestroyed()) {
                int lastIndex = activeEnemyProjectiles.size() - 1;

                // O(1) 移除法：將陣列最後一個元素覆蓋當前要刪除的元素
                if (i != lastIndex) {
                    Projectile lastProjectile = activeEnemyProjectiles.get(lastIndex);
                    activeEnemyProjectiles.set(i, lastProjectile);
                }

                activeEnemyProjectiles.remove(lastIndex);
                projectilePool.push(p);
            }
        }
    }

    public List<Projectile> getProjectile(boolean isPlayer) {
        if (isPlayer)
            return activeProjectiles;
        else
            return activeEnemyProjectiles;
    }

    public void reset() {
        for (Projectile p : activeProjectiles) {
            projectilePool.push(p);
        }
        activeProjectiles.clear();
        for (Projectile p : activeEnemyProjectiles) {
            projectilePool.push(p);
        }
        activeEnemyProjectiles.clear();
    }
}