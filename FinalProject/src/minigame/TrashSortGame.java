package minigame;

import java.awt.*;
import java.util.Random;

public class TrashSortGame {
    
    private boolean active;
    private int score;
    private String currentTrash;
    private String[] trashItems;
    private int[] correctAnswers;
    private Random random;
    private int rewardAttackBonus;
    
    public TrashSortGame() {
        this.active = false;
        this.score = 0;
        this.random = new Random();
        this.rewardAttackBonus = 0;
        
        this.trashItems = new String[]{"Plastic Bottle", "Cardboard Box", "Glass Bottle", "Apple Core"};
        this.correctAnswers = new int[]{1, 2, 3, 4};
    }
    
    public void start() {
        this.active = true;
        this.score = 0;
        this.rewardAttackBonus = 0;
        nextTrash();
    }
    
    public void stop() {
        this.active = false;
    }
    
    private void nextTrash() {
        int index = random.nextInt(trashItems.length);
        currentTrash = trashItems[index];
    }
    
    public void checkAnswer(int choice) {
        int index = -1;
        for (int i = 0; i < trashItems.length; i++) {
            if (trashItems[i].equals(currentTrash)) {
                index = i;
                break;
            }
        }
        
        if (index != -1 && correctAnswers[index] == choice) {
            score += 10;
            rewardAttackBonus += 2;
            nextTrash();
        } else {
            score = Math.max(0, score - 5);
            nextTrash();
        }
    }
    
    public void update() {
    }
    
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("TRASH SORTING", screenWidth/2 - 120, 80);
        
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Score: " + score, screenWidth/2 - 40, 140);
        
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString(currentTrash, screenWidth/2 - 100, 280);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("[1] Plastic   [2] Paper   [3] Glass   [4] Food Waste", 
                     screenWidth/2 - 220, 400);
        
        g.drawString("Press ESC to exit", screenWidth/2 - 70, 500);
        g.drawString("Reward: +" + rewardAttackBonus + " Attack", screenWidth/2 - 90, 560);
    }
    
    public void onKeyPress(int key) {
        if (key >= 49 && key <= 52) {
            checkAnswer(key - 48);
        }
    }
    
    public void onMouseClick(int x, int y) {}
    
    public boolean isActive() { return active; }
    public int getRewardAttackBonus() { return rewardAttackBonus; }
}