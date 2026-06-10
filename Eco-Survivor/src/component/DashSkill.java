package component;

import entity.Monster;

public class DashSkill implements ISkillStrategy {
    public static final DashSkill INSTANCE = new DashSkill();

    private DashSkill() {}

    @Override
    public boolean executeSkill(Monster self, float targetX, float targetY) {
        // 檢查是否已經在衝刺狀態
        if (self.buffDurationFrames > 0 && self.currentSpeed > self.getBaseSpeed()) {
            return false;
        }
        
        self.currentSpeed = self.getBaseSpeed() * 4.0f;
        self.buffDurationFrames = 45;
        System.out.println("[DashSkill] 冲刺！速度: " + self.currentSpeed);
        return true;
    }
}