package system;

import entity.Player;

public class UpgradeSystem {
    
    private Player player;
    private boolean levelUpPending;
    private String[] currentOptions;
    
    public UpgradeSystem(Player player) {
        this.player = player;
        this.levelUpPending = false;
        this.currentOptions = new String[]{
            "⚔ Increase Attack +5",
            "🏃 Increase Speed +20%",
            "💚 Max Health +20"
        };
    }
    
    public boolean checkLevelUp() {
        if (levelUpPending) return true;
        return false;
    }
    
    public void triggerLevelUp() {
        this.levelUpPending = true;
    }
    
    public void applyUpgrade(int choice) {
        switch (choice) {
            case 0:
                player.setAttackDamage(player.getAttackDamage() + 5);
                break;
            case 1:
                player.setMoveSpeed(player.getMoveSpeed() * 1.2f);
                break;
            case 2:
                break;
        }
        levelUpPending = false;
    }
    
    public String[] getUpgradeOptions() {
        return currentOptions;
    }
    
    public void reset() {
        levelUpPending = false;
    }
}