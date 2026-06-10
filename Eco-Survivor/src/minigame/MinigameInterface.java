package minigame;

import java.awt.Graphics2D;

public interface MinigameInterface {
    void start(int width, int height);
    void update();
    void draw(Graphics2D g, int width, int height);
    void onKeyPress(int keyCode);
    void handleMouseClick(int mouseX, int mouseY);
    boolean isActive();
    void stop();
    boolean isWin();
}