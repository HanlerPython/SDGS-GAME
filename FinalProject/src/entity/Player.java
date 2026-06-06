package entity;

public class Player {
    
    private float x, y;
    private int health, maxHealth;
    private int level;
    private int experience;
    private int expToNext;
    private int attackDamage;
    private float moveSpeed;
    
    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.health = 100;
        this.maxHealth = 100;
        this.level = 1;
        this.experience = 0;
        this.expToNext = 50;
        this.attackDamage = 10;
        this.moveSpeed = 5.0f;
    }
    
    public void updateMovement(boolean up, boolean down, boolean left, boolean right) {
        float dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;
        
        if (dx != 0 || dy != 0) {
            double len = Math.hypot(dx, dy);
            dx /= len;
            dy /= len;
        }
        
        x += dx * moveSpeed;
        y += dy * moveSpeed;
    }
    
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public void addExperience(int exp) {
        this.experience += exp;
        while (this.experience >= expToNext) {
            levelUp();
        }
    }
    
    private void levelUp() {
        this.experience -= expToNext;
        this.level++;
        this.expToNext = this.level * 50;
        this.attackDamage += 5;
        this.health = Math.min(maxHealth, this.health + 20);
    }
    
    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.health = maxHealth;
        this.level = 1;
        this.experience = 0;
        this.expToNext = 50;
        this.attackDamage = 10;
        this.moveSpeed = 5.0f;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExpToNext() { return expToNext; }
    public int getAttackDamage() { return attackDamage; }
    public float getMoveSpeed() { return moveSpeed; }
    
    public void setAttackDamage(int damage) { this.attackDamage = damage; }
    public void setMoveSpeed(float speed) { this.moveSpeed = speed; }
}