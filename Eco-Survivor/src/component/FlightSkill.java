package component;

import entity.Monster;

public class FlightSkill implements ISkillStrategy {
    public static final FlightSkill INSTANCE = new FlightSkill();
    private static final int FLIGHT_DURATION_FRAMES = 60; //無敵時間減少

    private FlightSkill() {}

    @Override
    public boolean executeSkill(Monster self, float targetX, float targetY) {
        // 如果已經在飛行（無敵）狀態，不重複觸發
        if (self.isInvincible) {
            return false;
        }
        
        self.isInvincible = true;
        self.buffDurationFrames = FLIGHT_DURATION_FRAMES;
        System.out.println("[FlightSkill] 飞起来！將無敵 " + FLIGHT_DURATION_FRAMES + " 幀");
        return true;
    }
}