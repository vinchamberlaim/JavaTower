package javatower.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javatower.entities.Enemy;
import javatower.entities.Tower;

/**
 * Renders pixel-art sprites for enemies and towers.
 * Supports idle animations and multiple animation states.
 */
public class PixelArtRenderer {
    
    // Animation timing
    private double animationTimer = 0;
    private static final double ANIMATION_SPEED = 8.0; // bobs per second
    
    /**
     * Updates animation timer. Call every frame.
     */
    public void update(double dt) {
        animationTimer += dt * ANIMATION_SPEED;
    }
    
    /**
     * Gets current bob offset for idle animation (0 to 1 range).
     */
    private double getBobOffset() {
        return Math.sin(animationTimer) * 0.5 + 0.5;
    }
    
    /**
     * Draws a pixel-art enemy sprite.
     */
    public void drawEnemySprite(GraphicsContext gc, Enemy enemy, double x, double y) {
        double scale = enemy.getRadius() / 10.0; // Scale based on enemy size
        double bobY = getBobOffset() * 2 * scale; // Idle bob animation
        
        switch (enemy.getType()) {
            case ZOMBIE: drawZombie(gc, x, y - bobY, scale, enemy); break;
            case SKELETON: drawSkeleton(gc, x, y - bobY, scale, enemy); break;
            case GHOUL: drawGhoul(gc, x, y - bobY, scale, enemy); break;
            case WIGHT: drawWight(gc, x, y - bobY, scale, enemy); break;
            case WRAITH: drawWraith(gc, x, y - bobY, scale, enemy); break;
            case REVENANT: drawRevenant(gc, x, y - bobY, scale, enemy); break;
            case DEATH_KNIGHT: drawDeathKnight(gc, x, y - bobY, scale, enemy); break;
            case LICH: drawLich(gc, x, y - bobY, scale, enemy); break;
            case CULTIST: drawCultist(gc, x, y - bobY, scale, enemy); break;
            case DEEP_ONE: drawDeepOne(gc, x, y - bobY, scale, enemy); break;
            case SHOGGOTH: drawShoggoth(gc, x, y - bobY, scale, enemy); break;
            case BONE_COLOSSUS: drawBoneColossus(gc, x, y - bobY, scale, enemy); break;
            case NECROMANCER_KING: drawNecromancerKing(gc, x, y - bobY, scale, enemy); break;
            default: drawDefaultEnemy(gc, x, y - bobY, scale, enemy);
        }
    }
    
    /**
     * Draws a pixel-art tower sprite.
     */
    public void drawTowerSprite(GraphicsContext gc, Tower tower, double x, double y) {
        double scale = 3.0; // Fixed scale for towers
        
        switch (tower.getType()) {
            case ARROW: drawArrowTower(gc, x, y, scale, tower); break;
            case MAGIC: drawMagicTower(gc, x, y, scale, tower); break;
            case SIEGE: drawSiegeTower(gc, x, y, scale, tower); break;
            case SUPPORT: drawSupportTower(gc, x, y, scale, tower); break;
        }
    }
    
    // ========== ENEMY SPRITES ==========
    
    private void drawZombie(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color skin = Color.web("#5a7a5a");
        Color clothes = Color.web("#3d5c3d");
        
        // Body (bobbing)
        double bob = getBobOffset() * 2;
        
        // Legs
        gc.setFill(clothes);
        gc.fillRect(x - 4*scale, y + 8*scale, 3*scale, 6*scale);
        gc.fillRect(x + 1*scale, y + 8*scale, 3*scale, 6*scale);
        
        // Torso
        gc.setFill(clothes);
        gc.fillRect(x - 5*scale, y + 2*scale, 10*scale, 8*scale);
        
        // Arms (outstretched)
        gc.setFill(skin);
        gc.fillRect(x - 9*scale, y + 3*scale, 4*scale, 3*scale);
        gc.fillRect(x + 5*scale, y + 3*scale, 4*scale, 3*scale);
        
        // Head
        gc.setFill(skin);
        gc.fillRect(x - 4*scale, y - 6*scale + bob, 8*scale, 8*scale);
        
        // Eyes (red, glowing)
        gc.setFill(Color.web("#ff0000", 0.8));
        gc.fillRect(x - 2*scale, y - 3*scale + bob, 2*scale, 2*scale);
        gc.fillRect(x + 1*scale, y - 3*scale + bob, 2*scale, 2*scale);
    }
    
