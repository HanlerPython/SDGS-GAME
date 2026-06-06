package entity;

public class Projectile {
    
    private float x, y;
    private float vx, vy;
    private int damage;
    
    public Projectile(float x, float y, float targetX, float targetY, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        
        float dx = targetX - x;
        float dy = targetY - y;
        double len = Math.hypot(dx, dy);
        if (len > 0) {
            this.vx = (float)(dx / len * 12);
            this.vy = (float)(dy / len * 12);
        }
    }
    
    public void update() {
        x += vx;
        y += vy;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getDamage() { return damage; }
}