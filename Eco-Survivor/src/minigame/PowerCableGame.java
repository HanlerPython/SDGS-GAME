package minigame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PowerCableGame implements MinigameInterface {

    private boolean active;
    private boolean isConnected;
    private int screenWidth;
    private int screenHeight;
    private boolean isFailed;
    private int restartsLeft;
    private long startTime;
    private final int TIME_LIMIT_MS = 15000; // 限10秒

    // 遊戲節點類別
    private static class Node {
        int x, y;
        boolean isPowered;
        String name;

        Node(int x, int y, String name) {
            this.x = x;
            this.y = y;
            this.isPowered = false;
            this.name = name;
        }
    }

    // 障礙物類別
    private static class Obstacle {
        int x, y, radius;
        Obstacle(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    // 連線類別
    private static class Connection {
        Node n1, n2;
        Connection(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
    }

    private List<Node> nodes;
    private List<Obstacle> obstacles;
    private List<Connection> connections;
    private List<Node> activePath = new ArrayList<>();

    private Node selectedNode = null; 
    
    private int currentMouseX;
    private int currentMouseY;

    private int maxWireLength; 
    private final int NODE_RADIUS = 20;

    private final int MAX_WIRES = 5;
    
    // 獨立計算總共消耗的電線數量（退回不返還）
    private int totalWiresUsed = 0; 

    public PowerCableGame() {
        this.active = false;
        this.nodes = new ArrayList<>();
        this.obstacles = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.activePath.clear();
        this.totalWiresUsed = 0;
    }

    public void start(int screenWidth, int screenHeight) {
        this.active = true;
        this.isConnected = false;
        this.isFailed = false;
        this.restartsLeft = 1;
        this.selectedNode = null;
        this.connections.clear();
        this.activePath.clear(); 
        this.totalWiresUsed = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.maxWireLength = (int)(screenWidth * 0.25); 

        this.startTime = System.currentTimeMillis();
        initLevel(this.screenWidth, this.screenHeight);
    }

    private void initLevel(int width, int height) {
        nodes.clear();
        obstacles.clear();

        int cx = width / 2;
        int cy = height / 2;

        //新增節點區塊
        Node source = new Node(cx - (int)(width * 0.25), cy, "SOURCE");
        source.isPowered = true; 
        Node city = new Node(cx + (int)(width * 0.25), cy, "CITY");
        nodes.add(source); 
        nodes.add(city);   

        // 新增主節點
        nodes.add(new Node(cx - (int)(width * 0.22) + randomOffset(40), cy - (int)(height * 0.15) + randomOffset(40), "Node A"));
        nodes.add(new Node(cx - (int)(width * 0.20) + randomOffset(40), cy + (int)(height * 0.18) + randomOffset(40), "Node B"));
        nodes.add(new Node(cx + randomOffset(50),                       cy -  randomOffset(50), "Node C"));
        nodes.add(new Node(cx + (int)(width * 0.20) + randomOffset(40), cy + (int)(height * 0.15) + randomOffset(40), "Node D"));
        nodes.add(new Node(cx + (int)(width * 0.22) + randomOffset(40), cy - (int)(height * 0.18) + randomOffset(40), "Node E"));

        //新增激動節點
        int decoyCount = 2 + (int)(Math.random() * 2); // 2~3個機動節點(其實是用來混淆視聽?我是天才吧!)
        int decoyAttemptsMax = 50;

        for (int i = 0; i < decoyCount; i++) {
            boolean validDecoy = false;
            int attempts = 0;

            while (!validDecoy && attempts < decoyAttemptsMax) {
                // 讓假節點散佈在更廣的範圍
                int decoyX = cx + randomOffset((int)(width * 0.25));
                int decoyY = cy + randomOffset((int)(height * 0.19));

                validDecoy = true;

                // 檢查 A：不要生在障礙物「裡面」，但可以生在障礙物「旁邊」(增加誤導性)
                for (Obstacle obs : obstacles) {
                    if (Math.hypot(decoyX - obs.x, decoyY - obs.y) < (obs.radius + NODE_RADIUS + 5)) {
                        validDecoy = false;
                        break;
                    }
                }

                // 檢查 B：確保節點之間不會互相重疊，導致畫面破圖
                if (validDecoy) {
                    for (Node n : nodes) {
                        if (Math.hypot(decoyX - n.x, decoyY - n.y) < (NODE_RADIUS * 2 + 15)) {
                            validDecoy = false;
                            break;
                        }
                    }
                }

                // 若位置合法，加入名單
                if (validDecoy) {
                    nodes.add(new Node(decoyX, decoyY, "DECOY"));
                }
                attempts++;
            }
        }
        
        
        // 新增障礙物區塊（改進：中心禁區 + 防堆積
        int obstacleCount = 4 + (int)(Math.random() * 2); // 4~5個
        int maxAttempts = 150;
        
        // 定義中心禁區
        int corridorWidth = (int)(width * 0.10); 
        int corridorHeight = (int)(height * 0.30); 

        for (int i = 0; i < obstacleCount; i++) {
            boolean validPosition = false;
            int attempts = 0;
            
            while (!validPosition && attempts < maxAttempts) {
                int obsX = cx + randomOffset((int)(width * 0.22));
                int obsY = cy + randomOffset((int)(height * 0.22));
                int obsR = (int)(height * (0.05 + Math.random() * 0.03));
                
                validPosition = true;
                
                // 檢查 1：不在中心禁區內（這是通路保留區）
                if (Math.abs(obsX - cx) < corridorWidth && Math.abs(obsY - cy) < corridorHeight) {
                    validPosition = false;
                }
                
                // 檢查 2：不要太靠近節點
                if (validPosition) {
                    for (Node n : nodes) {
                        double dist = Math.hypot(n.x - obsX, n.y - obsY);
                        if (dist < (obsR + NODE_RADIUS + 30)) {
                            validPosition = false; 
                            break;
                        }
                    }
                }
                
                // 檢查 3：與其他障礙物保持距離（防止堆積成牆）
                if (validPosition) {
                    for (Obstacle other : obstacles) {
                        double dist = Math.hypot(obsX - other.x, obsY - other.y);
                        // 最小間距 = 兩個半徑之和 + 50px 的緩衝
                        if (dist < (obsR + other.radius + 50)) {
                            validPosition = false;
                            break;
                        }
                    }
                }
                
                if (validPosition) {
                    obstacles.add(new Obstacle(obsX, obsY, obsR));
                }
                attempts++;
            }
        }
    }

    private int randomOffset(int range) {
        return (int)((Math.random() - 0.5) * 2 * range); // +1 * range ~ -1 * range 
    }


    // 遊戲運行區塊
    @Override
    public void stop() {
        this.active = false;
    }

    @Override
    public void update() {
        if (!active) return;
        updatePowerPropagation();
        
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= TIME_LIMIT_MS && !isConnected && !isFailed) {
            this.isFailed = true;
        }

        if (nodes.get(1).isPowered && !isConnected) {   // 連到城市->獲勝
            this.isConnected = true;
            isFailed = false;
        } else if (!nodes.get(1).isPowered && totalWiresUsed >= MAX_WIRES && !isFailed) {   // 線超出限制導致失敗
            this.isFailed = true;
        }
    }

    // 電力傳播
    private void updatePowerPropagation() { 
        for (int i = 1; i < nodes.size(); i++) {
            nodes.get(i).isPowered = false;
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Connection c : connections) {  // 供電，連線沒有方向性所以要互相供電
                if (c.n1.isPowered && !c.n2.isPowered) {   
                    c.n2.isPowered = true;
                    changed = true;
                } else if (c.n2.isPowered && !c.n1.isPowered) {
                    c.n1.isPowered = true;
                    changed = true;
                }
            }
        }
    }

    public void handleMouseMove(int x, int y) {
        if (!active || isConnected || isFailed) return;
        this.currentMouseX = x;
        this.currentMouseY = y;
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY) {
        if (!active || isConnected || isFailed) return;

        Node clickedNode = null;
        for (Node n : nodes) {
            double dist = Math.sqrt(Math.pow(mouseX - n.x, 2) + Math.pow(mouseY - n.y, 2));
            int clickTolerance = (n.name.equals("SOURCE") || n.name.equals("CITY")) ? 40 : (NODE_RADIUS + 10);
            if (dist <= clickTolerance) { 
                clickedNode = n;
                break;
            }
        }

        if (clickedNode != null) {
            
            // 情況一：退回機制（拔除實體線與記憶，但 totalWiresUsed 不會減少！）
            if (activePath.contains(clickedNode)) {
                int targetIndex = activePath.indexOf(clickedNode);
                for (int i = activePath.size() - 1; i > targetIndex; i--) {
                    Node n1 = activePath.get(i);
                    Node n2 = activePath.get(i - 1);
                    removeConnection(n1, n2); 
                    activePath.remove(i);     
                }
                selectedNode = clickedNode; 
            } 
            
            // 情況二：剛開局，點擊起點
            else if (activePath.isEmpty() && clickedNode.isPowered) {
                activePath.add(clickedNode);
                selectedNode = clickedNode;
            } 
            
            // 情況三：手上有拿線，且點擊「不在路徑上」的新節點
            else if (selectedNode != null && clickedNode != selectedNode) {
                // 用 totalWiresUsed 來檢查是否超過上限
                if (totalWiresUsed < MAX_WIRES) {
                    if (tryConnect(selectedNode, clickedNode)) {
                        activePath.add(clickedNode); 
                        selectedNode = clickedNode;  
                        totalWiresUsed++;
                    }
                }
            }
        } else {
            selectedNode = null;
        }
    }

    private void removeConnection(Node n1, Node n2) {
        connections.removeIf(c -> (c.n1 == n1 && c.n2 == n2) || (c.n1 == n2 && c.n2 == n1));
    }

    private boolean tryConnect(Node n1, Node n2) {
        double dist = Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2));
        if (dist > maxWireLength) return false; 

        for (Obstacle obs : obstacles) {
            if (isLineIntersectingCircle(n1.x, n1.y, n2.x, n2.y, obs.x, obs.y, obs.radius)) {
                return false; 
            }
        }

        for (Connection c : connections) {
            if ((c.n1 == n1 && c.n2 == n2) || (c.n1 == n2 && c.n2 == n1)) {
                return false; 
            }
        }

        connections.add(new Connection(n1, n2));
        return true;
    }
    
    private boolean isLineIntersectingCircle(int x1, int y1, int x2, int y2, int cx, int cy, int r) {
        double lineLenSq = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (lineLenSq == 0) return false;

        double t = ((cx - x1) * (x2 - x1) + (cy - y1) * (y2 - y1)) / lineLenSq;
        t = Math.max(0, Math.min(1, t)); 

        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);

        double distToCentSq = Math.pow(projX - cx, 2) + Math.pow(projY - cy, 2);
        return distToCentSq < Math.pow(r, 2);
    }
    
    @Override
    public void onKeyPress(int key){
        if (!active || isConnected) return;
        if (key == 82) { // 82 是 R 鍵
            if (restartsLeft > 0) {
                initLevel(this.screenWidth, this.screenHeight); 
                this.connections.clear();
                this.activePath.clear(); 
                this.totalWiresUsed = 0;
                this.selectedNode = null;
                this.restartsLeft--;  
        
            }
        }
    }
    
    // 繪圖區塊
    @Override
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(20, 25, 35));
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isConnected && !isFailed) {
            long elapsed = System.currentTimeMillis() - startTime;
            double timeRatio = Math.max(0, 1.0 - ((double) elapsed / TIME_LIMIT_MS));
            
            // 根據剩餘時間變色（綠 -> 黃 -> 紅）
            Color timerColor = Color.GREEN;
            if (timeRatio < 0.3) timerColor = Color.RED;
            else if (timeRatio < 0.6) timerColor = Color.YELLOW;
            
            g.setColor(timerColor);
            g.fillRect(0, 0, (int)(screenWidth * timeRatio), 6); // 在螢幕最頂端畫一條 6 像素高的進度條
        }

        if (isConnected) {
            drawCenteredString(g, "SDG 7: RENEWABLE ENERGY", screenWidth, 150, new Font("Arial", Font.BOLD, 36), new Color(255, 200, 50));
            drawCenteredString(g, "SUCCESS! ENERGY NETWORK FULLY CONNECTED!", screenWidth, 250, new Font("Arial", Font.BOLD, 28), new Color(255, 220, 100));
            drawCenteredString(g, "Reward: Level Up!", screenWidth, 350, new Font("Arial", Font.BOLD, 24), Color.WHITE);
            drawCenteredString(g, "Press ESC to Return to Battlefield", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 18), Color.GRAY);
        } else if (isFailed) {
            drawCenteredString(g, "SDG 7: RENEWABLE ENERGY", screenWidth, 150, new Font("Arial", Font.BOLD, 36), new Color(255, 200, 50));
            drawCenteredString(g, "FAILURE! GRID LOST CONNECTION", screenWidth, 250, new Font("Arial", Font.BOLD, 28), new Color(255, 80, 80));
            drawCenteredString(g, "Reward: None", screenWidth, 350, new Font("Arial", Font.BOLD, 24), Color.WHITE);
            drawCenteredString(g, "Press ESC to Return to Battlefield", screenWidth, screenHeight - 80, new Font("Arial", Font.PLAIN, 18), Color.GRAY);
        } else {
            g.setColor(new Color(0, 220, 255));
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("SDG 7: GRID CONSTRUCT", screenWidth / 2 - 240, 80);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Rules: Click nodes to deploy wires. Connect SOURCE to CITY within 5 wires!", screenWidth / 2 - 340, 120);

            // 顯示獨立的 totalWiresUsed
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.setColor(totalWiresUsed >= MAX_WIRES ? Color.RED : Color.YELLOW);
            g.drawString("Wires Used: " + totalWiresUsed + " / " + MAX_WIRES, screenWidth / 2 - 100, 160);

            // 1. 繪製障礙物
            for (Obstacle obs : obstacles) {
                g.setColor(new Color(180, 50, 50, 150));
                g.fillOval(obs.x - obs.radius, obs.y - obs.radius, obs.radius * 2, obs.radius * 2);
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(2));
                g.drawOval(obs.x - obs.radius, obs.y - obs.radius, obs.radius * 2, obs.radius * 2);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("HIGH RESISTANCE", obs.x - 60, obs.y + 5);
            }

            // 2. 繪製預覽線 (Ghost Wire)
            if (selectedNode != null && active) {
                double dist = Math.sqrt(Math.pow(selectedNode.x - currentMouseX, 2) + Math.pow(selectedNode.y - currentMouseY, 2));
                boolean tooFar = dist > maxWireLength;
                boolean hitObstacle = false;
                for (Obstacle obs : obstacles) {
                    if (isLineIntersectingCircle(selectedNode.x, selectedNode.y, currentMouseX, currentMouseY, obs.x, obs.y, obs.radius)) {
                        hitObstacle = true;
                        break;
                    }
                }
                
                // 用 totalWiresUsed 判斷是否沒電線了
                boolean outOfWire = totalWiresUsed >= MAX_WIRES;

                if (tooFar || hitObstacle || outOfWire) {
                    g.setColor(new Color(255, 50, 50, 180)); 
                } else {
                    g.setColor(new Color(0, 255, 255, 180)); 
                }

                Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{10}, 0);
                g.setStroke(dashed);
                g.drawLine(selectedNode.x, selectedNode.y, currentMouseX, currentMouseY);
                
                g.setFont(new Font("Arial", Font.BOLD, 12));
                if (outOfWire) {
                    g.drawString("OUT OF WIRE", currentMouseX + 15, currentMouseY);
                } else if (tooFar) {
                    g.drawString("TOO LONG", currentMouseX + 15, currentMouseY);
                } else if (hitObstacle) {
                    g.drawString("BLOCKED", currentMouseX + 15, currentMouseY);
                }
            }

            // 3. 繪製已接好的電線
            g.setStroke(new BasicStroke(4));
            for (Connection c : connections) {
                g.setColor(c.n1.isPowered && c.n2.isPowered ? Color.GREEN : Color.DARK_GRAY);
                g.drawLine(c.n1.x, c.n1.y, c.n2.x, c.n2.y);
            }

            // 4. 繪製節點
            for (Node n : nodes) {
                if (n.name.equals("SOURCE")) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(n.x - 30, n.y - 30, 60, 60);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.drawString("GEN", n.x - 14, n.y + 5);
                } else if (n.name.equals("CITY")) {
                    g.setColor(n.isPowered ? Color.GREEN : Color.GRAY);
                    g.fillRect(n.x - 30, n.y - 30, 60, 60);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.drawString("CITY", n.x - 14, n.y + 5);
                } else {
                    g.setColor(n.isPowered ? Color.GREEN : Color.LIGHT_GRAY);
                    g.fillOval(n.x - NODE_RADIUS, n.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
                }

                if (selectedNode == n) {
                    g.setColor(Color.CYAN);
                    g.setStroke(new BasicStroke(3));
                    if (n.name.equals("SOURCE") || n.name.equals("CITY")) {
                        g.drawRect(n.x - 35, n.y - 35, 70, 70);
                    } else {
                        g.drawOval(n.x - NODE_RADIUS - 5, n.y - NODE_RADIUS - 5, (NODE_RADIUS + 5) * 2, (NODE_RADIUS + 5) * 2);
                    }
                }
            }

            // 5. 底部操作資訊
            g.setStroke(new BasicStroke(1)); 
            Font bottomFont = new Font("Arial", Font.PLAIN, 16);
            
            if (restartsLeft > 0) {
                drawCenteredString(g, "Press ESC to Return | If no possible path -> Press R to Restart (Left: " + restartsLeft + ")", 
                                   screenWidth, screenHeight - 60, bottomFont, Color.GRAY);
            } else {
                drawCenteredString(g, "Press ESC to Return | No Restarts Left", 
                                   screenWidth, screenHeight - 60, bottomFont, Color.GRAY);
            }
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
    public boolean isActive() { return active; }
    @Override
    public boolean isWin() { return isConnected; }
}