    private void drawSkeleton(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color bone = Color.web("#e8dcc8");
        
        // Legs
        gc.setFill(bone);
        gc.fillRect(x - 3*scale, y + 8*scale, 2*scale, 6*scale);
        gc.fillRect(x + 1*scale, y + 8*scale, 2*scale, 6*scale);
        
        // Pelvis
        gc.fillRect(x - 3*scale, y + 6*scale, 6*scale, 3*scale);
        
        // Spine
        gc.fillRect(x - 1*scale, y, 2*scale, 8*scale);
        
        // Ribs
        gc.fillRect(x - 4*scale, y + 2*scale, 8*scale, 1*scale);
        gc.fillRect(x - 3*scale, y + 4*scale, 6*scale, 1*scale);
        
        // Arms (holding bow)
        gc.fillRect(x - 6*scale, y + 1*scale, 4*scale, 2*scale);
        gc.fillRect(x + 2*scale, y + 1*scale, 4*scale, 2*scale);
        
        // Skull
        double bob = getBobOffset() * 1.5;
        gc.fillRect(x - 3*scale, y - 5*scale + bob, 6*scale, 6*scale);
        
        // Eye sockets
        gc.setFill(Color.BLACK);
        gc.fillRect(x - 2*scale, y - 2*scale + bob, 1.5*scale, 1.5*scale);
        gc.fillRect(x + 0.5*scale, y - 2*scale + bob, 1.5*scale, 1.5*scale);
    }
    
    private void drawGhoul(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color skin = Color.web("#8b4513"); // Brown, decayed
        
        // Crouched pose
        double bob = getBobOffset() * 3;
        
        // Legs (bent)
        gc.setFill(skin);
        gc.fillRect(x - 5*scale, y + 6*scale, 4*scale, 4*scale);
        gc.fillRect(x + 1*scale, y + 6*scale, 4*scale, 4*scale);
        
        // Body (hunched)
        gc.fillRect(x - 4*scale, y + 2*scale, 8*scale, 6*scale);
        
        // Arms (long, dragging)
        gc.fillRect(x - 7*scale, y + 4*scale, 3*scale, 8*scale);
        gc.fillRect(x + 4*scale, y + 4*scale, 3*scale, 8*scale);
        
        // Head (forward)
        gc.fillRect(x - 3*scale, y - 4*scale + bob, 6*scale, 6*scale);
        
        // Claws
        gc.setFill(Color.web("#2a1810"));
        gc.fillRect(x - 8*scale, y + 11*scale, 1*scale, 3*scale);
        gc.fillRect(x + 7*scale, y + 11*scale, 1*scale, 3*scale);
    }
    
    private void drawWight(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color armor = Color.web("#4a5568");
        Color glow = Color.web("#63b3ed", 0.6);
        
        double bob = getBobOffset() * 2;
        
        // Armored legs
        gc.setFill(armor);
        gc.fillRect(x - 4*scale, y + 6*scale, 3*scale, 6*scale);
        gc.fillRect(x + 1*scale, y + 6*scale, 3*scale, 6*scale);
        
        // Armored body
        gc.fillRect(x - 5*scale, y, 10*scale, 8*scale);
        
        // Shoulders
        gc.fillRect(x - 7*scale, y, 3*scale, 4*scale);
        gc.fillRect(x + 4*scale, y, 3*scale, 4*scale);
        
        // Helmet
        gc.fillRect(x - 4*scale, y - 6*scale + bob, 8*scale, 7*scale);
        
        // Eye glow
        gc.setFill(glow);
        gc.fillRect(x - 2*scale, y - 2*scale + bob, 1.5*scale, 1.5*scale);
        gc.fillRect(x + 0.5*scale, y - 2*scale + bob, 1.5*scale, 1.5*scale);
    }
    
    private void drawWraith(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color body = Color.web("#6b46c1", 0.7); // Translucent purple
        
        // Floating - more dramatic bob
        double bob = getBobOffset() * 4;
        
        // Ghostly tail
        gc.setFill(Color.web("#6b46c1", 0.3));
        gc.fillRect(x - 2*scale, y + 8*scale - bob, 4*scale, 6*scale);
        
        // Body (tattered robes)
        gc.setFill(body);
        gc.fillRect(x - 4*scale, y - bob, 8*scale, 10*scale);
        
        // Hood
        gc.fillRect(x - 4*scale, y - 6*scale - bob, 8*scale, 7*scale);
        
        // Glowing eyes
        gc.setFill(Color.web("#fbbf24"));
        gc.fillRect(x - 2*scale, y - 2*scale - bob, 1.5*scale, 1.5*scale);
        gc.fillRect(x + 0.5*scale, y - 2*scale - bob, 1.5*scale, 1.5*scale);
    }
    
