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
import javatower.entities.towers.SupportTower;
import javatower.factories.TowerFactory;
import javatower.systems.Shop;
import javatower.systems.SetBonusManager;
import javatower.database.DatabaseManager;
import javatower.util.Constants;
import javatower.util.GameState;
import javatower.util.Logger;
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

    // Selected tower for upgrading
    private Tower selectedTower = null;

    // Tower boost tracking
    private double towerBoostTimer = 0;
    private List<Tower> boostedTowers = new ArrayList<>();
    private List<Integer> boostedOriginalDamage = new ArrayList<>();

    // Bone piles from dead enemies
    private List<BonePile> bonePiles = new ArrayList<>();

    // Ability cooldown timers (#52)
    private double skillCooldownTimer = 0;
    private double specialCooldownTimer = 0;
    private double healCooldownTimer = 0;
    private static final double SKILL_COOLDOWN = 1.5;
    private static final double SPECIAL_COOLDOWN = 3.0;
    private static final double HEAL_COOLDOWN = 4.0;
    private static final double BOOST_COOLDOWN = 5.0;

    // Run timer (#75)
    private double runTime = 0;

    // Tower sell confirmation (#40) - requires double-press S within 2 seconds
    private boolean sellPending = false;
    private double sellConfirmTimer = 0;
    private static final double SELL_CONFIRM_WINDOW = 2.0;

    // Panels
    private HeroPanel heroPanel;
    private WaveInfoPanel waveInfoPanel;
    private CombatLogPanel combatLogPanel;

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
        subtitle.setFont(Font.font("Monospaced", 24));
        subtitle.setStyle("-fx-text-fill: #eee;");

        Button newGameBtn = createMenuButton("New Game");
        newGameBtn.setOnAction(e -> startNewGame());

        Button loadGameBtn = createMenuButton("Load Game");
        loadGameBtn.setOnAction(e -> loadSavedGame());
        try {
            loadGameBtn.setDisable(!DatabaseManager.getInstance().hasSaveFile());
        } catch (Exception ex) {
            loadGameBtn.setDisable(true);
        }

        Button quitBtn = createMenuButton("Quit");
        quitBtn.setOnAction(e -> primaryStage.close());

        menuBox.getChildren().addAll(title, subtitle, newGameBtn, loadGameBtn, quitBtn);

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

        // Initialize meta_progression row if first run
        try {
            DatabaseManager.getInstance().saveMetaProgression(0, 0,
                    hero.getInventory().getWidth(), hero.getInventory().getHeight());
        } catch (Exception ex) { /* DB optional */ }

        showGameScene();
        startWave();
        startGameLoop();
    }

    /**
     * Loads a saved game from the database.
     */
    public void loadSavedGame() {
        DatabaseManager db = DatabaseManager.getInstance();
        Hero loaded = db.loadGame();
        if (loaded == null) return;

        hero = loaded;
        int savedWave = db.loadWave();
        waveManager = new javatower.systems.WaveManager();
        // Advance wave manager to saved wave
        while (waveManager.getCurrentWave() < savedWave) {
            waveManager.nextWave();
        }
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
        combatLogPanel = new CombatLogPanel();

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #16213e;");
        rightPanel.setPrefWidth(280);
        rightPanel.getChildren().addAll(heroPanel, waveInfoPanel, combatLogPanel);

        // ===== LEFT SIDEBAR WITH CLICKABLE ICONS =====
        VBox leftSidebar = new VBox(5);
        leftSidebar.setPadding(new Insets(8));
        leftSidebar.setStyle("-fx-background-color: #0f3460; -fx-border-color: #1a1a2e; -fx-border-width: 0 2 0 0;");
        leftSidebar.setPrefWidth(200);
        leftSidebar.setMinWidth(180);
        
        // Section: TOWERS
        Label towerLabel = createSectionLabel("🛡️ TOWERS");
        Button arrowTowerBtn = createSidebarButton("🏹 Arrow", "1", "#48bb78");
        arrowTowerBtn.setOnAction(e -> setTowerPlacementMode(TowerType.ARROW));
        Button magicTowerBtn = createSidebarButton("✨ Magic", "2", "#9f7aea");
        magicTowerBtn.setOnAction(e -> setTowerPlacementMode(TowerType.MAGIC));
        Button siegeTowerBtn = createSidebarButton("💥 Siege", "3", "#f56565");
        siegeTowerBtn.setOnAction(e -> setTowerPlacementMode(TowerType.SIEGE));
        Button supportTowerBtn = createSidebarButton("💚 Support", "4", "#4fd1c5");
        supportTowerBtn.setOnAction(e -> setTowerPlacementMode(TowerType.SUPPORT));
        Button upgradeTowerBtn = createSidebarButton("⬆️ Upgrade", "T", "#ed8936");
        upgradeTowerBtn.setOnAction(e -> upgradeTower());
        Button sellTowerBtn = createSidebarButton("💰 Sell", "S", "#a0aec0");
        sellTowerBtn.setOnAction(e -> sellTower());
        
        leftSidebar.getChildren().addAll(towerLabel, arrowTowerBtn, magicTowerBtn, siegeTowerBtn, 
                supportTowerBtn, upgradeTowerBtn, sellTowerBtn);
        
        // Separator
        leftSidebar.getChildren().add(createSeparator());
        
        // Section: ABILITIES
        Label abilityLabel = createSectionLabel("⚔️ ABILITIES");
        Button slashBtn = createSidebarButton("⚔️ Slash", "Q", "#e94560");
        slashBtn.setOnAction(e -> useSkill());
        Button novaBtn = createSidebarButton("🔥 Nova", "W", "#dd6b20");
        novaBtn.setOnAction(e -> useSpecialAbility());
        Button healBtn = createSidebarButton("💗 Heal", "E", "#48bb78");
        healBtn.setOnAction(e -> selfHeal());
        Button boostBtn = createSidebarButton("⚡ Boost", "R", "#ecc94b");
        boostBtn.setOnAction(e -> towerBoost());
        Button rollBtn = createSidebarButton("💨 Roll", "SHIFT", "#63b3ed");
        rollBtn.setOnAction(e -> hero.rollInMovementDirection());
        Button ultimateBtn = createSidebarButton("🔥 RAGE", "F", "#dc2626");
        ultimateBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12; -fx-pref-width: 180;");
        ultimateBtn.setOnAction(e -> useUltimateAbility());
        
        leftSidebar.getChildren().addAll(abilityLabel, slashBtn, novaBtn, healBtn, boostBtn, rollBtn, ultimateBtn);
        
        // Separator
        leftSidebar.getChildren().add(createSeparator());
        
        // Section: MENU
        Label menuLabel = createSectionLabel("☰ MENU");
        Button shopBtn = createSidebarButton("🛒 Shop", "TAB", "#533483");
        shopBtn.setOnAction(e -> showShop());
        Button skillTreeBtn = createSidebarButton("🌳 Skills", "", "#38a169");
        skillTreeBtn.setOnAction(e -> showSkillTree());
        Button inventoryBtn = createSidebarButton("🎒 Items", "", "#3182ce");
        inventoryBtn.setOnAction(e -> showInventory());
        Button forgeBtn = createSidebarButton("🔨 Forge", "", "#d69e2e");
        forgeBtn.setOnAction(e -> showForge());
        Button mapBtn = createSidebarButton("🗺️ Map", "M", "#718096");
        mapBtn.setOnAction(e -> gameBoard.getMiniMap().toggle());
        Button pauseBtn = createSidebarButton("⏸️ Pause", "ESC", "#4a5568");
        pauseBtn.setOnAction(e -> togglePause());
        Button fullscreenBtn = createSidebarButton("🖥️ Fullscreen", "F11", "#2d3748");
        fullscreenBtn.setOnAction(e -> toggleFullscreen());
        
        leftSidebar.getChildren().addAll(menuLabel, shopBtn, skillTreeBtn, inventoryBtn, forgeBtn, mapBtn, pauseBtn, fullscreenBtn);

        BorderPane root = new BorderPane();
        root.setCenter(gameBoard);
        root.setRight(rightPanel);
        root.setLeft(leftSidebar);
        root.setStyle("-fx-background-color: #1a1a2e;");

        currentScene = new Scene(root);
        
        // Setup keyboard hotkeys
        setupHotkeys(currentScene);
        
        primaryStage.setScene(currentScene);
    }
    
    /**
     * Sets up keyboard hotkeys for the game.
     */
    private void setupHotkeys(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (gameState != GameState.PLAYING) return;
            
            switch (e.getCode()) {
                // Tower placement hotkeys
                case DIGIT1:
                case NUMPAD1:
                    setTowerPlacementMode(TowerType.ARROW);
                    break;
                case DIGIT2:
                case NUMPAD2:
                    setTowerPlacementMode(TowerType.MAGIC);
                    break;
                case DIGIT3:
                case NUMPAD3:
                    setTowerPlacementMode(TowerType.SIEGE);
                    break;
                case DIGIT4:
                case NUMPAD4:
                    setTowerPlacementMode(TowerType.SUPPORT);
                    break;
                
                // Ability hotkeys
                case Q:
                    useSkill();
                    break;
                case W:
                    useSpecialAbility();
                    break;
                
                // Utility hotkeys
                case ESCAPE:
                    // Cancel tower placement OR toggle pause
                    if (pendingTowerType != null) {
                        pendingTowerType = null;
                        gameBoard.clearPendingTowerType();
                        selectedTower = null;
                    } else {
                        togglePause();
                    }
                    break;
                case TAB:
                    // Toggle shop
                    showShop();
                    break;
                case SPACE:
                    // Use inventory/potion (placeholder)
                    break;
                case M:
                    // Toggle mini-map
                    gameBoard.getMiniMap().toggle();
                    break;
                
                // Arrow key movement
                case UP:
                    hero.setMoveUp(true);
                    break;
                case DOWN:
                    hero.setMoveDown(true);
                    break;
                case LEFT:
                    hero.setMoveLeft(true);
                    break;
                case RIGHT:
                    hero.setMoveRight(true);
                    break;
                
                // Additional abilities
                case E:
                    selfHeal();
                    break;
                case R:
                    towerBoost();
                    break;
                case S:
                    sellTower();
                    break;
                case SHIFT:
                    // Dodge/Roll in current movement direction
                    if (hero.rollInMovementDirection()) {
                        Logger.info("Hero rolled!");
                    }
                    break;
                case F:
                    // Ultimate Ability
                    useUltimateAbility();
                    break;
                case F11:
                    // Toggle fullscreen
                    toggleFullscreen();
                    break;
                default:
                    break;
            }
        });
        
        // Key released — stop arrow-key movement
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP:    hero.setMoveUp(false); break;
                case DOWN:  hero.setMoveDown(false); break;
                case LEFT:  hero.setMoveLeft(false); break;
                case RIGHT: hero.setMoveRight(false); break;
                default: break;
            }
        });
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

        // Snapshot hero HP to detect enemy hits
        int heroPrevHP = hero.getCurrentHealth();

        // Update hero
        hero.update(dt, enemies);

        // Spawn hero attack effects
        if (hero.didAttackThisFrame()) {
            Enemy target = hero.getLastAttackTarget();
            if (target != null) {
                spawnHeroAttackEffect(target);
                // Damage number
                gameBoard.addEffect(VisualEffect.createDamageNumber(
                        target.getX(), target.getY(), hero.getLastDamageDealt(), hero.wasAttackCrit()));
                
                // Screen shake on critical hit
                if (hero.wasAttackCrit()) {
                    gameBoard.applyScreenShake(5.0, 0.2);
                }
                
                // Ultimate charge for dealing damage
                addUltimateCharge(hero.getLastDamageDealt() * 0.1); // 10% of damage dealt
            }
            hero.clearFrameFlags();
        }

        // Update enemies
        for (Enemy e : enemies) {
            e.update(dt, hero);
        }

        // Detect enemy damage on hero (HP dropped)
        int heroHPLost = heroPrevHP - hero.getCurrentHealth();
        if (heroHPLost > 0) {
            gameBoard.addEffect(VisualEffect.createDamageNumber(
                    hero.getX(), hero.getY(), heroHPLost, false));
            // Ultimate charge for taking damage
            addUltimateCharge(heroHPLost * 0.2); // 20% of damage taken
        }

        // Update towers + spawn tower projectiles
        for (Tower t : towers) {
            t.update(dt, enemies);
            if (t.didFireThisFrame() && t.getLastTarget() != null) {
                Enemy tgt = t.getLastTarget();
                switch (t.getType()) {
                    case ARROW:
                        gameBoard.addEffect(VisualEffect.createTowerArrow(t.getX(), t.getY(), tgt.getX(), tgt.getY()));
                        break;
                    case MAGIC:
                        gameBoard.addEffect(VisualEffect.createTowerMagic(t.getX(), t.getY(), tgt.getX(), tgt.getY()));
                        break;
                    case SIEGE:
                        gameBoard.addEffect(VisualEffect.createTowerSiege(t.getX(), t.getY(), tgt.getX(), tgt.getY()));
                        break;
                    default: break;
                }
            }
            // Support towers heal the hero each cycle
            if (t instanceof SupportTower) {
                ((SupportTower) t).supportHero(hero);
            }
        }

        // Update visual effects
        gameBoard.updateEffects(dt);

        // Update ability cooldowns (#52)
        if (skillCooldownTimer > 0) skillCooldownTimer -= dt;
        if (specialCooldownTimer > 0) specialCooldownTimer -= dt;
        if (healCooldownTimer > 0) healCooldownTimer -= dt;
        
        // Update ultimate ability
        if (ultimateActive) {
            ultimateDuration -= dt;
            if (ultimateDuration <= 0) {
                ultimateActive = false;
                hero.setUltimateActive(false);
                Logger.info("RAGE MODE ended");
            }
        }

        // Sell confirmation timer (#40)
        if (sellPending) {
            sellConfirmTimer -= dt;
            if (sellConfirmTimer <= 0) {
                sellPending = false;
                sellConfirmTimer = 0;
            }
        }

        // Update run timer (#75)
        runTime += dt;

        // Update tower boost timer
        if (towerBoostTimer > 0) {
            towerBoostTimer -= dt;
            if (towerBoostTimer <= 0) {
                // Restore original damage values
                for (int i = 0; i < boostedTowers.size(); i++) {
                    boostedTowers.get(i).setDamage(boostedOriginalDamage.get(i));
                }
                boostedTowers.clear();
                boostedOriginalDamage.clear();
            }
        }

        // Remove dead enemies — reward hero and spawn bone piles
        List<Enemy> dead = new ArrayList<>();
        for (Enemy e : enemies) {
            if (!e.isAlive()) dead.add(e);
        }
        for (Enemy e : dead) {
            // Death dissolve animation (#4)
            javafx.scene.paint.Color deathColor;
            switch (e.getType().tier) {
                case 1: case 2: deathColor = javafx.scene.paint.Color.web("#c4c4c4"); break;
                case 3: case 4: deathColor = javafx.scene.paint.Color.web("#e94560"); break;
                case 5: case 6: deathColor = javafx.scene.paint.Color.web("#a855f7"); break;
                case 7: case 8: deathColor = javafx.scene.paint.Color.web("#f97316"); break;
                case 9: deathColor = javafx.scene.paint.Color.web("#eab308"); break;
                case 10: deathColor = javafx.scene.paint.Color.web("#dc2626"); break;
                default: deathColor = javafx.scene.paint.Color.GRAY; break;
            }
            gameBoard.addEffect(VisualEffect.createDeathDissolve(e.getX(), e.getY(), e.getRadius(), deathColor));

            hero.gainExperience(e.getExperienceValue());
            hero.gainGold(e.getGoldValue());
            hero.recordKill(e.getMaxHealth(), e.getGoldValue(), e.getExperienceValue());
            
            // Ultimate charge for kills (more for elites/bosses)
            double killCharge = e.isElite() ? 15.0 : 5.0;
            if (e.isBoss() || e.isMiniBoss()) killCharge = 25.0;
            addUltimateCharge(killCharge);
            
            // Gold pickup floating text (#59)
            if (e.getGoldValue() > 0) {
                gameBoard.addEffect(VisualEffect.createGoldPickup(e.getX(), e.getY(), e.getGoldValue()));
            }
            
            // Combat log entry
            if (combatLogPanel != null) {
                combatLogPanel.logKill(e.getType().name(), e.getExperienceValue(), e.getGoldValue());
            }

            // Level-up flash effect (#8)
            if (hero.didLevelUpThisFrame()) {
                gameBoard.addEffect(VisualEffect.createLevelUp(hero.getX(), hero.getY(), hero.getLevel()));
                gameBoard.applyScreenShake(4.0, 0.3);
                if (combatLogPanel != null) {
                    combatLogPanel.logLevelUp(hero.getLevel());
                }
                hero.clearFrameFlags();
            }

            // Item drops from enemies (#11 + #41)
            Item drop = javatower.factories.ItemFactory.createItemDrop(e);
            if (drop != null) {
                if (!hero.getInventory().addItem(drop)) {
                    // Inventory full — item lost (future: ground drops)
                }
            }

            // Visual: gold + XP pickup
            gameBoard.addEffect(VisualEffect.createGoldPickup(e.getX(), e.getY(), e.getGoldValue()));
            gameBoard.addEffect(VisualEffect.createImpactBurst(e.getX(), e.getY(), javafx.scene.paint.Color.web("#e94560")));
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
            try { DatabaseManager.getInstance().updateMaxWave(waveManager.getCurrentWave()); }
            catch (Exception ex) { /* DB optional */ }
            showGameOver(false);
            return;
        }

        // Check wave complete → auto next wave
        if (waveManager.isWaveComplete() && !waitingForNextWave) {
            // Auto-save on wave complete
            try {
                DatabaseManager.getInstance().saveGame(hero, waveManager.getCurrentWave());
                DatabaseManager.getInstance().updateMaxWave(waveManager.getCurrentWave());
            } catch (Exception ex) { /* DB optional */ }

            if (waveManager.getCurrentWave() >= Constants.MAX_WAVES) {
                stopGameLoop();
                showGameOver(true);
                return;
            }
            waitingForNextWave = true;
            waveDelayTimer = 0;
            // Start countdown timer in UI
            if (waveInfoPanel != null) {
                waveInfoPanel.startCountdown(Constants.WAVE_DELAY);
            }
        }

        if (waitingForNextWave) {
            waveDelayTimer += dt;
            // Update countdown UI
            if (waveInfoPanel != null) {
                waveInfoPanel.updateCountdown(dt);
            }
            if (waveDelayTimer >= Constants.WAVE_DELAY) {
                waitingForNextWave = false;
                if (waveInfoPanel != null) {
                    waveInfoPanel.stopCountdown();
                }
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
            gameBoard.clearPendingTowerType();
            return;
        }

        // Check if clicking on an existing tower → select or cycle targeting mode (#31)
        for (Tower t : towers) {
            if (Math.abs(t.getX() - screenX) < 32 && Math.abs(t.getY() - screenY) < 32) {
                if (t == selectedTower) {
                    // Already selected — cycle targeting mode
                    t.cycleTargetMode();
                } else {
                    selectedTower = t;
                }
                return;
            }
        }
        selectedTower = null;

        // Click = move hero to that position
        hero.moveTo(screenX, screenY);
    }

    /**
     * Upgrades the currently selected tower if the hero can afford it.
     */
    private void upgradeTower() {
        if (selectedTower == null) return;
        if (selectedTower.getUpgradeLevel() >= 3) return;
        int cost = selectedTower.getUpgradeCost();
        if (hero.spendGold(cost)) {
            selectedTower.upgrade();
        }
        selectedTower = null;
    }
    
    /**
     * Sets tower placement mode with visual preview.
     */
    private void setTowerPlacementMode(TowerType type) {
        this.pendingTowerType = type;
        gameBoard.setPendingTowerType(type);
    }

    /**
     * Spawns visual attack effects based on the hero's equipped weapon class.
     */
    private void spawnHeroAttackEffect(Enemy target) {
        double hx = hero.getX(), hy = hero.getY();
        double tx = target.getX(), ty = target.getY();
        Item.WeaponClass wc = hero.getLastAttackWeaponClass();
        switch (wc) {
            case RANGED:
                gameBoard.addEffect(VisualEffect.createArrow(hx, hy, tx, ty));
                break;
            case NECROMANCY:
                gameBoard.addEffect(VisualEffect.createNecroBolt(hx, hy, tx, ty));
                break;
            case HOLY:
                gameBoard.addEffect(VisualEffect.createHolySmite(hx, hy, tx, ty));
                break;
            case MELEE:
            default:
                double angle = Math.atan2(ty - hy, tx - hx);
                gameBoard.addEffect(VisualEffect.createMeleeSlash(tx, ty, angle));
                break;
        }
    }

    /**
     * Toggles pause state. Shows pause overlay when paused.
     */
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            stopGameLoop();
            showPauseOverlay();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            hidePauseOverlay();
            startGameLoop();
        }
    }
    
    /**
     * Toggles fullscreen mode.
     */
    private void toggleFullscreen() {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }
    
    /**
     * Shows the pause overlay with resume/quit options.
     */
    private void showPauseOverlay() {
        // Create semi-transparent overlay
        VBox pauseBox = new VBox(20);
        pauseBox.setAlignment(Pos.CENTER);
        pauseBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        pauseBox.setPrefSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        
        Label pauseLabel = new Label("PAUSED");
        pauseLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        pauseLabel.setStyle("-fx-text-fill: #e94560;");
        
        Button resumeBtn = createMenuButton("Resume (ESC)");
        resumeBtn.setOnAction(e -> togglePause());
        
        Button saveBtn = createMenuButton("Save Game");
        saveBtn.setOnAction(e -> showSaveMenu());
        
        Button quitBtn = createMenuButton("Quit to Menu");
        quitBtn.setOnAction(e -> showMainMenu());
        
        pauseBox.getChildren().addAll(pauseLabel, resumeBtn, saveBtn, quitBtn);
        
        // Add to scene
        BorderPane root = (BorderPane) currentScene.getRoot();
        root.setCenter(pauseBox);
    }
    
    /**
     * Hides pause overlay and restores game board.
     */
    private void hidePauseOverlay() {
        BorderPane root = (BorderPane) currentScene.getRoot();
        root.setCenter(gameBoard);
    }
    
    /**
     * Quick save to auto-save slot (slot 0).
     */
    private void showSaveMenu() {
        javatower.systems.SaveGameManager saveManager = new javatower.systems.SaveGameManager();
        boolean saved = saveManager.autoSave(hero, towers, waveManager.getCurrentWave(), 0);
        if (saved) {
            Logger.info("Game saved to auto-save slot");
            if (combatLogPanel != null) {
                combatLogPanel.addEntry("💾 Game saved!");
            }
        } else {
            Logger.error("Failed to save game");
        }
    }

    private void useSkill() {
        if (gameState != GameState.PLAYING && gameState != GameState.PAUSED) return;
        if (skillCooldownTimer > 0) return;
        // Skill: damage all enemies in melee range for 1.8× ATK, costs 15 mana
        if (!hero.useMana(15)) return;
        skillCooldownTimer = SKILL_COOLDOWN;
        int skillDamage = (int)(hero.getAttack() * 1.8);
        for (Enemy e : waveManager.getActiveEnemies()) {
            if (e.isAlive() && hero.distanceTo(e) <= Constants.MELEE_RANGE + e.getRadius()) {
                e.takeDamage(skillDamage);
            }
        }
    }
    
    /**
     * Special ability (W key): Ranged AOE attack at hero position.
     * Costs 25 mana, deals 2.0× ATK in a larger radius.
     */
    private void useSpecialAbility() {
        if (gameState != GameState.PLAYING) return;
        if (specialCooldownTimer > 0) return;
        if (!hero.useMana(25)) return;
        specialCooldownTimer = SPECIAL_COOLDOWN;
        
        int damage = (int)(hero.getAttack() * 2.0);
        double range = Constants.MELEE_RANGE * 2.5;
        
        // Visual effect - expanding ring
        gameBoard.addEffect(VisualEffect.createFireSplash(hero.getX(), hero.getY()));
        
        for (Enemy e : waveManager.getActiveEnemies()) {
            if (e.isAlive() && hero.distanceTo(e) <= range + e.getRadius()) {
                e.takeDamage(damage);
            }
        }
    }

    /**
     * Self heal ability (E key): Heal 30% of max HP, costs 20 mana.
     */
    private void selfHeal() {
        if (gameState != GameState.PLAYING) return;
        if (healCooldownTimer > 0) return;
        if (!hero.useMana(20)) return;
        healCooldownTimer = HEAL_COOLDOWN;
        int healAmount = (int)(hero.getEffectiveMaxHealth() * 0.3);
        hero.heal(healAmount);
        gameBoard.addEffect(VisualEffect.createHealNumber(hero.getX(), hero.getY(), healAmount));
    }

    /**
     * Tower boost ability (R key): Double all tower damage for 5 seconds, costs 25 mana.
     */
    private void towerBoost() {
        if (gameState != GameState.PLAYING) return;
        if (towers.isEmpty() || towerBoostTimer > 0) return;
        if (!hero.useMana(25)) return;
        towerBoostTimer = 5.0;
        boostedTowers.clear();
        boostedOriginalDamage.clear();
        for (Tower t : towers) {
            boostedTowers.add(t);
            boostedOriginalDamage.add(t.getDamage());
            t.setDamage(t.getDamage() * 2);
        }
        gameBoard.applyScreenShake(3.0, 0.15);
    }
    
    // ========== ULTIMATE ABILITY ==========
    private double ultimateMeter = 0; // 0-100 rage meter
    private static final double ULTIMATE_COST = 100.0;
    private boolean ultimateActive = false;
    private double ultimateDuration = 0;
    private static final double ULTIMATE_DURATION = 10.0; // 10 seconds of rage
    
    /**
     * Ultimate ability (F key): Hero enters RAGE mode when meter is full.
     * RAGE: +50% damage, +50% attack speed, +30% movement speed, damage reduction 50%
     */
    private void useUltimateAbility() {
        if (gameState != GameState.PLAYING) return;
        if (ultimateMeter < ULTIMATE_COST) {
            Logger.info("Ultimate not ready! Meter: " + (int)ultimateMeter + "/" + (int)ULTIMATE_COST);
            return;
        }
        
        // Activate RAGE mode
        ultimateMeter = 0;
        ultimateActive = true;
        hero.setUltimateActive(true);
        ultimateDuration = ULTIMATE_DURATION;
        Logger.info("ULTIMATE ACTIVATED! RAGE MODE!");
        gameBoard.applyScreenShake(5.0, 0.3);
    }
    
    /**
     * Adds rage to the ultimate meter.
     * Gained from: dealing damage, taking damage, killing enemies.
     */
    public void addUltimateCharge(double amount) {
        if (ultimateActive) return; // Can't charge while active
        ultimateMeter = Math.min(ULTIMATE_COST, ultimateMeter + amount);
    }
    
    public double getUltimateMeter() { return ultimateMeter; }
    public double getUltimateMax() { return ULTIMATE_COST; }
    public boolean isUltimateActive() { return ultimateActive; }
    public double getUltimateDurationPercent() { 
        return ultimateActive ? (ultimateDuration / ULTIMATE_DURATION) : 0; 
    }

    /**
     * Sell the selected tower (S key): First press shows confirm, second press sells.
     */
    private void sellTower() {
        if (gameState != GameState.PLAYING) return;
        if (selectedTower == null) return;

        // First press: show confirm prompt
        if (!sellPending) {
            sellPending = true;
            sellConfirmTimer = SELL_CONFIRM_WINDOW;
            return;
        }

        // Second press within window: actually sell
        int refund = selectedTower.getUpgradeCost() / 2;
        hero.gainGold(refund);
        // Gold refund visual (#59)
        gameBoard.addEffect(VisualEffect.createGoldPickup(selectedTower.getX(), selectedTower.getY(), refund));
        // Clean up boost references
        int idx = boostedTowers.indexOf(selectedTower);
        if (idx >= 0) {
            boostedTowers.remove(idx);
            boostedOriginalDamage.remove(idx);
        }
        towers.remove(selectedTower);
        selectedTower = null;
        sellPending = false;
        sellConfirmTimer = 0;
        
        // Update synergies after selling
        javatower.systems.TowerSynergyManager.updateSynergies(towers);
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
        
        // Update tower synergies after placing new tower
        javatower.systems.TowerSynergyManager.updateSynergies(towers);
        
        // Log synergy if created
        if (tower.hasSynergy() && combatLogPanel != null) {
            String synergyDesc = javatower.systems.TowerSynergyManager.getSynergyDescription(tower.getActiveSynergy());
            combatLogPanel.addEntry("✨ Synergy: " + synergyDesc);
        }
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

        // Update HUD data before rendering (#52 + #75)
        gameBoard.updateHUD(
            new double[]{skillCooldownTimer, specialCooldownTimer, healCooldownTimer, towerBoostTimer},
            hero != null ? hero.getMana() : 0,
            runTime,
            waveManager != null ? waveManager.getCurrentWave() : 0,
            waveManager != null ? waveManager.getActiveEnemies().size() : 0
        );
        gameBoard.setSellConfirmActive(sellPending);

        gameBoard.render(gc);

        // Draw wave complete notification (countdown now shown in WaveInfoPanel)
        if (waitingForNextWave) {
            gc.setFill(javafx.scene.paint.Color.web("#4ecca3"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
            gc.fillText("Wave " + waveManager.getCurrentWave() + " Complete!",
                    Constants.SCREEN_WIDTH / 2.0 - 140, Constants.SCREEN_HEIGHT / 2.0);
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
        statsLabel.setFont(Font.font("Monospaced", 22));
        statsLabel.setStyle("-fx-text-fill: #eee;");

        Label killStatsLabel = new Label(String.format(
                "Kills: %d | Damage Dealt: %d\nGold Earned: %d | XP Earned: %d",
                hero.getTotalKills(), hero.getTotalDamageDealt(),
                hero.getTotalGoldEarned(), hero.getTotalXPEarned()));
        killStatsLabel.setFont(Font.font("Monospaced", 20));
        killStatsLabel.setStyle("-fx-text-fill: #aaa;");

        Button mainMenuBtn = createMenuButton("Main Menu");
        mainMenuBtn.setOnAction(e -> showMainMenu());

        gameOverBox.getChildren().addAll(resultLabel, statsLabel, killStatsLabel, mainMenuBtn);

        currentScene = new Scene(gameOverBox, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        btn.setPrefWidth(280);
        btn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospaced", 12));
        btn.setStyle("-fx-background-color: #533483; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }
    
    /**
     * Creates a styled icon button for the action bar.
     * @param text Button text with emoji icon
     * @param color Hex color for background
     */
    private Button createIconButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI Emoji", 16));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 16;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }
    
    /**
     * Creates a sidebar button with icon, name and hotkey.
     */
    private Button createSidebarButton(String iconName, String hotkey, String color) {
        String text = hotkey.isEmpty() ? iconName : iconName + " [" + hotkey + "]";
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI Emoji", 15));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10; -fx-pref-width: 180; -fx-alignment: CENTER_LEFT;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10; -fx-pref-width: 180; -fx-alignment: CENTER_LEFT; -fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10; -fx-pref-width: 180; -fx-alignment: CENTER_LEFT;"));
        return btn;
    }
    
    /**
     * Creates a section label for the sidebar.
     */
    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        label.setStyle("-fx-text-fill: #ecc94b; -fx-padding: 5 0 5 0;");
        return label;
    }
    
    /**
     * Creates a separator line.
     */
    private javafx.scene.shape.Line createSeparator() {
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 170, 0);
        line.setStyle("-fx-stroke: #1a1a2e; -fx-stroke-width: 2;");
        line.setTranslateX(5);
        return line;
    }
}
