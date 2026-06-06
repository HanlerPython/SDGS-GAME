package entity;

public class Monster {
    
    private float x, y;
    private int health, maxHealth;
    private float speed;
    private int type;
    private String name;
    private String description;
    private String harm;
    private int rewardExp;
    
    public Monster(float x, float y, int type, String name, String desc, String harm, int health, float speed) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.name = name;
        this.description = desc;
        this.harm = harm;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.rewardExp = 10 + type * 5;
    }
    
    public void chase(float targetX, float targetY) {
        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double len = Math.hypot(dx, dy);
        if (len > 0) {
            this.x += (dx / len) * speed;
            this.y += (dy / len) * speed;
        }
    }
    
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public boolean isDead() {
        return this.health <= 0;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHarm() { return harm; }
    public int getRewardExp() { return rewardExp; }
}