package system;

import entity.Monster;
import entity.Player;
import entity.Trash;
import entity.Weapon.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class WeaponSystem {
    public enum WeaponType {
        SEEDGUN("Seed Gun"),
        SOLARZONE("Solar Zone"),
        UVLASER("UV Laser");

        private final String displayName;

        WeaponType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }

    Map<WeaponType, Boolean> ownedType;
    private List<Weapon> weapons;

    public WeaponSystem() {
        this.ownedType = new HashMap<>();
        this.ownedType.put(WeaponType.SEEDGUN, false);
        this.ownedType.put(WeaponType.SOLARZONE, false);
        this.ownedType.put(WeaponType.UVLASER, false);
        this.weapons = new ArrayList<>();
        this.addWeapon(WeaponType.SEEDGUN);
    }

    public void update(Player player, float targetX, float targetY,
            ProjectileSystem projectileSystem, List<Monster> monsters, List<Trash> trashes) {
        for (Weapon weapon : weapons) {
            weapon.update(player, targetX, targetY, projectileSystem, monsters, trashes);
        }
    }

    public void upgrade(WeaponType type) {
        for (Weapon weapon : weapons) {
            if (weapon.getType() == type) {
                weapon.upgrade();
                break;
            }
        }
    }

    public void addWeapon(WeaponType type) {
        switch (type) {
            case SEEDGUN:
                weapons.add(new SeedGun());
                ownedType.put(WeaponType.SEEDGUN, true);
                break;
            case SOLARZONE:
                weapons.add(new SolarZone());
                ownedType.put(WeaponType.SOLARZONE, true);
                break;
            case UVLASER:
                weapons.add(new UVLaser());
                ownedType.put(WeaponType.UVLASER, true);
                break;
        }
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public Map<WeaponType, Boolean> getOwnedType() {
        return ownedType;
    }
    
    public void reset() {
        // 清除所有武器
        weapons.clear();
        
        // 重置擁有狀態
        ownedType.put(WeaponType.SEEDGUN, false);
        ownedType.put(WeaponType.SOLARZONE, false);
        ownedType.put(WeaponType.UVLASER, false);
        
        // 重新加入初始武器
        addWeapon(WeaponType.SEEDGUN);
        
        System.out.println("【WeaponSystem】武器系統已重置");
    }
    
}