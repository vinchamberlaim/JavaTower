package javatower.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Entity;
import javatower.entities.Hero;
import javatower.entities.Enemy;
import javatower.entities.Tower;
import javatower.entities.BonePile;
import javatower.entities.Item;
import javatower.entities.Tower.TowerType;
import javatower.util.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Renders the game world using pixel coordinates.
 */
public class GameBoard extends Canvas {
    private Hero hero;
    private List<Enemy> enemies;
    private List<Tower> towers;
    private List<BonePile> bonePiles = new ArrayList<>();
    private List<VisualEffect> effects = new ArrayList<>();
    
    // Tower placement preview
    private TowerType pendingTowerType = null;
    private double mouseX = 0;
    private double mouseY = 0;
    private boolean showPlacementPreview = false;
    
    // Screen shake effect
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    private double shakeTimer = 0;
    private double shakeOffsetX = 0;
    private double shakeOffsetY = 0;
    
    // Mini-map overlay
    private MiniMap miniMap = new MiniMap();
    
    // Pixel art renderer for enemies and towers
    private PixelArtRenderer pixelArtRenderer = new PixelArtRenderer();
    
    // Tower hover for range visualization
    private Tower hoveredTower = null;
    private static final double TOWER_HOVER_RADIUS = 32;

    // HUD data — set by GameGUI each frame
    private double[] abilityCooldowns = {0, 0, 0, 0}; // Q, W, E, R remaining
    private double[] abilityMaxCooldowns = {1.5, 3.0, 4.0, 5.0};
    private int[] abilityManaCosts = {15, 25, 20, 25};
    private int heroMana = 0;
    private double runTime = 0;
    private int waveNumber = 0;
    private int enemyCount = 0;
    private boolean sellConfirmActive = false; // tower sell confirmation (#40)

