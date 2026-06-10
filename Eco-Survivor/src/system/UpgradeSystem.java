package system;

import entity.Weapon.Weapon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.Map;

import entity.Player;
import system.WeaponSystem.WeaponType;
import ui.PopupManager;

public class UpgradeSystem {
    private record UpgradeOption(
            String description,
            BiConsumer<Player, WeaponSystem> action) {
    }

    private boolean levelUpPending;
    private List<UpgradeOption> activeChoices;

    public UpgradeSystem() {
        this.levelUpPending = false;
    }

    public void update(PopupManager popupManager, WeaponSystem weaponSystem, Player player) {
        if (levelUpPending) {
            triggerNewUpgradePopup(popupManager, weaponSystem, player);
        }
    }

    public void triggerLevelUp() {
        this.levelUpPending = true;
    }

    public void triggerNewUpgradePopup(PopupManager popupManager, WeaponSystem weaponSystem, Player player) {
        List<UpgradeOption> dynamicPool = new ArrayList<>();

        // 1. 加入永遠都可以升級的基礎能力
        dynamicPool.add(new UpgradeOption("Increase Attack +15", (p, w) -> p.setAttackDamage(p.getAttackDamage() + 15)));
        dynamicPool.add(new UpgradeOption("Increase Speed +20%", (p, w) -> p.setMoveSpeed(p.getMoveSpeed() * 1.2f)));
        dynamicPool.add(new UpgradeOption("Max Health +20", (p, w) -> p.setMaxHealth(p.getMaxHealth() + 20, true)));

        List<Weapon> weapons = weaponSystem.getWeapons();

        // 2. 動態檢查玩家身上的武器，未滿等才加入選項池
        if (weapons != null) {
            for (Weapon weapon : weapons) {
                if (!weapon.isMaxLevel()) {
                    dynamicPool.add(new UpgradeOption(
                            weapon.getNextLevelDescription(),
                            (p, w) -> weapon.upgrade()
                    ));
                }
            }

            if (weapons.size() == 1) {
                List<WeaponType> unownedWeapons = weaponSystem.getOwnedType().entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                for (WeaponType type : unownedWeapons) {
                    dynamicPool.add(new UpgradeOption(
                            "Get " + type.toString(),
                            (p, w) -> w.addWeapon(type)
                    ));
                }
            }
        }

        // 3. 打亂牌池並抽取最多三個選項
        Collections.shuffle(dynamicPool);
        int choiceCount = Math.min(3, dynamicPool.size());
        this.activeChoices = dynamicPool.subList(0, choiceCount);

        // 4. 轉換為字串陣列
        String[] displayTexts = new String[choiceCount];
        for (int i = 0; i < choiceCount; i++) {
            displayTexts[i] = activeChoices.get(i).description();
        }
        
        // 5. 建立回調陣列和圖案名稱陣列
        Runnable[] callbacks = new Runnable[choiceCount];
        String[] iconNames = new String[choiceCount];
        
        for (int i = 0; i < choiceCount; i++) {
            final int index = i;
            callbacks[i] = () -> {
                applyUpgrade(index, player, weaponSystem);
            };
            
            String description = activeChoices.get(i).description();
            
            // 根據選項內容設定圖案檔名
            if (description.contains("Attack") && !description.contains("Damage")) {
                iconNames[i] = "upgrade_attack.png";
            } else if (description.contains("Speed")) {
                iconNames[i] = "upgrade_speed.png";
            } else if (description.contains("Health")) {
                iconNames[i] = "upgrade_health.png";
            } else if (description.contains("Damage")) {
                iconNames[i] = "upgrade_damage_up.png";
            } else if (description.contains("Cooldown")) {
                iconNames[i] = "upgrade_cooldown.png";
            } else if (description.contains("Range")) {
                iconNames[i] = "upgrade_range.png";
            } else if (description.contains("Get Solar Zone")) {
                iconNames[i] = "upgrade_sun.png";
            } else if (description.contains("Get UV Laser")) {
                iconNames[i] = "upgrade_laser.png";
            } else if (description.contains("Seed Gun")) {
                iconNames[i] = "upgrade_peashooter.png";
            } else if (description.contains("Solar Zone")) {
                iconNames[i] = "upgrade_sun.png";
            } else if (description.contains("UV Laser")) {
                iconNames[i] = "upgrade_laser.png";
            } else {
                iconNames[i] = "upgrade_default.png";
            }
        }
        
        // 6. 顯示升級選單
        popupManager.showUpgradePopup(displayTexts, callbacks, iconNames);
    }

    public void applyUpgrade(int choice, Player player, WeaponSystem weaponSystem) {
        if (choice >= 0 && choice < activeChoices.size()) {
            activeChoices.get(choice).action().accept(player, weaponSystem);
        }
        levelUpPending = false;
    }

    public void reset() {
        levelUpPending = false;
    }
}