package minigame;

import java.awt.*;
import java.util.Random;

public class TrashSortGame implements MinigameInterface {
    
    private boolean active;
    private int score;
    private String currentTrash;
    private String[] trashItems;
    private int[] correctAnswers;
    private Random random;
    private int rewardAttackBonus;
    private int sortedCount;
    private final int TARGET_COUNT = 5;
    private boolean isWin;
    private boolean lastAnswerCorrect;
    
    public TrashSortGame() {
        this.active = false;
        this.score = 0;
        this.random = new Random();
        this.rewardAttackBonus = 0;
        
        this.trashItems = new String[]{"Plastic Bottle", "Cardboard Box", "Glass Bottle", "Apple Core"};
        this.correctAnswers = new int[]{1, 2, 3, 4};
    }
    
    @Override
    public void start(int width, int height) {
        this.active = true;
        this.score = 0;
        this.rewardAttackBonus = 0;
        this.sortedCount = 0;
        this.isWin = false;
        this.lastAnswerCorrect = true;
        nextTrash();
    }
    
    @Override
    public void stop() {
        this.active = false;
    }
    
    private void nextTrash() {
        int index = random.nextInt(trashItems.length);
        currentTrash = trashItems[index];
    }
    
    private void checkAnswer(int choice) {
        if (isWin) return;
        sortedCount++;

        int index = -1;
        for (int i = 0; i < trashItems.length; i++) {
            if (trashItems[i].equals(currentTrash)) {
                index = i;
                break;
            }
        }
        
        if (index != -1 && correctAnswers[index] == choice) {
            score += 10;
            this.lastAnswerCorrect = true;
            rewardAttackBonus += 5;
            nextTrash();
        } else {
            score = Math.max(0, score - 10);
            this.lastAnswerCorrect = false;
            rewardAttackBonus = Math.max(0, rewardAttackBonus - 5);
            nextTrash();
        }
        
        if (sortedCount >= TARGET_COUNT) {
            isWin = true;
        }
    }
    
    @Override
    public void update() {
    }
    
    @Override
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(20, 25, 35));
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        if (isWin) {
            drawCenteredString(g, "SDG 12: TRASH SORTING", screenWidth, 150, new Font("Arial", Font.BOLD, 36), new Color(100, 200, 100));
            drawCenteredString(g, "FINISHED! ALL ITEMS CLASSIFIED!", screenWidth, 250, new Font("Arial", Font.BOLD, 28), new Color(100, 255, 100));
            drawCenteredString(g, "Reward: +" + rewardAttackBonus + " Attack Power", screenWidth, 350, new Font("Arial", Font.BOLD, 24), Color.WHITE);
            drawCenteredString(g, "Press ESC to Return to Battlefield", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 18), Color.GRAY);
        } else {
            drawCenteredString(g, "SDG 12: TRASH SORTING", screenWidth, 80, new Font("Arial", Font.BOLD, 36), new Color(100, 200, 100));
            drawCenteredString(g, "Progress: " + sortedCount + " / " + TARGET_COUNT, screenWidth, 150, new Font("Arial", Font.PLAIN, 20), Color.WHITE);
            
            if (sortedCount > 0) {
                if (lastAnswerCorrect) {
                    drawCenteredString(g, "CORRECT!", screenWidth, 220, new Font("Arial", Font.BOLD, 24), new Color(100, 255, 100));
                } else {
                    drawCenteredString(g, "WRONG!", screenWidth, 220, new Font("Arial", Font.BOLD, 24), new Color(255, 100, 100));
                }
            }
            
            drawCenteredString(g, currentTrash, screenWidth, 280, new Font("Arial", Font.BOLD, 32), Color.WHITE);
            drawCenteredString(g, "[1] Plastic   [2] Paper   [3] Glass   [4] Food Waste", screenWidth, 380, new Font("Arial", Font.PLAIN, 18), Color.LIGHT_GRAY);
            drawCenteredString(g, "Attack Bonus: +" + rewardAttackBonus, screenWidth, 460, new Font("Arial", Font.PLAIN, 18), rewardAttackBonus > 0 ? Color.YELLOW : Color.LIGHT_GRAY);
            drawCenteredString(g, "Press ESC to exit", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 16), Color.GRAY);
        }
    }
    
    private void drawCenteredString(Graphics2D g, String text, int screenWidth, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (screenWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
    
    @Override
    public void onKeyPress(int key) {
        if (!active || isWin) return;
        if (key >= 49 && key <= 52) {
            checkAnswer(key - 48);
        }
    }
    
    @Override
    public void handleMouseClick(int x, int y) {}
    
    @Override
    public boolean isActive() { return active; }
    
    @Override
    public boolean isWin() { return isWin; }
    
    public int getRewardAttackBonus() { return rewardAttackBonus; }
    
    public void resetReward() { this.rewardAttackBonus = 0; }
}