    public GameBoard() {
        setWidth(Constants.SCREEN_WIDTH);
        setHeight(Constants.SCREEN_HEIGHT);
        
        // Track mouse for placement preview and tower hover
        setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            updateHoveredTower();
        });
    }
    
    /**
     * Updates which tower is currently hovered for range visualization.
     */
    private void updateHoveredTower() {
        hoveredTower = null;
        if (towers != null) {
            for (Tower t : towers) {
                if (t.isAlive() && Math.abs(t.getX() - mouseX) < TOWER_HOVER_RADIUS 
                        && Math.abs(t.getY() - mouseY) < TOWER_HOVER_RADIUS) {
                    hoveredTower = t;
                    break;
                }
            }
        }
    }

    /**
     * Renders the board and all entities.
     */
    public void render(GraphicsContext gc) {
        // Apply screen shake
        gc.save();
        gc.translate(shakeOffsetX, shakeOffsetY);
        
        // Background
        gc.setFill(Color.web("#0f3460"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawGrid(gc);

        // Draw bone piles (under everything else)
        for (BonePile bp : bonePiles) {
            drawBonePile(bp, gc);
        }
        
        // Draw tower placement preview (ghost tower + range)
        if (showPlacementPreview && pendingTowerType != null) {
            drawPlacementPreview(gc);
        }

        // Draw tower range indicators
        if (towers != null) {
            for (Tower t : towers) {
                if (t.isAlive()) drawTowerRange(t, gc);
            }
        }

        // Draw towers
        if (towers != null) {
            for (Tower t : towers) {
                if (t.isAlive()) drawTower(t, gc);
            }
        }

        // Draw enemies
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isAlive()) drawEnemy(e, gc);
            }
        }

        // Draw hero
        if (hero != null && hero.isAlive()) {
            drawHero(hero, gc);
        }

        // Draw visual effects (on top of everything)
        for (VisualEffect effect : effects) {
            effect.render(gc);
        }
        
        // Draw enemy hover tooltip (topmost layer)
        drawEnemyHoverInfo(gc);
        
        // Draw tower hover tooltip with kill stats + targeting mode (#37 + #31)
        drawTowerHoverInfo(gc);
        
        // Restore from screen shake
        gc.restore();
        
        // Draw mini-map (on top of everything, not affected by shake)
        miniMap.render(gc, hero, enemies, towers);
        
        // Draw HUD overlay (not affected by shake)
        drawHUD(gc);
    }

    /** Update and remove expired effects. Call each frame. */
    public void updateEffects(double dt) {
        Iterator<VisualEffect> it = effects.iterator();
        while (it.hasNext()) {
            if (it.next().update(dt)) it.remove();
        }
        
        // Update screen shake
        updateScreenShake(dt);
        
        // Update pixel art animations
        pixelArtRenderer.update(dt);
    }
    
    /**
     * Applies a screen shake effect.
     * @param intensity maximum pixel offset
     * @param duration duration in seconds
     */
    public void applyScreenShake(double intensity, double duration) {
        shakeIntensity = intensity;
        shakeDuration = duration;
        shakeTimer = duration;
    }
    
    private void updateScreenShake(double dt) {
        if (shakeTimer > 0) {
            shakeTimer -= dt;
            double progress = shakeTimer / shakeDuration;
            double currentIntensity = shakeIntensity * progress;
            
            // Random offset within intensity range
            shakeOffsetX = (Math.random() - 0.5) * 2 * currentIntensity;
            shakeOffsetY = (Math.random() - 0.5) * 2 * currentIntensity;
            
            if (shakeTimer <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }
        }
    }
    
    /**
     * Returns the current screen shake offset. Apply to all rendering.
     */
    public double getShakeOffsetX() { return shakeOffsetX; }
    public double getShakeOffsetY() { return shakeOffsetY; }

    public void addEffect(VisualEffect effect) {
        effects.add(effect);
    }

    public List<VisualEffect> getEffects() { return effects; }

    public void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.web("#1a1a2e", 0.3));
        gc.setLineWidth(0.5);
        // Draw coarse tower placement grid
        for (int x = 0; x <= Constants.TILE_COLS; x++) {
            gc.strokeLine(x * Constants.TILE_SIZE, 0, x * Constants.TILE_SIZE, getHeight());
        }
        for (int y = 0; y <= Constants.TILE_ROWS; y++) {
            gc.strokeLine(0, y * Constants.TILE_SIZE, getWidth(), y * Constants.TILE_SIZE);
        }
    }

    private void drawHero(Hero h, GraphicsContext gc) {
        double px = h.getX();
        double py = h.getY();
        double r = h.getRadius();

        // Equipment-based color scheme based on active set bonus
        Color bodyColor = getHeroBodyColor(h);
        Color armorColor = bodyColor.darker();

        // --- Procedural pixel knight (#1) ---
        double s = r * 0.12; // pixel scale factor

        // Feet (dark boots)
        gc.setFill(Color.web("#3d2817"));
        gc.fillRect(px - 5 * s, py + 6 * s, 3 * s, 3 * s);
        gc.fillRect(px + 2 * s, py + 6 * s, 3 * s, 3 * s);

        // Legs (pants)
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRect(px - 4 * s, py + 2 * s, 3 * s, 5 * s);
        gc.fillRect(px + 1 * s, py + 2 * s, 3 * s, 5 * s);

        // Torso (armor colored by set)
        gc.setFill(armorColor);
        gc.fillRect(px - 5 * s, py - 4 * s, 10 * s, 7 * s);
        // Armor highlight
        gc.setFill(bodyColor);
        gc.fillRect(px - 4 * s, py - 3 * s, 8 * s, 5 * s);
        // Belt
        gc.setFill(Color.web("#8b6914"));
        gc.fillRect(px - 5 * s, py + 1 * s, 10 * s, 2 * s);

        // Arms
        gc.setFill(armorColor);
        gc.fillRect(px - 7 * s, py - 3 * s, 2 * s, 6 * s);
        gc.fillRect(px + 5 * s, py - 3 * s, 2 * s, 6 * s);
        // Hands (skin)
        gc.setFill(Color.web("#deb887"));
        gc.fillRect(px - 7 * s, py + 2 * s, 2 * s, 2 * s);
        gc.fillRect(px + 5 * s, py + 2 * s, 2 * s, 2 * s);

        // Sword (right hand)
        gc.setFill(Color.web("#c0c0c0"));
        gc.fillRect(px + 6 * s, py - 6 * s, 2 * s, 9 * s);
        // Sword hilt
        gc.setFill(Color.web("#8b6914"));
        gc.fillRect(px + 5 * s, py + 1 * s, 4 * s, 2 * s);
        // Sword tip
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(px + 6.5 * s, py - 8 * s, 1 * s, 3 * s);

        // Shield (left hand)
        gc.setFill(Color.web("#e94560"));
        gc.fillRect(px - 10 * s, py - 4 * s, 4 * s, 7 * s);
        gc.setFill(Color.web("#c0392b"));
        gc.fillRect(px - 9 * s, py - 3 * s, 2 * s, 5 * s);

        // Head (skin)
        gc.setFill(Color.web("#deb887"));
        gc.fillRect(px - 3 * s, py - 8 * s, 6 * s, 5 * s);

        // Eyes
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(px - 2 * s, py - 6 * s, 1.5 * s, 1.5 * s);
        gc.fillRect(px + 0.5 * s, py - 6 * s, 1.5 * s, 1.5 * s);

        // Helmet
        gc.setFill(armorColor.brighter());
        gc.fillRect(px - 4 * s, py - 10 * s, 8 * s, 3 * s);
        // Helmet visor
        gc.setFill(Color.web("#1a1a2e", 0.5));
        gc.fillRect(px - 3 * s, py - 7 * s, 6 * s, 1 * s);

        drawHealthBar(h, gc);
        drawManaBar(h, gc);
    }

    /**
     * Returns hero body color based on equipped set (idea #9).
     */
    private Color getHeroBodyColor(Hero h) {
        Item[] eq = h.getEquippedItems();
        int holy = 0, death = 0, fire = 0, knight = 0;
        for (Item it : eq) {
            if (it == null) continue;
            if (it.getEquipmentSet() == null) continue;
            switch (it.getEquipmentSet()) {
                case HOLY: holy++; break;
                case DEATH: death++; break;
                case FIRE: fire++; break;
                case KNIGHT: knight++; break;
            }
        }
        // Pick dominant set (need 2+ pieces to show)
        int max = Math.max(Math.max(holy, death), Math.max(fire, knight));
        if (max >= 2) {
            if (holy == max) return Color.web("#ffd700"); // gold
            if (death == max) return Color.web("#9b59b6"); // purple
            if (fire == max) return Color.web("#e94560"); // red
            if (knight == max) return Color.web("#3498db"); // blue
        }
        return Color.web("#4ecca3"); // default teal
    }

    /**
     * Draws a mana bar below the health bar (idea #53).
     */
    private void drawManaBar(Hero h, GraphicsContext gc) {
        double px = h.getX();
        double py = h.getY();
        double r = h.getRadius();
        double barWidth = r * 2.5;
        double barHeight = 3;
        double manaPct = (double) h.getMana() / Math.max(1, h.getEffectiveMaxMana());
        double bx = px - barWidth / 2;
        double by = py - r - 4; // just below health bar

        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(bx, by, barWidth, barHeight);
        gc.setFill(Color.web("#3b82f6"));
        gc.fillRect(bx, by, barWidth * manaPct, barHeight);
    }

    private void drawEnemy(Enemy e, GraphicsContext gc) {
        double px = e.getX();
        double py = e.getY();
        
        // Draw pixel art sprite instead of colored circle
        pixelArtRenderer.drawEnemySprite(gc, e, px, py);

        drawHealthBar(e, gc);
    }

    private void drawTower(Tower t, GraphicsContext gc) {
        double px = t.getX();
        double py = t.getY();

        // Draw pixel art tower sprite
        pixelArtRenderer.drawTowerSprite(gc, t, px, py);
        
        // Level indicator (if upgraded)
        if (t.getUpgradeLevel() > 0) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", 10));
            gc.fillText("L" + t.getUpgradeLevel(), px - 8, py + 16);
        }
    }

    private void drawTowerRange(Tower t, GraphicsContext gc) {
        double rangePx = t.getRangePixels();
        boolean isHovered = (t == hoveredTower);
        
        if (isHovered) {
            // Bright, visible range circle when hovered
            Color rangeColor = getTowerColor(t.getType());
            gc.setStroke(Color.color(rangeColor.getRed(), rangeColor.getGreen(), rangeColor.getBlue(), 0.8));
            gc.setLineWidth(2);
            gc.strokeOval(t.getX() - rangePx, t.getY() - rangePx, rangePx * 2, rangePx * 2);
            
            // Fill with very transparent color
            gc.setFill(Color.color(rangeColor.getRed(), rangeColor.getGreen(), rangeColor.getBlue(), 0.1));
            gc.fillOval(t.getX() - rangePx, t.getY() - rangePx, rangePx * 2, rangePx * 2);
        } else {
            // Faint range circle normally
            gc.setStroke(Color.web("#ffffff", 0.1));
            gc.setLineWidth(1);
            gc.strokeOval(t.getX() - rangePx, t.getY() - rangePx, rangePx * 2, rangePx * 2);
        }
    }

    public void drawHealthBar(Entity e, GraphicsContext gc) {
        double px = e.getX();
        double py = e.getY();
        double r = e.getRadius();
        double barWidth = r * 2.5;
        double barHeight = 4;
        double healthPct = e.getHealthPercent() / 100.0;

        double bx = px - barWidth / 2;
        double by = py - r - 8;

        // Background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(bx, by, barWidth, barHeight);

        // Health fill
        Color hpColor = healthPct > 0.5 ? Color.LIMEGREEN : healthPct > 0.25 ? Color.YELLOW : Color.RED;
        gc.setFill(hpColor);
        gc.fillRect(bx, by, barWidth * healthPct, barHeight);
    }

    /**
     * Converts screen click to tower grid coordinates.
     */
    public int[] screenToTowerGrid(double x, double y) {
        return new int[] {(int)(x / Constants.TILE_SIZE), (int)(y / Constants.TILE_SIZE)};
    }

    /**
     * Converts tower grid coordinates to pixel center.
     */
    public static double[] towerGridToPixel(int gx, int gy) {
        return new double[] {gx * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0,
                             gy * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0};
    }

    private void drawBonePile(BonePile bp, GraphicsContext gc) {
        double px = bp.getX();
        double py = bp.getY();
        double sz = bp.getSize();
        double alpha = Math.min(1.0, bp.getAge() * 2); // fade in

        // Draw a dark smudge under the pile
        gc.setFill(Color.web("#1a1a2e", 0.4 * alpha));
        gc.fillOval(px - sz * 1.2, py - sz * 0.6, sz * 2.4, sz * 1.2);

        double[] ox = bp.getBoneOffsetsX();
        double[] oy = bp.getBoneOffsetsY();
        double[] angles = bp.getBoneAngles();

        for (int i = 0; i < ox.length; i++) {
            double bx = px + ox[i];
            double by = py + oy[i];
            double boneLen = 3 + sz * 0.3;

            gc.save();
            gc.translate(bx, by);
            gc.rotate(Math.toDegrees(angles[i]));

            // Bone shaft
            gc.setStroke(Color.web("#d4c9a8", 0.8 * alpha));
            gc.setLineWidth(2);
            gc.strokeLine(-boneLen, 0, boneLen, 0);

            // Bone knobs on each end
            gc.setFill(Color.web("#e8dcc8", 0.7 * alpha));
            gc.fillOval(-boneLen - 2, -2, 4, 4);
            gc.fillOval(boneLen - 2, -2, 4, 4);

            gc.restore();
        }

        // Skull for larger piles (tier 5+)
        if (bp.getBoneCount() >= 5) {
            gc.setFill(Color.web("#e8dcc8", 0.6 * alpha));
            double skullR = sz * 0.35;
            gc.fillOval(px - skullR, py - skullR, skullR * 2, skullR * 2);
            // Eye sockets
            gc.setFill(Color.web("#1a1a2e", 0.8 * alpha));
            gc.fillOval(px - skullR * 0.5, py - skullR * 0.3, skullR * 0.4, skullR * 0.35);
            gc.fillOval(px + skullR * 0.15, py - skullR * 0.3, skullR * 0.4, skullR * 0.35);
        }
    }

    public void highlightTowerCell(int gx, int gy, Color color) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(color.deriveColor(1, 1, 1, 0.3));
        gc.fillRect(gx * Constants.TILE_SIZE, gy * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);
    }

    // Tower placement preview methods
    public void setPendingTowerType(TowerType type) {
        this.pendingTowerType = type;
        this.showPlacementPreview = (type != null);
    }

    /**
     * Draws a tooltip box near the enemy closest to the mouse cursor.
     */
    private void drawEnemyHoverInfo(GraphicsContext gc) {
        if (enemies == null) return;
        Enemy hovered = null;
        double bestDist = 30; // hover detection radius in pixels
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - mouseX;
            double dy = e.getY() - mouseY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < bestDist) {
                bestDist = dist;
                hovered = e;
            }
        }
        if (hovered == null) return;

        String name = hovered.getName();
        String info = String.format("HP: %d/%d  ATK: %d  T%d",
                hovered.getCurrentHealth(), hovered.getMaxHealth(),
                hovered.getAttack(), hovered.getType().tier);

        double boxW = 180, boxH = 48;
        double bx = hovered.getX() + hovered.getRadius() + 8;
        double by = hovered.getY() - boxH / 2;
        // Keep box on screen
        if (bx + boxW > getWidth()) bx = hovered.getX() - hovered.getRadius() - boxW - 8;
        if (by < 0) by = 0;
        if (by + boxH > getHeight()) by = getHeight() - boxH;

        // Background
        gc.setFill(Color.web("#1a1a2e", 0.9));
        gc.fillRoundRect(bx, by, boxW, boxH, 8, 8);
        gc.setStroke(Color.web("#e94560", 0.7));
        gc.setLineWidth(1);
        gc.strokeRoundRect(bx, by, boxW, boxH, 8, 8);

        // Text
        gc.setFill(Color.web("#eee"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.fillText(name, bx + 6, by + 16);
        gc.setFont(Font.font("Monospaced", 11));
        gc.setFill(Color.web("#ccc"));
        gc.fillText(info, bx + 6, by + 34);
    }
    
    public void clearPendingTowerType() {
        this.pendingTowerType = null;
        this.showPlacementPreview = false;
    }

    /**
     * Draws tower stat tooltip on hover (#37 + #31).
     */
    private void drawTowerHoverInfo(GraphicsContext gc) {
        if (hoveredTower == null) return;
        Tower t = hoveredTower;
        String name = t.getName() + " L" + t.getUpgradeLevel();
        String stats = String.format("DMG:%d  Kills:%d  Total:%d", t.getDamage(), t.getKillCount(), t.getTotalDamageDealt());
        String mode = "Target: " + t.getTargetMode().name();
        String sellLine = sellConfirmActive
            ? ">> PRESS S TO CONFIRM SELL <<"
            : "S to sell (" + (t.getUpgradeCost() / 2) + "g refund)";

        double boxW = 220, boxH = 70;
        double bx = t.getX() + 36;
        double by = t.getY() - boxH / 2;
        if (bx + boxW > getWidth()) bx = t.getX() - 36 - boxW;
        if (by < 0) by = 0;
        if (by + boxH > getHeight()) by = getHeight() - boxH;

        gc.setFill(Color.web("#1a1a2e", 0.9));
        gc.fillRoundRect(bx, by, boxW, boxH, 8, 8);
        gc.setStroke(sellConfirmActive ? Color.web("#e94560", 0.9) : Color.web("#4ecca3", 0.7));
        gc.setLineWidth(1);
        gc.strokeRoundRect(bx, by, boxW, boxH, 8, 8);

        gc.setFill(Color.web("#eee"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.fillText(name, bx + 6, by + 14);
        gc.setFont(Font.font("Monospaced", 10));
        gc.setFill(Color.web("#ccc"));
        gc.fillText(stats, bx + 6, by + 28);
        gc.setFill(Color.web("#4ecca3"));
        gc.fillText(mode + " (click to cycle)", bx + 6, by + 42);
        gc.setFill(sellConfirmActive ? Color.web("#e94560") : Color.web("#aaa"));
        gc.fillText(sellLine, bx + 6, by + 56);
    }
    
    public TowerType getPendingTowerType() { return pendingTowerType; }
    
    /**
     * Draws a ghost tower and range indicator at mouse position.
     */
    private void drawPlacementPreview(GraphicsContext gc) {
        int[] grid = screenToTowerGrid(mouseX, mouseY);
        int gx = grid[0], gy = grid[1];
        
        // Bounds check
        if (gx < 0 || gx >= Constants.TILE_COLS || gy < 0 || gy >= Constants.TILE_ROWS) return;
        
        double[] center = towerGridToPixel(gx, gy);
        double cx = center[0], cy = center[1];
        
        // Get range for this tower type
        double rangePixels = getTowerRangePixels(pendingTowerType);
        
        // Draw range circle (semi-transparent)
        gc.setStroke(Color.web("#4ecca3", 0.4));
        gc.setLineWidth(2);
        gc.setFill(Color.web("#4ecca3", 0.05));
        gc.fillOval(cx - rangePixels, cy - rangePixels, rangePixels * 2, rangePixels * 2);
        gc.strokeOval(cx - rangePixels, cy - rangePixels, rangePixels * 2, rangePixels * 2);
        
        // Draw ghost tower
        Color ghostColor = getTowerColor(pendingTowerType).deriveColor(1, 1, 1, 0.5);
        double half = Constants.TILE_SIZE * 0.4;
        
        // Ghost base
        gc.setFill(ghostColor);
        gc.fillRect(cx - half, cy - half * 0.5, half * 2, half * 1.5);
        
        // Ghost top (triangle)
        gc.setFill(ghostColor.darker());
        double[] xPoints = {cx - half * 0.7, cx, cx + half * 0.7};
        double[] yPoints = {cy - half * 0.5, cy - half * 1.2, cy - half * 0.5};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Highlight grid cell
        gc.setFill(Color.web("#ffffff", 0.2));
        gc.fillRect(gx * Constants.TILE_SIZE, gy * Constants.TILE_SIZE, 
                    Constants.TILE_SIZE, Constants.TILE_SIZE);
        gc.setStroke(Color.web("#4ecca3", 0.6));
        gc.setLineWidth(1);
        gc.strokeRect(gx * Constants.TILE_SIZE, gy * Constants.TILE_SIZE, 
                      Constants.TILE_SIZE, Constants.TILE_SIZE);
    }
    
    private double getTowerRangePixels(TowerType type) {
        switch (type) {
            case ARROW: return 4 * Constants.TOWER_RANGE_UNIT;
            case MAGIC: return 3 * Constants.TOWER_RANGE_UNIT;
            case SIEGE: return 6 * Constants.TOWER_RANGE_UNIT;
            case SUPPORT: return 3.5 * Constants.TOWER_RANGE_UNIT;
            default: return 3 * Constants.TOWER_RANGE_UNIT;
        }
    }
    
    private Color getTowerColor(TowerType type) {
        switch (type) {
            case ARROW: return Color.web("#22d3ee");
            case MAGIC: return Color.web("#a855f7");
            case SIEGE: return Color.web("#f97316");
            case SUPPORT: return Color.web("#4ecca3");
            default: return Color.GRAY;
        }
    }

    // Getters and setters
    public void setHero(Hero hero) { this.hero = hero; }
    public void setEnemies(List<Enemy> enemies) { this.enemies = enemies; }
    public void setTowers(List<Tower> towers) { this.towers = towers; }
    public void setBonePiles(List<BonePile> bonePiles) { this.bonePiles = bonePiles; }
    public Hero getHero() { return hero; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<BonePile> getBonePiles() { return bonePiles; }
    public MiniMap getMiniMap() { return miniMap; }

    /**
     * Updates HUD data each frame from GameGUI.
     */
    public void updateHUD(double[] cooldowns, int mana, double time, int wave, int enemies) {
        this.abilityCooldowns = cooldowns;
        this.heroMana = mana;
        this.runTime = time;
        this.waveNumber = wave;
        this.enemyCount = enemies;
    }

    public void setSellConfirmActive(boolean active) { this.sellConfirmActive = active; }

    /**
     * Draws the in-game HUD: ability cooldowns (bottom-left) + run timer (top-left) + wave info (top-center).
     */
    private void drawHUD(GraphicsContext gc) {
        // --- Run timer (top-left) ---
        int mins = (int)(runTime / 60);
        int secs = (int)(runTime % 60);
        String timeStr = String.format("%02d:%02d", mins, secs);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#1a1a2e", 0.7));
        gc.fillRoundRect(8, 8, 70, 24, 6, 6);
        gc.setFill(Color.web("#eee"));
        gc.fillText(timeStr, 16, 25);

        // --- Wave info (top-center) ---
        String waveStr = "Wave " + waveNumber + "  Enemies: " + enemyCount;
        gc.setFill(Color.web("#1a1a2e", 0.7));
        double waveW = waveStr.length() * 8.5 + 16;
        gc.fillRoundRect(getWidth() / 2 - waveW / 2, 8, waveW, 24, 6, 6);
        gc.setFill(Color.web("#eee"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.fillText(waveStr, getWidth() / 2 - waveW / 2 + 8, 25);

        // --- Ability cooldown icons (bottom-left) ---
        String[] labels = {"Q", "W", "E", "R"};
        Color[] colors = {Color.web("#e94560"), Color.web("#f97316"), Color.web("#4ecca3"), Color.web("#a855f7")};
        double iconSize = 36;
        double gap = 6;
        double startX = 10;
        double startY = getHeight() - iconSize - 10;

        for (int i = 0; i < 4; i++) {
            double ix = startX + i * (iconSize + gap);
            double cd = abilityCooldowns[i];
            double maxCd = abilityMaxCooldowns[i];
            boolean canAfford = heroMana >= abilityManaCosts[i];
            boolean onCooldown = cd > 0;

            // Background
            gc.setFill(Color.web("#1a1a2e", 0.8));
            gc.fillRoundRect(ix, startY, iconSize, iconSize, 6, 6);

            // Cooldown overlay (darkened portion from top)
            if (onCooldown) {
                double cdFrac = cd / maxCd;
                gc.setFill(Color.web("#000000", 0.5));
                gc.fillRect(ix + 2, startY + 2, iconSize - 4, (iconSize - 4) * cdFrac);
            }

            // Border color
            Color borderColor = onCooldown ? Color.web("#555") : (canAfford ? colors[i] : Color.web("#666"));
            gc.setStroke(borderColor);
            gc.setLineWidth(2);
            gc.strokeRoundRect(ix, startY, iconSize, iconSize, 6, 6);

            // Key label
            gc.setFill(canAfford && !onCooldown ? Color.WHITE : Color.web("#888"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
            gc.fillText(labels[i], ix + iconSize / 2 - 5, startY + iconSize / 2 + 5);

            // Mana cost
            gc.setFont(Font.font("Monospaced", 9));
            gc.setFill(Color.web("#3b82f6"));
            gc.fillText(String.valueOf(abilityManaCosts[i]), ix + 2, startY + iconSize - 4);

            // Cooldown remaining text
            if (onCooldown) {
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
                gc.setFill(Color.web("#ff4444"));
                gc.fillText(String.format("%.1f", cd), ix + iconSize / 2 - 10, startY + iconSize - 4);
            }
        }
    }
}
