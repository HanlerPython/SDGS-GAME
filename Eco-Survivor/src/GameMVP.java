import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import minigame.FaucetGame;
import minigame.MinigameInterface;
import minigame.TrashSortGame;
import minigame.PowerCableGame;

import ui.Camera;
import ui.GameUI;
import ui.PopupManager;
import ui.MainMenu;
import ui.DirectionArrow;
import ui.MonsterGallery;
import ui.VictoryScreen;

import entity.Player;
import entity.Monster;
import entity.Trash;
import entity.Portal;
import entity.PurificationDevice;

import system.WeaponSystem;
import system.CollisionSystem;
import system.Spawner;
import system.UpgradeSystem;
import system.SDGStats;
import system.ProjectileSystem;
import system.MapSystem;
import system.GameAudio;
import system.VolumeManager;

public class GameMVP extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 720;
    private javax.swing.Timer timer;
    private String gameState = "playing";

    private Camera camera;
    private GameUI gameUI;
    private PopupManager popupManager;
    private MapSystem mapSystem;
    private ProjectileSystem projectileSystem;
    private MonsterGallery monsterGallery;
    private VictoryScreen victoryScreen;

    private Player player;
    private WeaponSystem weaponSystem;
    private CollisionSystem collisionSystem;

    private List<Monster> monsters;
    private Spawner spawner;
    private List<Trash> trashes;
    private List<Portal> portals;
    private UpgradeSystem upgradeSystem;
    private SDGStats sdgStats;

    private Random random = new Random();

    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    private float mouseWorldX = 0, mouseWorldY = 0;
    private int mouseScreenX = 0, mouseScreenY = 0;

    private Map<String, MinigameInterface> minigameRegistry = new HashMap<>();
    private MinigameInterface currentMinigame = null;

    private ui.WaveAnnouncement waveAnnouncement;
    private MainMenu mainMenu;
    private boolean inMainMenu = true;
    private long lastFrameTime = 0;

    private PurificationDevice purificationDevice;
    private DirectionArrow directionArrow;
    private long purificationTimeAccumulated = 0;
    private long lastPurificationTime = 0;
    private boolean isPurifying = false;
    private boolean isGameCleared = false;
    private static final long REQUIRED_PURIFICATION_TIME = 60000;
    private boolean finalMissionActive = false;

    public GameMVP() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
        gameUI = new GameUI();
        popupManager = new PopupManager();
        popupManager.setScreenSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        mapSystem = new MapSystem();
        mapSystem.generateRandomMap();

        upgradeSystem = new UpgradeSystem();
        projectileSystem = new ProjectileSystem();
        weaponSystem = new WeaponSystem();
        collisionSystem = new CollisionSystem();
        sdgStats = new SDGStats();

        monsters = new ArrayList<>();
        portals = new ArrayList<>();
        trashes = new ArrayList<>();

        minigameRegistry.put("trashsortgame", new TrashSortGame());
        minigameRegistry.put("powercablegame", new PowerCableGame());
        minigameRegistry.put("faucetgame", new FaucetGame());

        player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(), () -> upgradeSystem.triggerLevelUp());
        spawner = new Spawner(monsters, portals, trashes, player);
        waveAnnouncement = new ui.WaveAnnouncement();

        spawner.setWaveListener(waveNumber -> {
            if (waveAnnouncement != null && waveNumber != 5) {
                waveAnnouncement.showWave(waveNumber);
            }
        });

        spawner.setFinalWaveListener(() -> {
            if (waveAnnouncement != null)
                waveAnnouncement.showCustomMessage("請前往標記地點，啟動終極淨化裝置！");
            startFinalMission();
        });

        for (int i = 0; i < 3; i++)
            spawner.spawnMonster();

        VolumeManager.setListener(volume -> {
        });
        GameAudio.playBackgroundMusic("bgm.wav", VolumeManager.getVolume());

        monsterGallery = new MonsterGallery(() -> repaint());
        victoryScreen = new VictoryScreen();

        mainMenu = new MainMenu(SCREEN_WIDTH, SCREEN_HEIGHT, new MainMenu.MenuCallback() {
            @Override
            public void onStartGame() {
                inMainMenu = false;
                if (player == null) {
                    player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(),
                            () -> upgradeSystem.triggerLevelUp());
                }
                resetGame();
                repaint();
            }

            @Override
            public void onShowMonsterList() {
                monsterGallery.start();
            }

            @Override
            public void onExitGame() {
                System.exit(0);
            }
        });

        Monster.initMonsterProjectileSystem(projectileSystem);
        timer = new javax.swing.Timer(11, this);
        timer.start();
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;

        if (deltaTime > 50)
            deltaTime = 16;
        if (deltaTime < 5)
            return;

        if (monsterGallery.isActive())
            return;

        if (victoryScreen.isActive()) {
            victoryScreen.update();
            return;
        }

        if (inMainMenu) {
            mainMenu.updateBackground(SCREEN_WIDTH, SCREEN_HEIGHT, deltaTime);
            return;
        }

        if (player == null || popupManager.isShowing() || gameState.equals("gameover"))
            return;

        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.update();
            return;
        }

        player.updateMovement(upPressed, downPressed, leftPressed, rightPressed);
        mouseWorldX = camera.screenToWorldX(mouseScreenX);
        mouseWorldY = camera.screenToWorldY(mouseScreenY);

        if (mouseWorldX != 0 || mouseWorldY != 0) {
            weaponSystem.update(player, mouseWorldX, mouseWorldY, projectileSystem, monsters, trashes);
        }

        projectileSystem.update(player);
        spawner.update();

        if (finalMissionActive && !isGameCleared && purificationDevice != null && purificationDevice.isActive()) {
            updatePurification();
        }

        if (waveAnnouncement != null)
            waveAnnouncement.update();

        for (Monster m : monsters)
            m.update(player.getX(), player.getY());

        collisionSystem.checkBulletMonsterCollision(projectileSystem, monsters, trashes);
        collisionSystem.checkBulletPlayerCollision(projectileSystem, player);
        collisionSystem.checkMonsterPlayerCollision(monsters, player);
        collisionSystem.checkPlayerTrashCollision(player, trashes, sdgStats);

        Portal triggeredPortal = collisionSystem.checkPlayerPortalCollision(player, portals);
        if (triggeredPortal != null) {
            triggeredPortal.close();
            portals.remove(triggeredPortal);
            MinigameInterface targetGame = minigameRegistry.get(triggeredPortal.getGameType());
            if (targetGame != null) {
                gameState = "in_minigame";
                currentMinigame = targetGame;
                currentMinigame.start(SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        }

        if (player.getHealth() <= 0)
            gameState = "gameover";

        camera.follow(player);
        gameUI.updateStats(player.getHealth(), player.getMaxHealth(), player.getExperience(), player.getExpToNext(),
                player.getLevel(), player.getAttackDamage(), sdgStats.getTotalRecycled());

        upgradeSystem.update(popupManager, weaponSystem, player);
        spawner.cleanupDeadMonsters();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (monsterGallery.isActive()) {
            monsterGallery.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT, camera, mapSystem);
            return;
        }

        if (inMainMenu) {
            mainMenu.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
            return;
        }

        drawWorldBackground(g2);
        drawWorldObjects(g2);

        if (victoryScreen.isActive()) {
            victoryScreen.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT, camera);
            return;
        }

        gameUI.draw(g2);

        if (finalMissionActive && !isGameCleared && purificationDevice != null) {
            drawPurificationTimer(g2);
            if (directionArrow != null) {
                directionArrow.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT, player, purificationDevice);
            }
        }

        if (waveAnnouncement != null)
            waveAnnouncement.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        if (popupManager.isShowing())
            popupManager.draw(g2);
        if (gameState.equals("in_minigame") && currentMinigame != null)
            currentMinigame.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
        if (gameState.equals("gameover"))
            gameUI.drawGameOver(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void drawPurificationTimer(Graphics2D g) {
        long remaining = Math.max(0, REQUIRED_PURIFICATION_TIME - purificationTimeAccumulated);
        long seconds = remaining / 1000;
        long tenths = (remaining % 1000) / 100;
        String timeText = String.format("淨化剩餘: %02d.%01d 秒", seconds, tenths);

        g.setFont(new Font("微軟正黑體", Font.BOLD, 28));
        int textX = SCREEN_WIDTH - g.getFontMetrics().stringWidth(timeText) - 20;
        int textY = 50;

        g.setColor(Color.BLACK);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0)
                    g.drawString(timeText, textX + dx, textY + dy);
            }
        }
        g.setColor(Color.WHITE);
        g.drawString(timeText, textX, textY);
    }

    private void drawWorldBackground(Graphics2D g2) {
        mapSystem.drawMap(g2, camera, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void drawWorldObjects(Graphics2D g2) {
        for (Portal p : portals) {
            if (camera.worldToScreenX(p.getX()) > -50 && camera.worldToScreenX(p.getX()) < SCREEN_WIDTH + 50)
                p.draw(g2, camera);
        }

        if (finalMissionActive && purificationDevice != null && purificationDevice.isActive())
            purificationDevice.draw(g2, camera);

        for (Trash t : trashes) {
            if (camera.worldToScreenX(t.getX()) > -50 && camera.worldToScreenX(t.getX()) < SCREEN_WIDTH + 50)
                t.draw(g2, camera);
        }

        for (Monster m : monsters) {
            if (camera.worldToScreenX(m.getX()) > -100 && camera.worldToScreenX(m.getX()) < SCREEN_WIDTH + 100)
                m.draw(g2, camera);
        }

        projectileSystem.draw(g2, camera);

        for (entity.Weapon.Weapon weapon : weaponSystem.getWeapons()) {
            if (weapon instanceof entity.Weapon.SolarZone)
                ((entity.Weapon.SolarZone) weapon).draw(g2, camera, player);
            else if (weapon instanceof entity.Weapon.UVLaser)
                ((entity.Weapon.UVLaser) weapon).draw(g2, camera, player);
        }

        player.draw(g2, camera);

        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) camera.worldToScreenX(player.getX()), (int) camera.worldToScreenY(player.getY()),
                (int) camera.worldToScreenX(mouseWorldX), (int) camera.worldToScreenY(mouseWorldY));
    }

    private void returnToMainMenu() {
        inMainMenu = true;
        gameState = "playing";
        if (monsters != null)
            monsters.clear();
        if (trashes != null)
            trashes.clear();
        if (portals != null)
            portals.clear();
        if (projectileSystem != null)
            projectileSystem.reset();
        if (spawner != null)
            spawner.reset();
        if (upgradeSystem != null)
            upgradeSystem.reset();
        if (sdgStats != null)
            sdgStats.reset();
        if (gameUI != null)
            gameUI.reset();
        if (popupManager != null && popupManager.isShowing())
            popupManager.closePopup();
        if (currentMinigame != null) {
            currentMinigame.stop();
            currentMinigame = null;
        }
        if (victoryScreen != null && victoryScreen.isActive())
            victoryScreen.stop();

        GameAudio.playBackgroundMusic("bgm.wav", system.VolumeManager.getVolume());
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (popupManager.isShowing()) {
            if (popupManager.getCurrentType() == PopupManager.PopupType.MONSTER) {
                if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
                    popupManager.closePopup();
                    spawner.resumeSpawning();
                }
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.UPGRADE) {
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_3) {
                    upgradeSystem.applyUpgrade(key - KeyEvent.VK_1, player, weaponSystem);
                    popupManager.closePopup();
                    player.triggerInvincibility(500);
                }
            }
            return;
        }

        if (key == KeyEvent.VK_ESCAPE && !popupManager.isShowing() && gameState.equals("playing")
                && !monsterGallery.isActive() && !victoryScreen.isActive()) {
            popupManager.showPauseMenu(() -> {
            }, this::resetGame, this::returnToMainMenu);
            return;
        }

        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.onKeyPress(key);
            if (key == KeyEvent.VK_ESCAPE) {
                if (currentMinigame instanceof TrashSortGame && ((TrashSortGame) currentMinigame).isWin()) {
                    player.setAttackDamage(
                            player.getAttackDamage() + ((TrashSortGame) currentMinigame).getRewardAttackBonus());
                    ((TrashSortGame) currentMinigame).resetReward();
                } else if (currentMinigame instanceof PowerCableGame && ((PowerCableGame) currentMinigame).isWin()) {
                    player.addExperience(player.getExpToNext() - player.getExperience());
                } else if (currentMinigame instanceof FaucetGame && ((FaucetGame) currentMinigame).isWin()) {
                    player.setMaxHealth(player.getMaxHealth() + 10, false);
                    player.heal(((FaucetGame) currentMinigame).getRewardHealAmount());
                }
                currentMinigame.stop();
                player.triggerInvincibility(500);
                currentMinigame = null;
                gameState = "playing";
            }
            return;
        }

        if (key == KeyEvent.VK_W)
            upPressed = true;
        if (key == KeyEvent.VK_S)
            downPressed = true;
        if (key == KeyEvent.VK_A)
            leftPressed = true;
        if (key == KeyEvent.VK_D)
            rightPressed = true;
        if (key == KeyEvent.VK_R && gameState.equals("gameover"))
            resetGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W)
            upPressed = false;
        if (key == KeyEvent.VK_S)
            downPressed = false;
        if (key == KeyEvent.VK_A)
            leftPressed = false;
        if (key == KeyEvent.VK_D)
            rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
        if (victoryScreen.isActive()) {
            victoryScreen.handleMouseMove(e.getX(), e.getY());
            return;
        }
        if (inMainMenu && !monsterGallery.isActive()) {
            mainMenu.handleMouseMove(e.getX(), e.getY());
            repaint();
            return;
        }
        popupManager.updateHover(e.getX(), e.getY());
        if (gameState.equals("in_minigame") && currentMinigame instanceof PowerCableGame) {
            ((PowerCableGame) currentMinigame).handleMouseMove(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
        popupManager.updateHover(e.getX(), e.getY());
        if (gameState.equals("in_minigame") && currentMinigame instanceof PowerCableGame) {
            ((PowerCableGame) currentMinigame).handleMouseMove(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (monsterGallery.isActive()) {
            monsterGallery.handleMouseClick(e.getX(), e.getY());
            return;
        }
        if (victoryScreen.isActive()) {
            victoryScreen.handleMouseClick(e.getX(), e.getY());
            return;
        }
        if (gameState.equals("gameover")) {
            if (gameUI.getGameOverRestartRect() != null && gameUI.getGameOverRestartRect().contains(e.getX(), e.getY()))
                resetGame();
            else if (gameUI.getGameOverMenuRect() != null && gameUI.getGameOverMenuRect().contains(e.getX(), e.getY()))
                returnToMainMenu();
            return;
        }
        if (inMainMenu) {
            mainMenu.handleMousePress(e.getX(), e.getY());
            repaint();
            return;
        }
        if (popupManager.isShowing()) {
            if (popupManager.getCurrentType() == PopupManager.PopupType.MONSTER
                    && popupManager.isCloseButtonClicked(e.getX(), e.getY())) {
                popupManager.closePopup();
                spawner.resumeSpawning();
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.PAUSE) {
                popupManager.handlePauseMenuClick(e.getX(), e.getY());
            } else if (popupManager.getCurrentType() == PopupManager.PopupType.UPGRADE) {
                popupManager.handleUpgradeClick(e.getX(), e.getY());
                player.triggerInvincibility(500);
            }
            return;
        }
        if (gameState.equals("in_minigame") && currentMinigame != null) {
            currentMinigame.handleMouseClick(e.getX(), e.getY());
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (inMainMenu && mainMenu != null && !monsterGallery.isActive()) {
            mainMenu.handleMouseRelease(e.getX(), e.getY());
            return;
        }
        if (popupManager.isShowing() && popupManager.getCurrentType() == PopupManager.PopupType.PAUSE) {
            popupManager.handlePauseMenuRelease();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void startFinalMission() {
        finalMissionActive = true;
        if (portals != null)
            portals.clear();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 400 + random.nextDouble() * 400;
        float deviceX = Math.max(100, Math.min(mapSystem.WORLD_TILES_X * mapSystem.TILE_SIZE - 100,
                player.getX() + (float) (Math.cos(angle) * distance)));
        float deviceY = Math.max(100, Math.min(mapSystem.WORLD_TILES_Y * mapSystem.TILE_SIZE - 100,
                player.getY() + (float) (Math.sin(angle) * distance)));

        purificationDevice = new PurificationDevice(deviceX, deviceY);
        directionArrow = new DirectionArrow();
        purificationTimeAccumulated = 0;
        isPurifying = false;
        if (waveAnnouncement != null)
            waveAnnouncement.showCustomMessage("請前往標記地點，啟動終極淨化裝置！");
    }

    private void updatePurification() {
        if (purificationDevice.isPlayerInRange(player.getX(), player.getY())) {
            long currentTime = System.currentTimeMillis();
            if (!isPurifying) {
                isPurifying = true;
                lastPurificationTime = currentTime;
            } else {
                purificationTimeAccumulated += currentTime - lastPurificationTime;
                lastPurificationTime = currentTime;
                if (purificationTimeAccumulated >= REQUIRED_PURIFICATION_TIME)
                    completeGame();
            }
        } else {
            isPurifying = false;
        }
    }

    private void completeGame() {
        isGameCleared = true;
        finalMissionActive = false;
        if (spawner != null)
            spawner.setGameCleared(true);
        startExplosionAnimation();
    }

    private void startExplosionAnimation() {
        showVictoryScreen();
    }

    private void showVictoryScreen() {
        gameState = "victory";
        victoryScreen.start(0, player.getLevel(), sdgStats.getTotalRecycled(), monsters, this::resetGame,
                this::returnToMainMenu);
    }

    private void resetGame() {
        inMainMenu = false;
        if (player == null)
            player = new Player(mapSystem.getMapCenterX(), mapSystem.getMapCenterY(),
                    () -> upgradeSystem.triggerLevelUp());
        else
            player.reset(mapSystem.getMapCenterX(), mapSystem.getMapCenterY());

        if (monsters != null)
            monsters.clear();
        if (trashes != null)
            trashes.clear();
        if (portals != null)
            portals.clear();
        if (spawner != null)
            spawner.reset();
        if (projectileSystem != null)
            projectileSystem.reset();
        if (upgradeSystem != null)
            upgradeSystem.reset();
        if (weaponSystem != null)
            weaponSystem.reset();
        if (sdgStats != null)
            sdgStats.reset();
        if (gameUI != null)
            gameUI.reset();
        if (victoryScreen != null && victoryScreen.isActive()) {
            victoryScreen.stop();
        }

        gameState = "playing";
        if (currentMinigame != null) {
            currentMinigame.stop();
            currentMinigame = null;
        }

        GameAudio.playBackgroundMusic("bgm.wav", system.VolumeManager.getVolume());

        if (spawner != null)
            spawner.setWaveListener(waveNumber -> {
                if (waveAnnouncement != null)
                    waveAnnouncement.showWave(waveNumber);
            });

        for (int i = 0; i < 3; i++)
            if (spawner != null)
                spawner.spawnMonster();
        if (waveAnnouncement != null)
            waveAnnouncement.showWave(1);

        finalMissionActive = false;
        isGameCleared = false;
        purificationDevice = null;
        directionArrow = null;
        purificationTimeAccumulated = 0;
        isPurifying = false;
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