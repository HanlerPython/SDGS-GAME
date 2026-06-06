package system;

public class MonsterData {
    
    public static class MonsterInfo {
        public String name;
        public String desc;
        public String harm;
        public int health;
        public float speed;
        
        public MonsterInfo(String name, String desc, String harm, int health, float speed) {
            this.name = name;
            this.desc = desc;
            this.harm = harm;
            this.health = health;
            this.speed = speed;
        }
    }
    
    public static final MonsterInfo[] MONSTERS = {
        new MonsterInfo(
            "💩 Plastic Golem",
            "A monster made of discarded plastic bottles",
            "Plastic kills marine life and takes 500 years to decompose",
            50, 2.0f
        ),
        new MonsterInfo(
            "🏭 Smog Chimera",
            "Factory monster emitting black smoke",
            "Causes air pollution, global warming, and respiratory diseases",
            70, 1.8f
        ),
        new MonsterInfo(
            "🔋 Toxic Battery",
            "A monster formed from discarded batteries",
            "Heavy metals contaminate soil and groundwater",
            60, 2.2f
        ),
        new MonsterInfo(
            "🛍 Plastic Phantom",
            "A ghost made of floating plastic bags",
            "Clogs drainage systems and suffocates animals",
            55, 2.5f
        ),
        new MonsterInfo(
            "📱 E-Waste Golem",
            "A monster made of discarded electronics",
            "Toxic substances leach into the environment",
            80, 1.5f
        )
    };
    
    public static MonsterInfo getMonster(int index) {
        return MONSTERS[Math.min(index, MONSTERS.length - 1)];
    }
    
    public static int getMonsterCount() {
        return MONSTERS.length;
    }
}