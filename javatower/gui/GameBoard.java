package javatower.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javatower.entities.Entity;
import javatower.entities.Hero;
import javatower.entities.Enemy;
import javatower.entities.Tower;
import javatower.entities.BonePile;
import javatower.util.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the game world using pixel coordinates.
 */
public class GameBoard extends Canvas {
    private Hero hero;
    private List<Enemy> enemies;
    private List<Tower> towers;
    private List<BonePile> bonePiles = new ArrayList<>();

    public GameBoard() {
        setWidth(Constants.SCREEN_WIDTH);
        setHeight(Constants.SCREEN_HEIGHT);
    }

    /**
     * Renders the board and all entities.
     */
    public void render(GraphicsContext gc) {
        // Background
        gc.setFill(Color.web("#0f3460"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawGrid(gc);

        // Draw bone piles (under everything else)
        for (BonePile bp : bonePiles) {
            drawBonePile(bp, gc);
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
    }

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

        // Body
        gc.setFill(Color.web("#4ecca3"));
        gc.fillOval(px - r, py - r, r * 2, r * 2);

        // Shield outline
        gc.setStroke(Color.web("#e94560"));
        gc.setLineWidth(2);
        gc.strokeOval(px - r - 2, py - r - 2, r * 2 + 4, r * 2 + 4);

        // Label
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText("H", px - 4, py + 4);

        drawHealthBar(h, gc);
    }

    private void drawEnemy(Enemy e, GraphicsContext gc) {
        double px = e.getX();
        double py = e.getY();
        double r = e.getRadius();

        // Color based on tier
        Color color;
        switch (e.getType().tier) {
            case 1: case 2: color = Color.web("#c4c4c4"); break;
            case 3: case 4: color = Color.web("#e94560"); break;
            case 5: case 6: color = Color.web("#a855f7"); break;
            case 7: case 8: color = Color.web("#f97316"); break;
            case 9: color = Color.web("#eab308"); break;
            case 10: color = Color.web("#dc2626"); break;
            default: color = Color.GRAY; break;
        }

        gc.setFill(color);
        gc.fillOval(px - r, py - r, r * 2, r * 2);

        // Border
        gc.setStroke(color.darker());
        gc.setLineWidth(1.5);
        gc.strokeOval(px - r, py - r, r * 2, r * 2);

        // Name abbreviation
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Monospaced", Math.max(8, r * 0.7)));
        String label = e.getName().length() >= 2 ? e.getName().substring(0, 2).toUpperCase() : e.getName().toUpperCase();
        gc.fillText(label, px - r * 0.5, py + r * 0.25);

        drawHealthBar(e, gc);
    }

    private void drawTower(Tower t, GraphicsContext gc) {
        double px = t.getX();
        double py = t.getY();

        Color color;
        switch (t.getType()) {
            case ARROW: color = Color.web("#22d3ee"); break;
            case MAGIC: color = Color.web("#a855f7"); break;
            case SIEGE: color = Color.web("#f97316"); break;
            case SUPPORT: color = Color.web("#4ecca3"); break;
            default: color = Color.GRAY;
        }

        double half = Constants.TILE_SIZE * 0.4;

        // Tower base
        gc.setFill(color);
        gc.fillRect(px - half, py - half * 0.5, half * 2, half * 1.5);

        // Tower top (triangle)
        gc.setFill(color.darker());
        double[] xPoints = {px - half * 0.7, px, px + half * 0.7};
        double[] yPoints = {py - half * 0.5, py - half * 1.2, py - half * 0.5};
        gc.fillPolygon(xPoints, yPoints, 3);

        // Level indicator
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText("L" + t.getUpgradeLevel(), px - 8, py + 4);
    }

    private void drawTowerRange(Tower t, GraphicsContext gc) {
        double rangePx = t.getRangePixels();
        gc.setStroke(Color.web("#ffffff", 0.1));
        gc.setLineWidth(1);
        gc.strokeOval(t.getX() - rangePx, t.getY() - rangePx, rangePx * 2, rangePx * 2);
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

    // Getters and setters
    public void setHero(Hero hero) { this.hero = hero; }
    public void setEnemies(List<Enemy> enemies) { this.enemies = enemies; }
    public void setTowers(List<Tower> towers) { this.towers = towers; }
    public void setBonePiles(List<BonePile> bonePiles) { this.bonePiles = bonePiles; }
    public Hero getHero() { return hero; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<BonePile> getBonePiles() { return bonePiles; }
}
