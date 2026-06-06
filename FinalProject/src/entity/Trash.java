package entity;

public class Trash {
    
    private float x, y;
    private int type;
    private int value;
    
    public Trash(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = 10;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getType() { return type; }
    public int getValue() { return value; }
}