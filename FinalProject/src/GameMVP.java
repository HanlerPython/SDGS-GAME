import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import ui.Camera;
import ui.GameUI;
import ui.PopupManager;
import entity.Player;
import entity.Projectile;
import entity.Monster;
import entity.Trash;
import system.WeaponSystem;
import system.CollisionSystem;
import system.Spawner;
import system.UpgradeSystem;
import system.SDGStats;
import minigame.TrashSortGame;

public class GameMVP extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    
    private final int SCREEN_WIDTH = 1920;
    private final int SCREEN_HEIGHT = 1080;
    private javax.swing.Timer timer;
    private String gameState = "playing";
    
    private Camera camera;
    private GameUI gameUI;
    private PopupManager popupManager;
    
    private Player player;
    private List<Projectile> bullets;
    private WeaponSystem weaponSystem;
    private CollisionSystem collisionSystem;
    
    private List<Monster> monsters;
    private Spawner spawner;
    
    private List<Trash> trashes;
    private UpgradeSystem upgradeSystem;
    private SDGStats sdgStats;
    private TrashSortGame trashSortGame;
    
    private Random random = new Random();
    
    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    private float mouseWorldX = 0, mouseWorldY = 0;
    
    public GameMVP() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(20, 30, 20));
        setFocusable(true);
        
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
        gameUI = new GameUI();
        popupManager = new PopupManager();
        popupManager.setScreenSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        
        player = new Player(0, 0);
        bullets = new ArrayList<>();
        weaponSystem = new WeaponSystem();
        collisionSystem = new CollisionSystem();
        
        monsters = new ArrayList<>();
        spawner = new Spawner(monsters, player, popupManager);
        
        trashes = new ArrayList<>();
        upgradeSystem = new UpgradeSystem(player);
        sdgStats = new SDGStats();
        trashSortGame = new TrashSortGame();
        
        for (int i = 0; i < 3; i++) {
            spawner.spawnMonster();
        }
        
        timer = new javax.swing.Timer(16, this);
        timer.start();
    }
    
    private void update() {
        if (popupManager.isShowing()) return;
        
        if (gameState.equals("minigame")) {
            trashSortGame.update();
            return;
        }
        
        if (gameState.equals("gameover")) return;
        
        player.updateMovement(upPressed, downPressed, leftPressed, rightPressed);
        
        if (mouseWorldX != 0 || mouseWorldY != 0) {
            weaponSystem.update(player, mouseWorldX, mouseWorldY, bullets);
        }
        
        for (int i = 0; i < bullets.size(); i++) {
            Projectile b = bullets.get(i);
            b.update();
            if (Math.abs(b.getX() - player.getX()) > 3000 || 
                Math.abs(b.getY() - player.getY()) > 3000) {
                bullets.remove(i);
                i--;
            }
        }
        
        spawner.update();
        
        for (Monster m : monsters) {
            m.chase(player.getX(), player.getY());
        }
        
        collisionSystem.checkBulletMonsterCollision(bullets, monsters, trashes, sdgStats);
        collisionSystem.checkMonsterPlayerCollision(monsters, player);
        collisionSystem.checkPlayerTrashCollision(player, trashes, upgradeSystem, sdgStats);
        
        if (player.getHealth() <= 0) {
            gameState = "gameover";
        }
        
        camera.follow(player);
        
        gameUI.updateStats(
            player.getHealth(), player.getMaxHealth(),
            player.getExperience(), player.getExpToNext(),
            player.getLevel(), player.getAttackDamage(),
            sdgStats.getTotalRecycled()
        );
        
        if (upgradeSystem.checkLevelUp()) {
            gameState = "upgrade";
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawWorldBackground(g2);
        drawWorldObjects(g2);
        gameUI.draw(g2);
        
        if (popupManager.isShowing()) {
            popupManager.draw(g2);
        }
        
        if (gameState.equals("upgrade")) {
            gameUI.drawUpgradeMenu(g2, SCREEN_WIDTH, SCREEN_HEIGHT, upgradeSystem.getUpgradeOptions());
        }
        
        if (gameState.equals("minigame")) {
            trashSortGame.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
        
        if (gameState.equals("gameover")) {
            gameUI.drawGameOver(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }
    
    private void drawWorldBackground(Graphics2D g2) {
        int gridSize = 100;
        float startX = camera.getX() % gridSize;
        float startY = camera.getY() % gridSize;
        
        g2.setColor(new Color(40, 50, 40));
        g2.setStroke(new BasicStroke(1));
        
        for (float x = -startX; x <= SCREEN_WIDTH + gridSize; x += gridSize) {
            g2.drawLine((int)x, 0, (int)x, SCREEN_HEIGHT);
        }
        for (float y = -startY; y <= SCREEN_HEIGHT + gridSize; y += gridSize) {
            g2.drawLine(0, (int)y, SCREEN_WIDTH, (int)y);
        }
    }
    
    private void drawWorldObjects(Graphics2D g2) {
        for (Trash t : trashes) {
            float screenX = camera.worldToScreenX(t.getX());
            float screenY = camera.worldToScreenY(t.getY());
            if (screenX > -50 && screenX < SCREEN_WIDTH + 50) {
                g2.setColor(new Color(150, 100, 50));
                g2.fillRect((int)screenX - 6, (int)screenY - 6, 12, 12);
                g2.setColor(Color.WHITE);
                g2.drawString("T", (int)screenX - 3, (int)screenY + 4);
            }
        }
        
        for (Monster m : monsters) {
            float screenX = camera.worldToScreenX(m.getX());
            float screenY = camera.worldToScreenY(m.getY());
            if (screenX > -100 && screenX < SCREEN_WIDTH + 100) {
                g2.setColor(new Color(100, 60, 40));
                g2.fillOval((int)screenX - 18, (int)screenY - 18, 36, 36);
                g2.setColor(Color.BLACK);
                g2.fillOval((int)screenX - 8, (int)screenY - 6, 5, 5);
                g2.fillOval((int)screenX + 3, (int)screenY - 6, 5, 5);
                
                g2.setColor(Color.RED);
                g2.fillRect((int)screenX - 20, (int)screenY - 28, 40, 6);
                g2.setColor(Color.GREEN);
                int hpWidth = (int)(40 * m.getHealth() / m.getMaxHealth());
                g2.fillRect((int)screenX - 20, (int)screenY - 28, hpWidth, 6);
            }
        }
        
        for (Projectile b : bullets) {
            float screenX = camera.worldToScreenX(b.getX());
            float screenY = camera.worldToScreenY(b.getY());
            g2.setColor(new Color(50, 200, 50));
            g2.fillOval((int)screenX - 4, (int)screenY - 4, 8, 8);
        }
        
        float playerScreenX = camera.worldToScreenX(player.getX());
        float playerScreenY = camera.worldToScreenY(player.getY());
        
        g2.setColor(new Color(0, 255, 0, 80));
        g2.fillOval((int)playerScreenX - 25, (int)playerScreenY - 25, 50, 50);
        g2.setColor(new Color(0, 200, 0));
        g2.fillOval((int)playerScreenX - 18, (int)playerScreenY - 18, 36, 36);
        g2.setColor(Color.WHITE);
        g2.fillOval((int)playerScreenX - 8, (int)playerScreenY - 6, 5, 5);
        g2.fillOval((int)playerScreenX + 3, (int)playerScreenY - 6, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval((int)playerScreenX - 7, (int)playerScreenY - 5, 3, 3);
        g2.fillOval((int)playerScreenX + 4, (int)playerScreenY - 5, 3, 3);
        
        float mouseScreenX = camera.worldToScreenX(mouseWorldX);
        float mouseScreenY = camera.worldToScreenY(mouseWorldY);
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int)playerScreenX, (int)playerScreenY, (int)mouseScreenX, (int)mouseScreenY);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (popupManager.isShowing()) {
            if (key == KeyEvent.VK_ESCAPE) {
                popupManager.closePopup();
                spawner.onPopupClosed();
            }
            return;
        }
        
        if (gameState.equals("upgrade")) {
            if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_3) {
                int choice = key - KeyEvent.VK_1;
                upgradeSystem.applyUpgrade(choice);
                gameState = "playing";
            }
            return;
        }
        
        if (gameState.equals("minigame")) {
            trashSortGame.onKeyPress(key);
            if (key == KeyEvent.VK_ESCAPE) {
                gameState = "playing";
            }
            return;
        }
        
        if (key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_D) rightPressed = true;
        
        if (key == KeyEvent.VK_R && gameState.equals("gameover")) {
            resetGame();
        }
        
        if (key == KeyEvent.VK_SPACE) {
            weaponSystem.forceShoot(player, mouseWorldX, mouseWorldY, bullets);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_S) downPressed = false;
        if (key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_D) rightPressed = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseWorldX = camera.screenToWorldX(e.getX());
        mouseWorldY = camera.screenToWorldY(e.getY());
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (popupManager.isShowing()) {
            if (popupManager.isCloseButtonClicked(e.getX(), e.getY())) {
                popupManager.closePopup();
                spawner.onPopupClosed();
            }
            return;
        }
        
        if (gameState.equals("playing") && e.getButton() == MouseEvent.BUTTON1) {
            weaponSystem.forceShoot(player, mouseWorldX, mouseWorldY, bullets);
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
    
    private void resetGame() {
        player.reset(0, 0);
        bullets.clear();
        monsters.clear();
        trashes.clear();
        spawner.reset();
        upgradeSystem.reset();
        sdgStats.reset();
        gameUI.reset();
        gameState = "playing";
        
        for (int i = 0; i < 3; i++) {
            spawner.spawnMonster();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Eco Survivor - SDGs");
            GameMVP game = new GameMVP();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}