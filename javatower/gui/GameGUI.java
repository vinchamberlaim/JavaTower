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
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
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
import javatower.util.Difficulty;
import javatower.util.GameState;
import javatower.util.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main JavaFX {@link Application} — owns the primary stage, game loop, and
 * all scene transitions (main menu, gameplay, shop, inventory, forge,
 * skill tree, pause, game-over).
 * <p>
 * The real-time game loop is driven by an {@link AnimationTimer} that calls
 * {@link #gameUpdate(double)} (simulation) and {@link #renderBoard()}
 * (rendering) every frame at ~60 FPS.
 * </p>
 * <p>
 * <b>CIS096 relevance:</b> acts as the controller/orchestrator in an MVC-like
 * architecture: coordinates model updates ({@link Hero}, {@link Enemy}, towers),
 * drives view refreshes ({@link GameBoard}), and enforces state transitions.
 * </p>
 * <h3>Keyboard Hotkeys</h3>
 * <ul>
 *   <li><b>1–4</b> — place Arrow / Magic / Siege / Support tower</li>
 *   <li><b>Q</b> — melee AoE skill (15 mana)</li>
 *   <li><b>W</b> — ranged AoE blast (25 mana)</li>
 *   <li><b>E</b> — self-heal 30 % HP (20 mana)</li>
 *   <li><b>R</b> — tower boost ×2 damage for 5 s (25 mana)</li>
 *   <li><b>SHIFT</b> — dodge-roll (invincible dash)</li>
 *   <li><b>F</b> — ultimate RAGE mode (requires full meter)</li>
 *   <li><b>S</b> — sell selected tower (double-press to confirm)</li>
 *   <li><b>TAB</b> — open shop | <b>ESC</b> — pause | <b>M</b> — mini-map</li>
 * </ul>
 *
 * @author Vincent Chamberlain (2424309)
 * @author Nicolas Alfaro (2301126)
 * @author Emmanuel Adewumi (2507044)
 * @version 2.0 — CIS096-1 Assessment 2
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
    /** The 60-FPS {@link AnimationTimer} that drives the game loop. */
    private AnimationTimer gameLoop;
    /** Nanosecond timestamp of the previous frame (for delta-time calculation). */
    private long lastNanoTime;

    // ==================== Wave Transition ====================
    /** Countdown timer for the inter-wave delay (seconds remaining). */
    private double waveDelayTimer = 0;
    /** Flag indicating we are between waves, showing a countdown. */
    private boolean waitingForNextWave = false;

    // ==================== Tower Placement ====================
    /** The tower type the player is about to place (null = not placing). */
    private TowerType pendingTowerType = null;
    /** The tower currently selected for upgrade / sell actions. */
    private Tower selectedTower = null;

    // ==================== Tower Boost Tracking ====================
    /** Remaining duration of the R-key tower boost buff (seconds). */
    private double towerBoostTimer = 0;
    /** Towers receiving the boost (so original damage can be restored). */
    private List<Tower> boostedTowers = new ArrayList<>();
    /** Original damage values before boost was applied. */
    private List<Integer> boostedOriginalDamage = new ArrayList<>();

    // ==================== Bone Piles ====================
    /** Bone piles dropped by dead enemies — shared with enemies for consumption. */
    private List<BonePile> bonePiles = new ArrayList<>();

    // ==================== Ability Cooldown Timers ====================
    /** Remaining cooldown for Q skill (melee AoE). */
    private double skillCooldownTimer = 0;
    /** Remaining cooldown for W skill (ranged AoE). */
    private double specialCooldownTimer = 0;
    /** Remaining cooldown for E skill (self-heal). */
    private double healCooldownTimer = 0;
    /** Maximum cooldown for Q skill in seconds. */
    private static final double SKILL_COOLDOWN = 1.5;
    /** Maximum cooldown for W skill in seconds. */
    private static final double SPECIAL_COOLDOWN = 3.0;
    /** Maximum cooldown for E skill in seconds. */
    private static final double HEAL_COOLDOWN = 4.0;
    /** Maximum cooldown for R skill in seconds. */
    private static final double BOOST_COOLDOWN = 5.0;

    // ==================== Run Timer ====================
    /** Elapsed real time since the run started (seconds). */
    private double runTime = 0;

    // ==================== Tower Sell Confirmation ====================
    /** Whether a sell confirmation prompt is active (double-press S). */
    private boolean sellPending = false;
    /** Countdown timer for the sell-confirm window. */
    private double sellConfirmTimer = 0;
    /** How long the player has to press S again to confirm a sell. */
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
        newGameBtn.setOnAction(e -> showDifficultySelect());

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
     * Shows the difficulty selection screen before starting a new game.
     */
    private void showDifficultySelect() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        box.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("Select Difficulty");
        title.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #e94560;");

        String[][] info = {
            {"Easy",      "Enemies x0.7 · Hero +30% stats — relaxed"},
            {"Normal",    "Standard experience — balanced for all players"},
            {"Hard",      "Enemies x1.3 · Gold & XP +30% bonus"},
            {"Nightmare", "Enemies x1.8 · Gold & XP +80% bonus"}
        };
        Difficulty[] diffs = Difficulty.values();

        for (int i = 0; i < diffs.length; i++) {
            Difficulty d = diffs[i];
            Button btn = createMenuButton(d.displayName);
            btn.setStyle(btn.getStyle() + "-fx-text-fill: " + d.colour + ";");
            Label desc = new Label(info[i][1]);
            desc.setFont(Font.font("Monospaced", 13));
            desc.setStyle("-fx-text-fill: #999;");
            VBox row = new VBox(2, btn, desc);
            row.setAlignment(Pos.CENTER);
            btn.setOnAction(e -> { Difficulty.setCurrent(d); startNewGame(); });
            box.getChildren().add(row);
        }

        Button back = createMenuButton("Back");
        back.setOnAction(e -> showMainMenu());
        box.getChildren().addAll(title, back);
        // Move title to top
        box.getChildren().remove(title);
        box.getChildren().add(0, title);

        currentScene = new Scene(box, Constants.SCREEN_WIDTH + 250,
                Constants.SCREEN_HEIGHT + 60);
        primaryStage.setScene(currentScene);
    }

    /**
     * Starts a new game.
     */
    public void startNewGame() {
        hero = new Hero("Hero");
        hero.setPosition(100, Constants.SCREEN_HEIGHT / 2.0);
        // Apply difficulty multiplier to hero starting stats
        Difficulty diff = Difficulty.getCurrent();
        hero.setMaxHealth((int)(hero.getMaxHealth() * diff.heroStatMul));
        hero.setCurrentHealth(hero.getMaxHealth());
        hero.setAttack((int)(hero.getAttack() * diff.heroStatMul));
        hero.setDefence((int)(hero.getDefence() * diff.heroStatMul));
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

        // ===== NEW COMPACT LAYOUT =====
        // Right panel: Stats (narrower)
        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(8));
        rightPanel.setStyle("-fx-background-color: #0d1b2a; -fx-border-color: #1b263b; -fx-border-width: 0 0 0 2;");
        rightPanel.setPrefWidth(220);
        rightPanel.setMinWidth(200);
        rightPanel.getChildren().addAll(heroPanel, waveInfoPanel, combatLogPanel);

        // Bottom bar: Abilities + Towers (like MOBA games)
        HBox bottomBar = createBottomActionBar();

        // Top bar: Menu buttons (compact)
        HBox topBar = createTopMenuBar();

        BorderPane root = new BorderPane();
        root.setCenter(gameBoard);
        root.setRight(rightPanel);
        root.setBottom(bottomBar);
        root.setTop(topBar);
        root.setStyle("-fx-background-color: #0d1b2a;");

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

        // Auto-cast class spells based on equipped set pieces
        hero.tickSpellTimers(dt);
        processAutoSpells();
        
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

            int xp = (int)(e.getExperienceValue() * Difficulty.getCurrent().xpMul);
            int gold = (int)(e.getGoldValue() * Difficulty.getCurrent().goldMul);
            hero.gainExperience(xp);
            hero.gainGold(gold);
            hero.recordKill(e.getMaxHealth(), gold, xp);
            
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
            // Only count original wave enemies, not summoned ones
            if (!e.isSummoned()) {
                waveManager.onEnemyKilled(e);
            }
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

            waitingForNextWave = true; // Prevent multiple wave starts
            waveManager.nextWave();
            startWave();
            waitingForNextWave = false; // Allow next wave after start completes
        }

        // Refresh panels every few frames (reduce label thrashing)
        refreshPanels();
    }

    // ---- Click handling ----

    private void handleBoardClick(double screenX, double screenY) {
        if (gameState != GameState.PLAYING) return;
        
        // Convert screen coordinates to world coordinates (account for camera)
        double[] worldCoords = gameBoard.screenToWorld(screenX, screenY);
        double worldX = worldCoords[0];
        double worldY = worldCoords[1];

        // Tower placement mode
        if (pendingTowerType != null) {
            int[] grid = gameBoard.screenToTowerGrid(worldX, worldY);
            placeTower(pendingTowerType, grid[0], grid[1]);
            pendingTowerType = null;
            gameBoard.clearPendingTowerType();
            return;
        }

        // Check if clicking on an existing tower → select or cycle targeting mode (#31)
        for (Tower t : towers) {
            if (Math.abs(t.getX() - worldX) < 32 && Math.abs(t.getY() - worldY) < 32) {
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

        // Click = move hero to that position (in world coordinates)
        hero.moveTo(worldX, worldY);
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

    // ========== AUTO-CAST CLASS SPELLS ==========

    /**
     * Processes automatic class spells triggered by equipped set pieces.
     * Each set fires a unique auto-spell on a timer when the hero has 2+ pieces equipped.
     * 4-piece sets get an enhanced version of the spell.
     *
     * HOLY:   Divine Judgement — heal self + smite nearby enemies
     * DEATH:  Soul Siphon — drain life from nearby enemies
     * FIRE:   Flame Nova — fire explosion around hero
     * KNIGHT: War Cry — brief defence buff + stun nearby enemies
     */
    private void processAutoSpells() {
        if (gameState != GameState.PLAYING) return;
        Item[] eq = hero.getEquippedItems();
        java.util.List<Enemy> enemies = waveManager.getActiveEnemies();

        // HOLY: Divine Judgement
        if (SetBonusManager.hasTwoPiece(eq, Item.EquipmentSet.HOLY) && hero.getHolySpellTimer() <= 0) {
            boolean four = SetBonusManager.hasFourPiece(eq, Item.EquipmentSet.HOLY);
            int healAmt = four ? (int)(hero.getEffectiveMaxHealth() * 0.25) : (int)(hero.getEffectiveMaxHealth() * 0.12);
            hero.heal(healAmt);
            gameBoard.addEffect(VisualEffect.createHealNumber(hero.getX(), hero.getY() - 20, healAmt));
            // Smite nearest enemies (reduced multipliers for balance)
            int smiteDmg = (int)(hero.getEffectiveAttack() * (four ? 1.2 : 0.5));
            double range = four ? 120 : 70;
            for (Enemy e : enemies) {
                if (e.isAlive() && hero.distanceTo(e) <= range + e.getRadius()) {
                    e.takeDamage(smiteDmg);
                    gameBoard.addEffect(VisualEffect.createHolySmite(hero.getX(), hero.getY(), e.getX(), e.getY()));
                }
            }
            hero.setHolySpellTimer(Hero.HOLY_SPELL_CD);
        }

        // DEATH: Soul Siphon
        if (SetBonusManager.hasTwoPiece(eq, Item.EquipmentSet.DEATH) && hero.getDeathSpellTimer() <= 0) {
            boolean four = SetBonusManager.hasFourPiece(eq, Item.EquipmentSet.DEATH);
            double necroMult = 1.0 + hero.getNecroSummonBonus();
            int drainDmg = (int)(hero.getEffectiveAttack() * (four ? 1.0 : 0.4) * necroMult);
            double range = four ? 110 : 70;
            int totalDrained = 0;
            int hitCount = 0;
            for (Enemy e : enemies) {
                if (e.isAlive() && hero.distanceTo(e) <= range + e.getRadius()) {
                    int dealt = e.takeDamage(drainDmg);
                    totalDrained += dealt;
                    hitCount++;
                    gameBoard.addEffect(VisualEffect.createNecroBolt(e.getX(), e.getY(), hero.getX(), hero.getY()));
                }
            }
            if (totalDrained > 0) {
                int lifeBack = (int)(totalDrained * (four ? 0.35 : 0.20));
                hero.heal(lifeBack);
                gameBoard.addEffect(VisualEffect.createHealNumber(hero.getX(), hero.getY() - 10, lifeBack));
            }
            // Mana restore
            int manaBack = four ? 8 + hitCount * 3 : 4 + hitCount * 2;
            hero.setMana(Math.min(hero.getEffectiveMaxMana(), hero.getMana() + manaBack));
            hero.setDeathSpellTimer(Hero.DEATH_SPELL_CD);
        }

        // FIRE: Flame Nova
        if (SetBonusManager.hasTwoPiece(eq, Item.EquipmentSet.FIRE) && hero.getFireSpellTimer() <= 0) {
            boolean four = SetBonusManager.hasFourPiece(eq, Item.EquipmentSet.FIRE);
            double pyroMult = 1.0 + hero.getPyroFireBonus();
            int novaDmg = (int)(hero.getEffectiveAttack() * (four ? 1.5 : 0.6) * pyroMult);
            double range = four ? 140 : 90;
            gameBoard.addEffect(VisualEffect.createFireSplash(hero.getX(), hero.getY()));
            for (Enemy e : enemies) {
                if (e.isAlive() && hero.distanceTo(e) <= range + e.getRadius()) {
                    e.takeDamage(novaDmg);
                }
            }
            hero.setFireSpellTimer(Hero.FIRE_SPELL_CD);
        }

        // KNIGHT: War Cry — AoE damage + brief slow (damage only, no separate slow mechanic)
        if (SetBonusManager.hasTwoPiece(eq, Item.EquipmentSet.KNIGHT) && hero.getKnightSpellTimer() <= 0) {
            boolean four = SetBonusManager.hasFourPiece(eq, Item.EquipmentSet.KNIGHT);
            int cryDmg = (int)(hero.getEffectiveAttack() * (four ? 1.0 : 0.4));
            double range = four ? 100 : 60;
            gameBoard.addEffect(VisualEffect.createImpactBurst(hero.getX(), hero.getY(), javafx.scene.paint.Color.STEELBLUE));
            for (Enemy e : enemies) {
                if (e.isAlive() && hero.distanceTo(e) <= range + e.getRadius()) {
                    e.takeDamage(cryDmg);
                }
            }
            // Self-heal (Knight tank identity)
            int tankHeal = four ? (int)(hero.getEffectiveMaxHealth() * 0.10) : (int)(hero.getEffectiveMaxHealth() * 0.05);
            hero.heal(tankHeal);
            hero.setKnightSpellTimer(Hero.KNIGHT_SPELL_CD);
        }
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
     * Creates the bottom action bar with abilities and towers.
     */
    private HBox createBottomActionBar() {
        HBox bottomBar = new HBox(12);
        bottomBar.setPadding(new Insets(10, 15, 20, 15));
        bottomBar.setStyle("-fx-background-color: #0d1b2a; -fx-border-color: #1b263b; -fx-border-width: 2 0 0 0;");
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPrefHeight(90);
        bottomBar.setMaxHeight(90);

        // Left: Tower buttons
        VBox towerSection = new VBox(4);
        towerSection.setAlignment(Pos.CENTER);
        Label towerLabel = new Label("🏰 TOWERS");
        towerLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        towerLabel.setStyle("-fx-text-fill: #778da9;");
        
        HBox towerBtns = new HBox(6);
        towerBtns.setAlignment(Pos.CENTER);
        towerBtns.getChildren().addAll(
            createActionIcon("🏹", "1", "#48bb78", () -> setTowerPlacementMode(TowerType.ARROW)),
            createActionIcon("✨", "2", "#9f7aea", () -> setTowerPlacementMode(TowerType.MAGIC)),
            createActionIcon("💥", "3", "#f56565", () -> setTowerPlacementMode(TowerType.SIEGE)),
            createActionIcon("💚", "4", "#4fd1c5", () -> setTowerPlacementMode(TowerType.SUPPORT))
        );
        towerSection.getChildren().addAll(towerLabel, towerBtns);

        // Center: Ability buttons (with cooldown indicators)
        VBox abilitySection = new VBox(4);
        abilitySection.setAlignment(Pos.CENTER);
        Label abilityLabel = new Label("⚔️ ABILITIES");
        abilityLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        abilityLabel.setStyle("-fx-text-fill: #778da9;");
        
        HBox abilityBtns = new HBox(8);
        abilityBtns.setAlignment(Pos.CENTER);
        abilityBtns.getChildren().addAll(
            createAbilityButton("⚔️", "Q", "#e94560", 15),
            createAbilityButton("🔥", "W", "#dd6b20", 25),
            createAbilityButton("💗", "E", "#48bb78", 20),
            createAbilityButton("⚡", "R", "#ecc94b", 25)
        );
        abilitySection.getChildren().addAll(abilityLabel, abilityBtns);

        // Right: Special actions
        VBox specialSection = new VBox(4);
        specialSection.setAlignment(Pos.CENTER);
        Label specialLabel = new Label("💨 SPECIAL");
        specialLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        specialLabel.setStyle("-fx-text-fill: #778da9;");
        
        HBox specialBtns = new HBox(6);
        specialBtns.setAlignment(Pos.CENTER);
        
        // Ultimate button (bigger)
        Button ultBtn = new Button("🔥 F");
        ultBtn.setFont(Font.font("Segoe UI Emoji", 14));
        ultBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 12; -fx-min-width: 50;");
        ultBtn.setOnAction(e -> useUltimateAbility());
        
        // Roll button
        Button rollBtn = new Button("💨 SHIFT");
        rollBtn.setFont(Font.font("Segoe UI Emoji", 12));
        rollBtn.setStyle("-fx-background-color: #63b3ed; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 8;");
        rollBtn.setOnAction(e -> hero.rollInMovementDirection());
        
        specialBtns.getChildren().addAll(ultBtn, rollBtn);
        specialSection.getChildren().addAll(specialLabel, specialBtns);

        // Utility: Sell/Upgrade
        VBox utilSection = new VBox(4);
        utilSection.setAlignment(Pos.CENTER);
        Label utilLabel = new Label("🔧 UTIL");
        utilLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        utilLabel.setStyle("-fx-text-fill: #778da9;");
        
        HBox utilBtns = new HBox(6);
        utilBtns.setAlignment(Pos.CENTER);
        Button upBtn = createSmallBtn("⬆️ T", () -> upgradeTower());
        Button sellBtn = createSmallBtn("💰 S", () -> sellTower());
        utilBtns.getChildren().addAll(upBtn, sellBtn);
        utilSection.getChildren().addAll(utilLabel, utilBtns);

        bottomBar.getChildren().addAll(towerSection, new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL), 
                abilitySection, new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL),
                specialSection, new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL),
                utilSection);
        
        HBox.setHgrow(abilitySection, Priority.ALWAYS);
        return bottomBar;
    }

    /**
     * Creates the top menu bar.
     */
    private HBox createTopMenuBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8, 15, 8, 15));
        topBar.setStyle("-fx-background-color: #0d1b2a; -fx-border-color: #1b263b; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(50);

        // Title
        Label title = new Label("⚔️ JavaTower");
        title.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #e94560;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Menu buttons
        HBox menuBtns = new HBox(8);
        menuBtns.setAlignment(Pos.CENTER_RIGHT);
        menuBtns.getChildren().addAll(
            createTopBtn("🛒 Shop", () -> showShop()),
            createTopBtn("🎒 Items", () -> showInventory()),
            createTopBtn("🌳 Skills", () -> showSkillTree()),
            createTopBtn("🔨 Forge", () -> showForge()),
            createTopBtn("🗺️ Map", () -> gameBoard.getMiniMap().toggle()),
            createTopBtn("⏸️ Pause", () -> togglePause()),
            createTopBtn("🖥️ Full", () -> toggleFullscreen())
        );

        topBar.getChildren().addAll(title, spacer, menuBtns);
        return topBar;
    }

    private Button createActionIcon(String icon, String hotkey, String color, Runnable action) {
        Button btn = new Button(icon);
        btn.setFont(Font.font("Segoe UI Emoji", 18));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10; -fx-min-width: 45; -fx-min-height: 45;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createAbilityButton(String icon, String hotkey, String color, int manaCost) {
        Button btn = new Button(icon + "\n" + hotkey);
        btn.setFont(Font.font("Segoe UI Emoji", 14));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4; -fx-min-width: 50; -fx-min-height: 50;");
        btn.setOnAction(e -> {
            switch(hotkey) {
                case "Q": useSkill(); break;
                case "W": useSpecialAbility(); break;
                case "E": selfHeal(); break;
                case "R": towerBoost(); break;
            }
        });
        return btn;
    }

    private Button createSmallBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI Emoji", 11));
        btn.setStyle("-fx-background-color: #415a77; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 8;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createTopBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI Emoji", 11));
        btn.setStyle("-fx-background-color: #1b263b; -fx-text-fill: #e0e1dd; -fx-cursor: hand; -fx-padding: 6 10;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #415a77; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1b263b; -fx-text-fill: #e0e1dd; -fx-cursor: hand; -fx-padding: 6 10;"));
        btn.setOnAction(e -> action.run());
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
