package javatower.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.*;
import javatower.entities.Tower.TowerType;
import javatower.factories.TowerFactory;
import javatower.systems.Shop;
import javatower.systems.SetBonusManager;
import javatower.util.Constants;
import javatower.util.GameState;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main JavaFX application window — real-time game loop with AnimationTimer.
 */
public class GameGUI extends Application {
    private Stage primaryStage;
    private Scene currentScene;
    private GameBoard gameBoard;
    private Hero hero;
    private javatower.systems.WaveManager waveManager;
    private Shop shop;
    private List<Tower> towers;
    private GameState gameState;
    private AnimationTimer gameLoop;
    private long lastNanoTime;

    // Wave transition
    private double waveDelayTimer = 0;
    private boolean waitingForNextWave = false;

    // Tower placement mode
    private TowerType pendingTowerType = null;

    // Bone piles from dead enemies
    private List<BonePile> bonePiles = new ArrayList<>();

    // Panels
    private HeroPanel heroPanel;
    private WaveInfoPanel waveInfoPanel;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("JavaTower");
        primaryStage.setResizable(false);
        showMainMenu();
        primaryStage.show();
    }

    /**
     * Shows the main menu scene.
     */
    public void showMainMenu() {
        stopGameLoop();
        gameState = GameState.MAIN_MENU;

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(60));
        menuBox.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("JavaTower");
        title.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        title.setStyle("-fx-text-fill: #e94560;");

        Label subtitle = new Label("Tower Defense RPG");
        subtitle.setFont(Font.font("Monospaced", 18));
        subtitle.setStyle("-fx-text-fill: #eee;");

        Button newGameBtn = createMenuButton("New Game");
        newGameBtn.setOnAction(e -> startNewGame());

        Button quitBtn = createMenuButton("Quit");
        quitBtn.setOnAction(e -> primaryStage.close());

        menuBox.getChildren().addAll(title, subtitle, newGameBtn, quitBtn);

        currentScene = new Scene(menuBox, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    /**
     * Starts a new game.
     */
    public void startNewGame() {
        hero = new Hero("Hero");
        hero.setPosition(100, Constants.SCREEN_HEIGHT / 2.0);
        waveManager = new javatower.systems.WaveManager();
        towers = new ArrayList<>();
        bonePiles = new ArrayList<>();
        shop = new Shop();
        gameState = GameState.PLAYING;

        showGameScene();
        startWave();
        startGameLoop();
    }

    /**
     * Shows the main gameplay scene with board + panels.
     */
    private void showGameScene() {
        gameBoard = new GameBoard();
        gameBoard.setHero(hero);
        gameBoard.setTowers(towers);
        gameBoard.setBonePiles(bonePiles);

        gameBoard.setOnMouseClicked(e -> handleBoardClick(e.getX(), e.getY()));

        heroPanel = new HeroPanel(hero);
        waveInfoPanel = new WaveInfoPanel(waveManager);

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #16213e;");
        rightPanel.setPrefWidth(240);
        rightPanel.getChildren().addAll(heroPanel, waveInfoPanel);

        // Action buttons
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(8));
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setStyle("-fx-background-color: #0f3460;");

        Button skillBtn = createActionButton("Skill (Q)");
        skillBtn.setOnAction(e -> useSkill());
        Button shopBtn = createActionButton("Shop");
        shopBtn.setOnAction(e -> showShop());
        Button skillTreeBtn = createActionButton("Skills");
        skillTreeBtn.setOnAction(e -> showSkillTree());
        Button inventoryBtn = createActionButton("Inventory");
        inventoryBtn.setOnAction(e -> showInventory());
        Button forgeBtn = createActionButton("Forge");
        forgeBtn.setOnAction(e -> showForge());

        Button arrowTowerBtn = createActionButton("Arrow T");
        arrowTowerBtn.setOnAction(e -> pendingTowerType = TowerType.ARROW);
        Button magicTowerBtn = createActionButton("Magic T");
        magicTowerBtn.setOnAction(e -> pendingTowerType = TowerType.MAGIC);
        Button siegeTowerBtn = createActionButton("Siege T");
        siegeTowerBtn.setOnAction(e -> pendingTowerType = TowerType.SIEGE);
        Button supportTowerBtn = createActionButton("Sup T");
        supportTowerBtn.setOnAction(e -> pendingTowerType = TowerType.SUPPORT);

        actionBar.getChildren().addAll(skillBtn, shopBtn, skillTreeBtn, inventoryBtn, forgeBtn,
                arrowTowerBtn, magicTowerBtn, siegeTowerBtn, supportTowerBtn);

        BorderPane root = new BorderPane();
        root.setCenter(gameBoard);
        root.setRight(rightPanel);
        root.setBottom(actionBar);
        root.setStyle("-fx-background-color: #1a1a2e;");

        currentScene = new Scene(root);
        primaryStage.setScene(currentScene);
    }

    // ---- Real-time game loop ----

    private void startGameLoop() {
        lastNanoTime = System.nanoTime();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = now;
                // Cap dt to prevent huge jumps
                if (dt > 0.1) dt = 0.1;
                gameUpdate(dt);
                renderBoard();
            }
        };
        gameLoop.start();
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    private void gameUpdate(double dt) {
        if (gameState != GameState.PLAYING) return;

        List<Enemy> enemies = waveManager.getActiveEnemies();

        // Update hero
        hero.update(dt, enemies);

        // Update enemies
        for (Enemy e : enemies) {
            e.update(dt, hero);
        }

        // Update towers
        for (Tower t : towers) {
            t.update(dt, enemies);
        }

        // Remove dead enemies — reward hero and spawn bone piles
        List<Enemy> dead = new ArrayList<>();
        for (Enemy e : enemies) {
            if (!e.isAlive()) dead.add(e);
        }
        for (Enemy e : dead) {
            hero.gainExperience(e.getExperienceValue());
            hero.gainGold(e.getGoldValue());
            // Spawn bone pile at death location
            bonePiles.add(new BonePile(e.getX(), e.getY(), e.getRadius(), e.getType().tier));
            waveManager.onEnemyKilled(e);
        }
        enemies.removeIf(e -> !e.isAlive());

        // Update bone pile ages
        for (BonePile bp : bonePiles) {
            bp.update(dt);
        }

        // Check hero death
        if (!hero.isAlive()) {
            stopGameLoop();
            showGameOver(false);
            return;
        }

        // Check wave complete → auto next wave
        if (waveManager.isWaveComplete() && !waitingForNextWave) {
            if (waveManager.getCurrentWave() >= Constants.MAX_WAVES) {
                stopGameLoop();
                showGameOver(true);
                return;
            }
            waitingForNextWave = true;
            waveDelayTimer = 0;
        }

        if (waitingForNextWave) {
            waveDelayTimer += dt;
            if (waveDelayTimer >= Constants.WAVE_DELAY) {
                waitingForNextWave = false;
                waveManager.nextWave();
                startWave();
            }
        }

        // Refresh panels every few frames (reduce label thrashing)
        refreshPanels();
    }

    // ---- Click handling ----

    private void handleBoardClick(double screenX, double screenY) {
        if (gameState != GameState.PLAYING) return;

        // Tower placement mode
        if (pendingTowerType != null) {
            int[] grid = gameBoard.screenToTowerGrid(screenX, screenY);
            placeTower(pendingTowerType, grid[0], grid[1]);
            pendingTowerType = null;
            return;
        }

        // Click = move hero to that position
        hero.moveTo(screenX, screenY);
    }

    private void useSkill() {
        if (gameState != GameState.PLAYING) return;
        // Skill: damage all enemies in melee range for 1.8× ATK, costs 15 mana
        if (!hero.useMana(15)) return;
        int skillDamage = (int)(hero.getAttack() * 1.8);
        for (Enemy e : waveManager.getActiveEnemies()) {
            if (e.isAlive() && hero.distanceTo(e) <= Constants.MELEE_RANGE + e.getRadius()) {
                e.takeDamage(skillDamage);
            }
        }
    }

    private void placeTower(TowerType type, int gx, int gy) {
        // Bounds check
        if (gx < 0 || gx >= Constants.TILE_COLS || gy < 0 || gy >= Constants.TILE_ROWS) return;

        double[] center = GameBoard.towerGridToPixel(gx, gy);
        double cx = center[0], cy = center[1];

        // Check cell is empty
        for (Tower t : towers) {
            if (Math.abs(t.getX() - cx) < 1 && Math.abs(t.getY() - cy) < 1) return;
        }
        Tower tower = TowerFactory.createTower(type, gx, gy);
        if (tower == null) return;
        // Position at pixel center of grid cell
        tower.setPosition(cx, cy);
        int cost = tower.getUpgradeCost();
        if (hero.getGold() < cost) return;
        hero.spendGold(cost);
        towers.add(tower);
    }

    private void startWave() {
        if (waveManager.getCurrentWave() == 1 && !waveManager.isWaveInProgress()) {
            waveManager.startWave();
        }
        List<Enemy> enemies = waveManager.getActiveEnemies();
        // Position enemies along the right edge, spread vertically
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.setSiblings(enemies);
            enemy.setBonePiles(bonePiles);
            double ex = Constants.SCREEN_WIDTH - 40 - (i % 3) * 40;
            double ey = 60 + (i * 50) % (Constants.SCREEN_HEIGHT - 120);
            enemy.setPosition(ex, ey);
        }
        gameBoard.setEnemies(enemies);
    }

    private void renderBoard() {
        if (gameBoard == null) return;
        GraphicsContext gc = gameBoard.getGraphicsContext2D();
        gc.clearRect(0, 0, gameBoard.getWidth(), gameBoard.getHeight());
        gameBoard.render(gc);

        // Draw wave transition text
        if (waitingForNextWave) {
            gc.setFill(javafx.scene.paint.Color.web("#4ecca3"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
            gc.fillText("Wave " + waveManager.getCurrentWave() + " Complete!",
                    Constants.SCREEN_WIDTH / 2.0 - 140, Constants.SCREEN_HEIGHT / 2.0);
            gc.setFont(Font.font("Monospaced", 16));
            gc.fillText("Next wave in " + String.format("%.0f", Constants.WAVE_DELAY - waveDelayTimer) + "s",
                    Constants.SCREEN_WIDTH / 2.0 - 80, Constants.SCREEN_HEIGHT / 2.0 + 30);
        }
    }

    private void refreshPanels() {
        if (heroPanel != null) heroPanel.refresh();
        if (waveInfoPanel != null) waveInfoPanel.refresh();
    }

    // ---- Secondary screens ----

    public void showShop() {
        stopGameLoop();
        gameState = GameState.SHOPPING;
        shop.refreshStock(waveManager.getCurrentWave());

        ShopPanel shopPanel = new ShopPanel(hero, shop, this);

        currentScene = new Scene(shopPanel, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    public void showInventory() {
        stopGameLoop();
        InventoryPanel invPanel = new InventoryPanel(hero, this);

        currentScene = new Scene(invPanel, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    public void showForge() {
        stopGameLoop();
        ForgePanel forgePanel = new ForgePanel(hero, this);

        currentScene = new Scene(forgePanel, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    public void showSkillTree() {
        stopGameLoop();
        gameState = GameState.SKILL_TREE;

        SkillTreePanel skillTreePanel = new SkillTreePanel(hero, this);

        currentScene = new Scene(skillTreePanel, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    public void returnToGame() {
        gameState = GameState.PLAYING;
        showGameScene();
        if (waveManager.isWaveInProgress()) {
            List<Enemy> enemies = waveManager.getActiveEnemies();
            gameBoard.setEnemies(enemies);
        }
        startGameLoop();
    }

    public void showGameOver(boolean victory) {
        stopGameLoop();
        gameState = victory ? GameState.VICTORY : GameState.GAME_OVER;

        VBox gameOverBox = new VBox(20);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setPadding(new Insets(60));
        gameOverBox.setStyle("-fx-background-color: #1a1a2e;");

        Label resultLabel = new Label(victory ? "VICTORY!" : "GAME OVER");
        resultLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        resultLabel.setStyle(victory ? "-fx-text-fill: #4ecca3;" : "-fx-text-fill: #e94560;");

        Label statsLabel = new Label(String.format("Wave: %d | Level: %d | Gold: %d",
                waveManager.getCurrentWave(), hero.getLevel(), hero.getGold()));
        statsLabel.setFont(Font.font("Monospaced", 16));
        statsLabel.setStyle("-fx-text-fill: #eee;");

        Button mainMenuBtn = createMenuButton("Main Menu");
        mainMenuBtn.setOnAction(e -> showMainMenu());

        gameOverBox.getChildren().addAll(resultLabel, statsLabel, mainMenuBtn);

        currentScene = new Scene(gameOverBox, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        btn.setPrefWidth(200);
        btn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospaced", 12));
        btn.setStyle("-fx-background-color: #533483; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }
}
