package component;

import entity.Monster;
import java.util.Random;
import system.Spawner;

public class BirthSkill implements ISkillStrategy {
    public static final BirthSkill INSTANCE = new BirthSkill();
    private static final Random random = new Random();
    private BirthSkill() {}

    @Override
    public boolean executeSkill(Monster self, float targetX, float targetY) {
        float dx = targetX - self.getX();
        float dy = targetY - self.getY();
        float distSq = dx * dx + dy * dy;
        
        // 玩家太遠，不浪費技能
        if (distSq > 400.0f * 400.0f) {
            return false;
        }
        
        if (self.skillStateTracker < 3) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float finalX = self.getX() + (float)(Math.cos(angle) * 20.0f);
            float finalY = self.getY() + (float)(Math.sin(angle) * 20.0f);
            Spawner.requestSpawnAt(0, finalX, finalY);
            
            self.skillStateTracker++;
            System.out.println("[BirthSkill] 诶他变多了，已生成 " + self.skillStateTracker + "/3 隻");
            return true;
        }
        
        return false;
    }
}