    private void drawRevenant(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color dark = Color.web("#1a202c");
        Color glow = Color.web("#f56565");
        
        double bob = getBobOffset() * 2;
        
        // Shadowy body
        gc.setFill(dark);
        gc.fillRect(x - 5*scale, y, 10*scale, 10*scale);
        
        // Spikes on shoulders
        gc.fillRect(x - 7*scale, y - 2*scale, 2*scale, 4*scale);
        gc.fillRect(x + 5*scale, y - 2*scale, 2*scale, 4*scale);
        
        // Glowing skull
        gc.setFill(glow);
        gc.fillRect(x - 3*scale, y - 5*scale + bob, 6*scale, 6*scale);
        
        // Eye sockets (dark)
        gc.setFill(Color.BLACK);
        gc.fillRect(x - 2*scale, y - 2*scale + bob, 1.5*scale, 2*scale);
        gc.fillRect(x + 0.5*scale, y - 2*scale + bob, 1.5*scale, 2*scale);
    }
    
    private void drawDeathKnight(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color armor = Color.web("#2d3748");
        Color trim = Color.web("#e53e3e");
        
        double bob = getBobOffset() * 1.5;
        
        // Heavy armored legs
        gc.setFill(armor);
        gc.fillRect(x - 5*scale, y + 5*scale, 4*scale, 8*scale);
        gc.fillRect(x + 1*scale, y + 5*scale, 4*scale, 8*scale);
        
        // Heavy body
        gc.fillRect(x - 6*scale, y - 2*scale, 12*scale, 9*scale);
        
        // Red trim
        gc.setFill(trim);
        gc.fillRect(x - 6*scale, y - 2*scale, 12*scale, 2*scale);
        gc.fillRect(x - 6*scale, y + 5*scale, 12*scale, 2*scale);
        
        // Helmet with horns
        gc.setFill(armor);
        gc.fillRect(x - 4*scale, y - 7*scale + bob, 8*scale, 7*scale);
        gc.fillRect(x - 6*scale, y - 6*scale + bob, 2*scale, 4*scale);
        gc.fillRect(x + 4*scale, y - 6*scale + bob, 2*scale, 4*scale);
        
        // Glowing red eyes
        gc.setFill(Color.web("#ff0000"));
        gc.fillRect(x - 2*scale, y - 3*scale + bob, 1.5*scale, 1.5*scale);
        gc.fillRect(x + 0.5*scale, y - 3*scale + bob, 1.5*scale, 1.5*scale);
    }
    
    private void drawLich(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color robe = Color.web("#553c9a");
        Color bone = Color.web("#f7fafc");
        
        double bob = getBobOffset() * 2;
        
        // Robes
        gc.setFill(robe);
        gc.fillRect(x - 4*scale, y + 2*scale, 8*scale, 10*scale);
        
        // Skeleton hands holding staff
        gc.setFill(bone);
        gc.fillRect(x + 4*scale, y + 3*scale, 2*scale, 6*scale);
        
        // Staff
        gc.setFill(Color.web("#744210"));
        gc.fillRect(x + 7*scale, y - 4*scale, 2*scale, 14*scale);
        
        // Staff gem (glowing)
        gc.setFill(Color.web("#9f7aea", 0.8 + getBobOffset() * 0.2));
        gc.fillOval(x + 5*scale, y - 6*scale, 6*scale, 6*scale);
        
        // Skull head
        gc.setFill(bone);
        gc.fillRect(x - 3*scale, y - 5*scale + bob, 6*scale, 7*scale);
        
        // Crown
        gc.setFill(Color.web("#d69e2e"));
        gc.fillRect(x - 3*scale, y - 7*scale + bob, 6*scale, 3*scale);
        gc.fillRect(x - 2*scale, y - 9*scale + bob, 1*scale, 3*scale);
        gc.fillRect(x + 1*scale, y - 9*scale + bob, 1*scale, 3*scale);
    }
    
    private void drawCultist(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color robe = Color.web("#2d1b4d");
        Color eye = Color.web("#34d399");
        double bob = getBobOffset() * 2;
        gc.setFill(robe);
        gc.fillRect(x - 4 * scale, y, 8 * scale, 11 * scale);
        gc.fillRect(x - 5 * scale, y - 5 * scale + bob, 10 * scale, 6 * scale);
        gc.setFill(eye);
        gc.fillRect(x - 2 * scale, y - 2 * scale + bob, 1.5 * scale, 1.5 * scale);
        gc.fillRect(x + 0.5 * scale, y - 2 * scale + bob, 1.5 * scale, 1.5 * scale);
    }
    
