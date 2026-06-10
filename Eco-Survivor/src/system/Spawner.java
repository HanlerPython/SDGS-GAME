package system;

import entity.Monster;
import entity.Player;
import entity.Portal;
import entity.Trash;
import system.WaveData.WaveConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class Spawner {
    
    public interface WaveListener {
        void onWaveChanged(int waveNumber);
    }
    
    public interface FinalWaveListener {
        void onFinalWaveTrigger();
    }
    
    private final Deque<Monster> monsterPool;
    private final List<Monster> activeMonsters;
    private final List<Trash> trashes;
    private final List<Portal> portals;
    private final Player player;
    private final Random random;

    private long lastRealTime = 0;
    private long accumulatedGameTime = 0;
    private long lastSpawnTime = 0;

    private int currentWaveIndex = 0;
    private WaveConfig currentWaveConfig;
    private int killCount = 0;
    private boolean finalWaveTriggered = false;
    private boolean isGameCleared = false;
    
    private boolean triggerPopup = false;
    private boolean isSpawningPaused = false;
    private long pauseStartTime = 0;
    private int currentMonsterType = 0;
    private final String[] GAME_TYPES = {"trashsortgame", "powercablegame", "faucetgame"};

    private static final int MAX_SPAWN_REQUESTS = 50;
    private static final int[] reqTypes = new int[MAX_SPAWN_REQUESTS];
    private static final float[] reqXs = new float[MAX_SPAWN_REQUESTS];
    private static final float[] reqYs = new float[MAX_SPAWN_REQUESTS];
    private static final boolean[] reqIsRandom = new boolean[MAX_SPAWN_REQUESTS];
    private static int requestCount = 0;
    private static final int ABSOLUTE_MAX_MONSTERS = 100;
    
    private WaveListener waveListener;
    private FinalWaveListener finalWaveListener;
    
    public void setWaveListener(WaveListener listener) {
        this.waveListener = listener;
    }
    
    public void setFinalWaveListener(FinalWaveListener listener) {
        this.finalWaveListener = listener;
    }
    
    public Spawner(List<Monster> monsters, List<Portal> portals, List<Trash> trashes, Player player) {
        this.activeMonsters = monsters;
        this.portals = portals;
        this.trashes = trashes;
        this.player = player;
        this.random = new Random();
        this.monsterPool = new ArrayDeque<>();
        
        this.currentWaveConfig = WaveData.getWave(0); 
    }
    
    public Spawner(List<Monster> monsters, List<Portal> portals, Player player) {
        this(monsters, portals, null, player);
    }

    public static void requestSpawnAt(int type, float x, float y) {
        if (requestCount < MAX_SPAWN_REQUESTS) {
            reqTypes[requestCount] = type;
            reqXs[requestCount] = x;
            reqYs[requestCount] = y;
            reqIsRandom[requestCount] = false;
            requestCount++;
        }
    }
    
    public static void requestSpawnRandom(int type) {
        if (requestCount < MAX_SPAWN_REQUESTS) {
            reqTypes[requestCount] = type;
            reqIsRandom[requestCount] = true;
            requestCount++;
        }
    }
    
    public void spawnMonster() {
        if (!isSpawningPaused && !isGameCleared) {
            int nextType = WaveData.getRandomMonsterType(this.currentWaveConfig);
            requestSpawnRandom(nextType);
        }
    }
    
    public void spawnPortal() {
        if (portals == null) return;
        
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 300 + random.nextDouble() * 500;
        float x = player.getX() + (float)(Math.cos(angle) * distance);
        float y = player.getY() + (float)(Math.sin(angle) * distance);
        
        int randomIndex = random.nextInt(GAME_TYPES.length);
        String gameType = GAME_TYPES[randomIndex];
        
        portals.add(new Portal(x, y, gameType));
    }
    
    public void cleanupPortals() {
        if (portals == null) return;
        for (int i = portals.size() - 1; i >= 0; i--) {
            Portal p = portals.get(i);
            double distance = Math.hypot(player.getX() - p.getX(), player.getY() - p.getY());
            if (distance > 2000) {
                portals.remove(i);
            }
        }
    }
    
    public boolean consumePopupEvent() {
        if (this.triggerPopup) {
            this.triggerPopup = false;
            return true;
        }
        return false;
    }
    
    public int getCurrentMonsterType() {
        return this.currentMonsterType;
    }
    
    public void pauseSpawning() {
        this.isSpawningPaused = true;
        this.pauseStartTime = System.currentTimeMillis();
    }
    
    public void resumeSpawning() {
        this.isSpawningPaused = false;
        long pausedDuration = System.currentTimeMillis() - this.pauseStartTime;
        this.lastSpawnTime += pausedDuration;
    }
    
    private void processSpawnRequests() {
        if (isGameCleared) return;
        
        for (int i = 0; i < requestCount; i++) {
            if (activeMonsters.size() < ABSOLUTE_MAX_MONSTERS) {
                
                float finalX = reqXs[i];
                float finalY = reqYs[i];
                
                if (reqIsRandom[i]) {
                    double angle = this.random.nextDouble() * 2 * Math.PI;
                    double distance = 800 + this.random.nextDouble() * 700;
                    finalX = this.player.getX() + (float)(Math.cos(angle) * distance);
                    finalY = this.player.getY() + (float)(Math.sin(angle) * distance);
                }
                
                spawnSpecificMonster(reqTypes[i], finalX, finalY);
            }
        }
        clearRequests(); 
    }
    
    public static void clearRequests() {
        requestCount = 0;
    }
    
    private void spawnSpecificMonster(int type, float x, float y) {
        Monster m;
        if (monsterPool.isEmpty()) {
            m = new Monster();
        } else {
            m = monsterPool.pop();
        }

        MonsterData.MonsterInfo info = MonsterData.getMonster(type);
        m.init(x, y, info);
        activeMonsters.add(m);
    }
    
    public void update() {
        long currentRealTime = System.currentTimeMillis();

        if (lastRealTime == 0) {
            lastRealTime = currentRealTime;
            return;
        }

        long deltaTime = currentRealTime - lastRealTime;
        lastRealTime = currentRealTime;

        if (deltaTime > 100) {
            deltaTime = 16;
        }

        if (!isSpawningPaused && !isGameCleared) {
            this.accumulatedGameTime += deltaTime;
        }
        
        checkWaveProgression();

        if (!isSpawningPaused && !isGameCleared && this.accumulatedGameTime - this.lastSpawnTime >= currentWaveConfig.spawnInterval) {
            if (activeMonsters.size() < currentWaveConfig.maxMonsters) {
                int nextType = WaveData.getRandomMonsterType(this.currentWaveConfig);
                requestSpawnRandom(nextType);
                this.lastSpawnTime = this.accumulatedGameTime;
            }
        }

        if (!isSpawningPaused && !isGameCleared && random.nextInt(200) < 1 && portals != null && portals.size() < 4) {
            spawnPortal();
        }
        cleanupPortals();

        processSpawnRequests();
    }

    private void checkWaveProgression() {
        if (isGameCleared) return;
        
        int requiredKills = currentWaveConfig.requiredKills;
        
        // 檢查是否為最終波次
        if (requiredKills == -1) {
            if (!finalWaveTriggered) {
                finalWaveTriggered = true;
                if (finalWaveListener != null) {
                    finalWaveListener.onFinalWaveTrigger();
                }
                System.out.println("[Spawner] 進入最終波次！觸發淨化任務，怪物繼續生成");
            }
            // 不 return，讓怪物繼續生成
        }
        
        // 非最終波次：檢查是否需要推進到下一波
        if (requiredKills > 0 && killCount >= requiredKills) {
            int nextWaveIndex = currentWaveIndex + 1;
            WaveConfig nextWaveConfig = WaveData.getWave(nextWaveIndex);
            
            this.currentWaveIndex = nextWaveIndex;
            this.currentWaveConfig = nextWaveConfig;
            this.currentMonsterType = Math.min(this.currentMonsterType + 1, MonsterData.getMonsterCount() - 1);
            
            if (nextWaveConfig.requiredKills == -1) {
                System.out.println("[Spawner] 第 " + (this.currentWaveIndex + 1) + " 波為最終波次，準備觸發淨化任務");
            } else {
                if (waveListener != null) {
                    waveListener.onWaveChanged(this.currentWaveIndex + 1);
                }
                for(int i = 0; i < 5; i++) { 
                	MonsterData.MonsterInfo info = MonsterData.getMonster(i);
                	info.baseHealth = (int)Math.round(info.baseHealth * (1 + Math.log(currentWaveIndex + 1) * 1.0f));
                }
                System.out.println("[Spawner] 波次推進！進入第 " + (this.currentWaveIndex + 1) + " 波 (擊殺: " + killCount + "/" + requiredKills + ")");
            }
        }
    }

    public void cleanupDeadMonsters() {
        for (int i = activeMonsters.size() - 1; i >= 0; i--) {
            Monster m = activeMonsters.get(i);
            
            if (m.isDead) {
                int lastIndex = activeMonsters.size() - 1;
                if (i != lastIndex) {
                    Monster lastMonster = activeMonsters.get(lastIndex);
                    activeMonsters.set(i, lastMonster);
                }
                activeMonsters.remove(lastIndex);
                
                if (trashes != null) {
                    trashes.add(new Trash(m.getX(), m.getY(), 1));
                }
                
                monsterPool.push(m);
                killCount++;
            }
        }
    }
    
    public int getKillCount() {
        return killCount;
    }
    
    public int getRequiredKillsForCurrentWave() {
        return currentWaveConfig.requiredKills;
    }
    
    public boolean isFinalWaveTriggered() {
        return finalWaveTriggered;
    }
    
    public void setGameCleared(boolean cleared) {
        this.isGameCleared = cleared;
    }
    
    public void stopSpawningForFinalWave() {
        this.finalWaveTriggered = true;
        this.isSpawningPaused = true;
    }

    public void reset() {
        this.accumulatedGameTime = 0;
        this.lastRealTime = 0;
        this.lastSpawnTime = 0;
        this.currentWaveIndex = 0;
        this.currentWaveConfig = WaveData.getWave(0);
        this.currentMonsterType = 0;
        this.triggerPopup = false;
        this.isSpawningPaused = false;
        this.killCount = 0;
        this.finalWaveTriggered = false;
        this.isGameCleared = false;

        for (Monster m : activeMonsters) {
            monsterPool.push(m); 
        }
        activeMonsters.clear();
        
        if (portals != null) {
            portals.clear();
        }
    }
}