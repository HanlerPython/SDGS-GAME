package system;

import java.util.Random;

public class WaveData {

    private static final Random RANDOM = new Random();

    public static class WaveConfig {
        public final int maxMonsters;
        public final long spawnInterval;
        public final int requiredKills;      // -1 表示最終波次，觸發淨化任務
        public final int[] allowedMonsterTypes;
        public final int[] spawnWeights;
        public final int totalWeight;

        public WaveConfig(int maxMonsters, long spawnInterval, int requiredKills, int[] allowedMonsterTypes, int[] spawnWeights) {
            if (allowedMonsterTypes.length != spawnWeights.length) {
                throw new IllegalArgumentException("波次設定的怪物種類與權重陣列長度不符");
            }

            this.maxMonsters = maxMonsters;
            this.spawnInterval = spawnInterval;
            this.requiredKills = requiredKills;
            this.allowedMonsterTypes = allowedMonsterTypes;
            this.spawnWeights = spawnWeights;

            int sum = 0;
            for (int weight : spawnWeights) {
                if (weight <= 0) {
                    throw new IllegalArgumentException("怪物生成權重必須大於 0");
                }
                sum += weight;
            }
            this.totalWeight = sum;
        }
    }
    
    private static final WaveConfig[] WAVES = {
        // 第 1 波
        new WaveConfig(10, 700, 30,
            new int[]{0}, 
            new int[]{100}
        ),
        // 第 2 波
        new WaveConfig(20, 500, 70,
            new int[]{0, 1}, 
            new int[]{80, 20}
        ),
        // 第 3 波
        new WaveConfig(30, 350, 120,
            new int[]{0, 1, 2}, 
            new int[]{60, 25, 15}
        ),
        // 第 4 波
        new WaveConfig(50, 250, 180,
            new int[]{0, 1, 2, 3, 4}, 
            new int[]{25, 30, 25, 5, 15}
        ),
        //第 5 波（最終波次）
        new WaveConfig(100, 150, -1,
            new int[]{0, 1, 2, 3, 4}, 
            new int[]{20, 25, 25, 10, 20}
        ),
    };
    
    private static final WaveConfig ENDLESS_WAVE = new WaveConfig(120, 80, 50,
        new int[]{0, 1, 2, 3, 4},
        new int[]{20, 25, 25, 15, 15}
    );
    
    public static int getRandomMonsterType(WaveConfig config) {
        int r = RANDOM.nextInt(config.totalWeight);
        
        for (int i = 0; i < config.spawnWeights.length; i++) {
            r -= config.spawnWeights[i];
            if (r < 0) {
                return config.allowedMonsterTypes[i];
            }
        }
        
        return config.allowedMonsterTypes[0];
    }
    
    public static WaveConfig getWave(int waveIndex) {
        int safeIndex = Math.max(0, waveIndex);
        
        if (safeIndex >= WAVES.length) {
            return ENDLESS_WAVE;
        }
        
        return WAVES[safeIndex];
    }
    
    public static int getWaveCount() {
        return WAVES.length;
    }
    
    public static boolean isFinalWave(int waveIndex) {
        if (waveIndex < 0 || waveIndex >= WAVES.length) {
            return false;
        }
        return WAVES[waveIndex].requiredKills == -1;
    }
    
    public static void setWaveRequiredKills(int waveIndex, int requiredKills) {
        if (waveIndex >= 0 && waveIndex < WAVES.length) {
            try {
                java.lang.reflect.Field field = WaveConfig.class.getDeclaredField("requiredKills");
                field.setAccessible(true);
                field.setInt(WAVES[waveIndex], requiredKills);
            } catch (Exception e) {
                System.err.println("【WaveData】無法動態修改擊殺需求: " + e.getMessage());
            }
        }
    }
}