    private void drawDeepOne(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color skin = Color.web("#0f766e");
        Color fin = Color.web("#134e4a");
        double bob = getBobOffset() * 1.5;
        gc.setFill(skin);
        gc.fillRect(x - 5 * scale, y + 2 * scale, 10 * scale, 9 * scale);
        gc.fillRect(x - 4 * scale, y - 4 * scale + bob, 8 * scale, 7 * scale);
        gc.setFill(fin);
        gc.fillRect(x - 7 * scale, y + 2 * scale, 2 * scale, 5 * scale);
        gc.fillRect(x + 5 * scale, y + 2 * scale, 2 * scale, 5 * scale);
        gc.fillRect(x - 1 * scale, y - 6 * scale + bob, 2 * scale, 2 * scale);
    }
    
    private void drawShoggoth(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color mass = Color.web("#111827");
        Color glow = Color.web("#22d3ee");
        double bob = getBobOffset() * 1.0;
        gc.setFill(mass);
        gc.fillRect(x - 8 * scale, y - 1 * scale, 16 * scale, 12 * scale);
        gc.fillRect(x - 7 * scale, y - 6 * scale + bob, 14 * scale, 6 * scale);
        gc.setFill(glow);
        gc.fillRect(x - 4 * scale, y - 3 * scale + bob, 2 * scale, 2 * scale);
        gc.fillRect(x - 1 * scale, y - 2 * scale + bob, 2 * scale, 2 * scale);
        gc.fillRect(x + 2 * scale, y - 3 * scale + bob, 2 * scale, 2 * scale);
    }
    
    private void drawBoneColossus(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color bone = Color.web("#e2e8f0");
        
        double bob = getBobOffset() * 1; // Slow, heavy movement
        
        // Massive legs
        gc.setFill(bone);
        gc.fillRect(x - 8*scale, y + 8*scale, 6*scale, 10*scale);
        gc.fillRect(x + 2*scale, y + 8*scale, 6*scale, 10*scale);
        
        // Massive body
        gc.fillRect(x - 10*scale, y - 2*scale, 20*scale, 12*scale);
        
        // Ribcage visible
        gc.setFill(Color.web("#cbd5e0"));
        gc.fillRect(x - 8*scale, y + 2*scale, 2*scale, 6*scale);
        gc.fillRect(x - 4*scale, y + 2*scale, 2*scale, 6*scale);
        gc.fillRect(x + 2*scale, y + 2*scale, 2*scale, 6*scale);
        gc.fillRect(x + 6*scale, y + 2*scale, 2*scale, 6*scale);
        
        // Giant skull
        gc.setFill(bone);
        gc.fillRect(x - 7*scale, y - 8*scale + bob, 14*scale, 10*scale);
        
        // Glowing eye
        gc.setFill(Color.web("#48bb78"));
        gc.fillOval(x - 2*scale, y - 4*scale + bob, 4*scale, 4*scale);
    }
    
    private void drawNecromancerKing(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        Color robe = Color.web("#1a202c");
        Color gold = Color.web("#d69e2e");
        
        // Complex multi-part boss
        double bob = getBobOffset() * 2;
        
        // Large flowing robes
        gc.setFill(robe);
        gc.fillRect(x - 8*scale, y + 4*scale, 16*scale, 14*scale);
        
        // Gold trim
        gc.setFill(gold);
        gc.fillRect(x - 8*scale, y + 4*scale, 16*scale, 3*scale);
        gc.fillRect(x - 8*scale, y + 14*scale, 16*scale, 2*scale);
        
        // Shoulder pads
        gc.fillRect(x - 10*scale, y + 2*scale, 4*scale, 6*scale);
        gc.fillRect(x + 6*scale, y + 2*scale, 4*scale, 6*scale);
        
        // Crowned helmet
        gc.setFill(robe);
        gc.fillRect(x - 5*scale, y - 6*scale + bob, 10*scale, 10*scale);
        
        // Crown
        gc.setFill(gold);
        gc.fillRect(x - 5*scale, y - 8*scale + bob, 10*scale, 4*scale);
        for (int i = -4; i <= 4; i += 2) {
            gc.fillRect(x + i*scale, y - 11*scale + bob, 1*scale, 4*scale);
        }
        
        // Glowing green eyes
        gc.setFill(Color.web("#48bb78", 0.8 + getBobOffset() * 0.2));
        gc.fillRect(x - 3*scale, y - 2*scale + bob, 2*scale, 2*scale);
        gc.fillRect(x + 1*scale, y - 2*scale + bob, 2*scale, 2*scale);
        
        // Floating orbs (animated)
        double orbBob = Math.sin(animationTimer * 1.5) * 3;
        gc.setFill(Color.web("#9f7aea", 0.6));
        gc.fillOval(x - 12*scale, y + orbBob*scale, 4*scale, 4*scale);
        gc.fillOval(x + 8*scale, y - orbBob*scale, 4*scale, 4*scale);
    }
    
