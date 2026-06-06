package ui;

import entity.Player;

public class Camera {
    
    private float x, y;
    private int screenWidth;
    private int screenHeight;
    
    public Camera(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.x = 0;
        this.y = 0;
    }
    
    public void follow(Player player) {
        this.x = player.getX() - screenWidth / 2;
        this.y = player.getY() - screenHeight / 2;
    }
    
    public float worldToScreenX(float worldX) {
        return worldX - x;
    }
    
    public float worldToScreenY(float worldY) {
        return worldY - y;
    }
    
    public float screenToWorldX(float screenX) {
        return screenX + x;
    }
    
    public float screenToWorldY(float screenY) {
        return screenY + y;
    }
    
    public boolean isInView(float worldX, float worldY, int margin) {
        float screenX = worldToScreenX(worldX);
        float screenY = worldToScreenY(worldY);
        return (screenX > -margin && screenX < screenWidth + margin &&
                screenY > -margin && screenY < screenHeight + margin);
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
}
