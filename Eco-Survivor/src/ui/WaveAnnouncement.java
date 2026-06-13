package ui;

import java.awt.*;

public class WaveAnnouncement {

    private String waveText = "";
    private String customMessage = "";
    private float alpha = 0f;
    private long startTime = 0;
    private boolean isShowing = false;
    private boolean isCustomMessage = false;

    private static final long FADE_IN_DURATION = 800; // 淡入 0.8 秒
    private static final long SHOW_DURATION = 2500; // 停留 2.5 秒
    private static final long FADE_OUT_DURATION = 800; // 淡出 0.8 秒
    private static final long TOTAL_DURATION = FADE_IN_DURATION + SHOW_DURATION + FADE_OUT_DURATION;

    public void showWave(int waveNumber) {
        this.waveText = "Wave " + waveNumber;
        this.isCustomMessage = false;
        this.startTime = System.currentTimeMillis();
        this.isShowing = true;
        this.alpha = 0f;

        System.out.println("【WaveAnnouncement】顯示: " + waveText);
    }

    public void showCustomMessage(String message) {
        this.customMessage = message;
        this.isCustomMessage = true;
        this.startTime = System.currentTimeMillis();
        this.isShowing = true;
        this.alpha = 0f;

        System.out.println("【WaveAnnouncement】顯示自訂訊息: " + message);
    }

    public void update() {
        if (!isShowing)
            return;

        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed >= TOTAL_DURATION) {
            isShowing = false;
            alpha = 0f;
            return;
        }

        if (elapsed < FADE_IN_DURATION) {
            alpha = (float) elapsed / FADE_IN_DURATION;
        } else if (elapsed < FADE_IN_DURATION + SHOW_DURATION) {
            alpha = 1f;
        } else {
            long fadeOutElapsed = elapsed - (FADE_IN_DURATION + SHOW_DURATION);
            alpha = 1f - (float) fadeOutElapsed / FADE_OUT_DURATION;
        }

        alpha = Math.max(0f, Math.min(1f, alpha));
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        if (!isShowing || alpha <= 0.01f)
            return;

        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String textToShow = isCustomMessage ? customMessage : waveText;

        Font font;
        if (isCustomMessage) {
            font = new Font("微軟正黑體", Font.BOLD, 32);
        } else {
            font = new Font("微軟正黑體", Font.BOLD, 56);
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(textToShow);
        int textHeight = fm.getAscent();

        int centerX = (screenWidth - textWidth) / 2;
        int centerY = (screenHeight - textHeight) / 2 + textHeight / 2;

        // 黑色外框（多層描邊）
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                if (Math.abs(dx) + Math.abs(dy) <= 3) {
                    g.drawString(textToShow, centerX + dx, centerY + dy);
                }
            }
        }

        // 白色文字
        g.setColor(Color.WHITE);
        g.drawString(textToShow, centerX, centerY);

        g.setComposite(originalComposite);
    }

    public boolean isShowing() {
        return isShowing;
    }
}