    private void drawDefaultEnemy(GraphicsContext gc, double x, double y, double scale, Enemy e) {
        // Fallback to colored circle
        gc.setFill(Color.GRAY);
        gc.fillOval(x - e.getRadius(), y - e.getRadius(), e.getRadius()*2, e.getRadius()*2);
    }
    
    // ========== TOWER SPRITES ==========
    
    private void drawArrowTower(GraphicsContext gc, double x, double y, double scale, Tower t) {
        Color wood = Color.web("#8b4513");
        Color roof = Color.web("#654321");
        
        // Base
        gc.setFill(wood);
        gc.fillRect(x - 6*scale, y, 12*scale, 10*scale);
        
        // Roof (triangle)
        gc.setFill(roof);
        double[] roofX = {x - 8*scale, x, x + 8*scale};
        double[] roofY = {y, y - 10*scale, y};
        gc.fillPolygon(roofX, roofY, 3);
        
        // Bow on top
        gc.setFill(Color.web("#d4a574"));
        gc.fillRect(x - 1*scale, y - 8*scale, 2*scale, 8*scale);
        gc.fillArc(x - 6*scale, y - 10*scale, 12*scale, 8*scale, 0, 180, javafx.scene.shape.ArcType.OPEN);
        
        // Level indicator
        if (t.getUpgradeLevel() > 0) {
            gc.setFill(Color.web("#ffd700"));
            gc.fillRect(x - 2*scale, y + 2*scale, 4*scale, 4*scale);
        }
    }
    
    private void drawMagicTower(GraphicsContext gc, double x, double y, double scale, Tower t) {
        Color stone = Color.web("#4a5568");
        Color crystal = Color.web("#9f7aea", 0.8 + getBobOffset() * 0.2);
        
        // Crystal base
        gc.setFill(stone);
        gc.fillRect(x - 5*scale, y + 4*scale, 10*scale, 6*scale);
        
        // Floating crystal (animated)
        double floatY = y - 4*scale + getBobOffset() * 2;
        gc.setFill(crystal);
        gc.fillOval(x - 5*scale, floatY, 10*scale, 10*scale);
        
        // Inner glow
        gc.setFill(Color.web("#e9d8fd", 0.6));
        gc.fillOval(x - 3*scale, floatY + 2*scale, 6*scale, 6*scale);
    }
    
    private void drawSiegeTower(GraphicsContext gc, double x, double y, double scale, Tower t) {
        Color metal = Color.web("#2d3748");
        Color rust = Color.web("#744210");
        
        // Heavy base
        gc.setFill(metal);
        gc.fillRect(x - 8*scale, y + 2*scale, 16*scale, 8*scale);
        
        // Rusty trim
        gc.setFill(rust);
        gc.fillRect(x - 8*scale, y + 2*scale, 16*scale, 2*scale);
        
        // Cannon barrel
        gc.setFill(rust);
        gc.fillRect(x - 3*scale, y - 8*scale, 6*scale, 10*scale);
        
        // Cannon opening
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 3*scale, y - 10*scale, 6*scale, 4*scale);
    }
    
    private void drawSupportTower(GraphicsContext gc, double x, double y, double scale, Tower t) {
        Color white = Color.web("#f7fafc");
        Color glow = Color.web("#4fd1c5", 0.4 + getBobOffset() * 0.3);
        
        // Pillar
        gc.setFill(white);
        gc.fillRect(x - 4*scale, y, 8*scale, 10*scale);
        
        // Floating aura ring (animated)
        double ringScale = 1.0 + getBobOffset() * 0.2;
        gc.setStroke(glow);
        gc.setLineWidth(2);
        gc.strokeOval(x - 8*scale*ringScale, y - 8*scale, 16*scale*ringScale, 6*scale);
        
        // Glow center
        gc.setFill(glow);
        gc.fillOval(x - 3*scale, y + 2*scale, 6*scale, 6*scale);
    }
}
