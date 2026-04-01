package javatower.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javatower.entities.Hero;
import javatower.entities.Enemy;
import javatower.entities.Tower;
import javatower.util.Constants;
import java.util.List;

/**
 * Mini-map overlay showing a scaled-down view of the game world.
 * Displays hero, towers, and enemies as colored dots.
 */
public class MiniMap {
    
    // Mini-map dimensions and position (bottom-left corner)
    public static final int WIDTH = 150;
    public static final int HEIGHT = 100;
    public static final int MARGIN = 10;
    public static final int X_POS = MARGIN;
    public static final int Y_POS = Constants.SCREEN_HEIGHT - HEIGHT - MARGIN;
    
    // Scale factors to convert world coords to mini-map coords
    private final double scaleX;
    private final double scaleY;
    
    // Toggle visibility
    private boolean visible = true;
    
    public MiniMap() {
        this.scaleX = (double) WIDTH / Constants.SCREEN_WIDTH;
        this.scaleY = (double) HEIGHT / Constants.SCREEN_HEIGHT;
    }
    
    /**
     * Renders the mini-map on the game canvas.
     */
    public void render(GraphicsContext gc, Hero hero, List<Enemy> enemies, List<Tower> towers) {
        if (!visible) return;
        
        // Draw background
        gc.setFill(Color.web("#0a0a1a", 0.85));
        gc.fillRect(X_POS, Y_POS, WIDTH, HEIGHT);
        
        // Draw border
        gc.setStroke(Color.web("#4ecca3", 0.6));
        gc.setLineWidth(2);
        gc.strokeRect(X_POS, Y_POS, WIDTH, HEIGHT);
        
        // Draw towers (blue dots)
        if (towers != null) {
            gc.setFill(Color.web("#22d3ee"));
            for (Tower t : towers) {
                if (t.isAlive()) {
                    int mx = X_POS + (int)(t.getX() * scaleX);
                    int my = Y_POS + (int)(t.getY() * scaleY);
                    gc.fillOval(mx - 2, my - 2, 4, 4);
                }
            }
        }
        
        // Draw enemies (red dots, size based on tier)
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isAlive()) {
                    int mx = X_POS + (int)(e.getX() * scaleX);
                    int my = Y_POS + (int)(e.getY() * scaleY);
                    
                    // Color based on enemy tier
                    Color color = getEnemyColor(e.getType().tier);
                    gc.setFill(color);
                    
                    // Size based on tier (1-3 pixels)
                    int size = Math.min(3, 1 + e.getType().tier / 4);
                    gc.fillOval(mx - size, my - size, size * 2, size * 2);
                }
            }
        }
        
        // Draw hero (green dot with white outline)
        if (hero != null && hero.isAlive()) {
            int mx = X_POS + (int)(hero.getX() * scaleX);
            int my = Y_POS + (int)(hero.getY() * scaleY);
            
            // White outline
            gc.setFill(Color.WHITE);
            gc.fillOval(mx - 4, my - 4, 8, 8);
            
            // Green center
            gc.setFill(Color.web("#4ecca3"));
            gc.fillOval(mx - 3, my - 3, 6, 6);
        }
        
        // Draw wave info text
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 9));
        gc.fillText("MAP", X_POS + 5, Y_POS + 12);
    }
    
    /**
     * Gets color for enemy based on tier.
     */
    private Color getEnemyColor(int tier) {
        switch (tier) {
            case 1: case 2: return Color.web("#c4c4c4");  // Gray
            case 3: case 4: return Color.web("#e94560");  // Red
            case 5: case 6: return Color.web("#a855f7");  // Purple
            case 7: case 8: return Color.web("#f97316");  // Orange
            case 9: return Color.web("#eab308");          // Gold
            case 10: return Color.web("#dc2626");         // Dark red (boss)
            default: return Color.GRAY;
        }
    }
    
    /**
     * Toggles mini-map visibility.
     */
    public void toggle() {
        visible = !visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Checks if a screen coordinate is inside the mini-map area.
     * Useful for detecting clicks on the mini-map.
     */
    public boolean contains(double screenX, double screenY) {
        return screenX >= X_POS && screenX <= X_POS + WIDTH &&
               screenY >= Y_POS && screenY <= Y_POS + HEIGHT;
    }
    
    /**
     * Converts a mini-map click to world coordinates.
     * Returns null if click is outside mini-map.
     */
    public double[] miniMapToWorld(double screenX, double screenY) {
        if (!contains(screenX, screenY)) return null;
        
        double worldX = (screenX - X_POS) / scaleX;
        double worldY = (screenY - Y_POS) / scaleY;
        return new double[]{worldX, worldY};
    }
}
