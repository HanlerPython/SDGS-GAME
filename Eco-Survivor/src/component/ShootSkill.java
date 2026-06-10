package component;

import entity.Monster;

public class ShootSkill implements ISkillStrategy {
    public static final ShootSkill INSTANCE = new ShootSkill();

    private ShootSkill() {
    }

    @Override
    public boolean executeSkill(Monster self, float targetX, float targetY) {
        // 檢查玩家是否在射程內
        float dx = targetX - self.getX();
        float dy = targetY - self.getY();
        float distSq = dx * dx + dy * dy;

        // 射程 500 單位
        if (distSq > 500 * 500) {
            return false;
        }

        System.out.println("怪物類型 " + self.getType() + " 發射了子彈！");
        self.getProjectileSystem().spawnProjectile(self.getX(), self.getY(), targetX, targetY,
                self.getAttackDamage(), false);

        return true;
    }
}