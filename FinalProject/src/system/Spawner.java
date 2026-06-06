package system;

import entity.Monster;
import entity.Player;
import ui.PopupManager;
import java.util.List;
import java.util.Random;

public class Spawner {
    
    private List<Monster> monsters;
    private Player player;
    private PopupManager popupManager;
    private Random random;
    
    private int waveCount;
    private int nextNewMonsterWave;
    private int currentMonsterType;
    private boolean waitingForPopup;
    
    public Spawner(List<Monster> monsters, Player player, PopupManager popupManager) {
        this.monsters = monsters;
        this.player = player;
        this.popupManager = popupManager;
        this.random = new Random();
        this.waveCount = 0;
        this.nextNewMonsterWave = 3;
        this.currentMonsterType = 0;
        this.waitingForPopup = false;
    }
    
    public void update() {
        if (waitingForPopup) return;
        
        if (random.nextInt(100) < 3 && monsters.size() < 20) {
            spawnMonster();
        }
        
        if (waveCount >= nextNewMonsterWave && currentMonsterType < MonsterData.MONSTERS.length - 1) {
            triggerNewMonsterPopup();
        }
    }
    
    public void spawnMonster() {
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 800 + random.nextDouble() * 700;
        float x = player.getX() + (float)(Math.cos(angle) * distance);
        float y = player.getY() + (float)(Math.sin(angle) * distance);
        
        MonsterData.MonsterInfo info = MonsterData.getMonster(currentMonsterType);
        Monster monster = new Monster(x, y, currentMonsterType, 
                                       info.name, info.desc, info.harm,
                                       info.health, info.speed);
        monsters.add(monster);
        waveCount++;
    }
    
    private void triggerNewMonsterPopup() {
        currentMonsterType++;
        MonsterData.MonsterInfo info = MonsterData.getMonster(currentMonsterType);
        popupManager.showMonsterPopup(info.name, info.desc, info.harm);
        waitingForPopup = true;
        nextNewMonsterWave += 4;
    }
    
    public void onPopupClosed() {
        waitingForPopup = false;
        spawnMonster();
    }
    
    public void reset() {
        waveCount = 0;
        currentMonsterType = 0;
        waitingForPopup = false;
        monsters.clear();
    }
}