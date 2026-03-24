package javatower.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Lightweight visual effects rendered on the game canvas:
 * projectiles, slash arcs, damage numbers, impact bursts, spell effects.
 */
public class VisualEffect {

    public enum EffectType {
        ARROW,          // Arrow projectile flying to target
        FIREBALL,       // Fireball with trail
        NECRO_BOLT,     // Dark energy bolt
        HOLY_SMITE,     // Golden beam/pulse
        MELEE_SLASH,    // Arc slash at target position
        DAMAGE_NUMBER,  // Floating damage text
        HEAL_NUMBER,    // Floating green heal text
        IMPACT_BURST,   // Small explosion on hit
        FIRE_SPLASH,    // AoE fire ring
        TOWER_ARROW,    // Arrow from tower
        TOWER_MAGIC,    // Magic bolt from tower
        TOWER_SIEGE,    // Siege boulder from tower
        GOLD_PICKUP     // Gold text floating up
    }

    private EffectType type;
    private double x, y;           // current position
    private double targetX, targetY; // destination (for projectiles)
    private double startX, startY;   // origin (for projectiles)
    private double vx, vy;          // velocity (for floating numbers)
    private double lifetime;         // total lifetime
    private double age;             // current age
    private double speed;           // projectile speed (pixels/sec)
    private String text;            // for damage/heal numbers
    private Color color;            // primary color
    private Color secondaryColor;   // trail/glow color
    private int value;              // damage value for display
    private boolean crit;           // crit hit (bigger text)
    private double angle;           // rotation for slash
    private boolean arrived;        // projectile reached target

    public VisualEffect(EffectType type) {
        this.type = type;
        this.age = 0;
        this.arrived = false;
    }

    // --- Factory methods ---

    public static VisualEffect createArrow(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.ARROW);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 400; e.lifetime = 2.0;
        e.color = Color.web("#f5e6ab");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createFireball(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.FIREBALL);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 300; e.lifetime = 2.0;
        e.color = Color.web("#ff6b35");
        e.secondaryColor = Color.web("#ffcc00");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createNecroBolt(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.NECRO_BOLT);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 350; e.lifetime = 2.0;
        e.color = Color.web("#8b5cf6");
        e.secondaryColor = Color.web("#1a0a2e");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createHolySmite(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.HOLY_SMITE);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 450; e.lifetime = 2.0;
        e.color = Color.web("#ffd700");
        e.secondaryColor = Color.web("#fffbe6");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createMeleeSlash(double tx, double ty, double angle) {
        VisualEffect e = new VisualEffect(EffectType.MELEE_SLASH);
        e.x = tx; e.y = ty;
        e.angle = angle;
        e.lifetime = 0.25;
        e.color = Color.web("#ffffff", 0.8);
        e.secondaryColor = Color.web("#e94560", 0.6);
        return e;
    }

    public static VisualEffect createDamageNumber(double x, double y, int damage, boolean crit) {
        VisualEffect e = new VisualEffect(EffectType.DAMAGE_NUMBER);
        e.x = x + (Math.random() - 0.5) * 20;
        e.y = y - 10;
        e.vx = (Math.random() - 0.5) * 30;
        e.vy = -60;
        e.lifetime = 0.8;
        e.value = damage;
        e.crit = crit;
        e.text = String.valueOf(damage);
        e.color = crit ? Color.web("#ff4444") : Color.WHITE;
        return e;
    }

    public static VisualEffect createHealNumber(double x, double y, int amount) {
        VisualEffect e = new VisualEffect(EffectType.HEAL_NUMBER);
        e.x = x + (Math.random() - 0.5) * 15;
        e.y = y - 5;
        e.vx = 0;
        e.vy = -40;
        e.lifetime = 0.7;
        e.value = amount;
        e.text = "+" + amount;
        e.color = Color.web("#4ecca3");
        return e;
    }

    public static VisualEffect createGoldPickup(double x, double y, int gold) {
        VisualEffect e = new VisualEffect(EffectType.GOLD_PICKUP);
        e.x = x; e.y = y - 15;
        e.vx = 0; e.vy = -50;
        e.lifetime = 0.9;
        e.text = "+" + gold + "g";
        e.color = Color.web("#eab308");
        return e;
    }

    public static VisualEffect createImpactBurst(double x, double y, Color color) {
        VisualEffect e = new VisualEffect(EffectType.IMPACT_BURST);
        e.x = x; e.y = y;
        e.lifetime = 0.3;
        e.color = color;
        return e;
    }

    public static VisualEffect createFireSplash(double x, double y) {
        VisualEffect e = new VisualEffect(EffectType.FIRE_SPLASH);
        e.x = x; e.y = y;
        e.lifetime = 0.4;
        e.color = Color.web("#ff6b35", 0.5);
        e.secondaryColor = Color.web("#ffcc00", 0.3);
        return e;
    }

    public static VisualEffect createTowerArrow(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.TOWER_ARROW);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 500; e.lifetime = 1.5;
        e.color = Color.web("#22d3ee");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createTowerMagic(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.TOWER_MAGIC);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 350; e.lifetime = 1.5;
        e.color = Color.web("#a855f7");
        e.secondaryColor = Color.web("#d4a5ff");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    public static VisualEffect createTowerSiege(double sx, double sy, double tx, double ty) {
        VisualEffect e = new VisualEffect(EffectType.TOWER_SIEGE);
        e.startX = sx; e.startY = sy; e.x = sx; e.y = sy;
        e.targetX = tx; e.targetY = ty;
        e.speed = 200; e.lifetime = 2.5;
        e.color = Color.web("#8b7355");
        e.angle = Math.atan2(ty - sy, tx - sx);
        return e;
    }

    // --- Update ---

    public boolean update(double dt) {
        age += dt;
        if (age >= lifetime) return true; // expired

        switch (type) {
            case ARROW: case FIREBALL: case NECRO_BOLT: case HOLY_SMITE:
            case TOWER_ARROW: case TOWER_MAGIC: case TOWER_SIEGE:
                if (!arrived) {
                    double dx = targetX - x;
                    double dy = targetY - y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < speed * dt) {
                        x = targetX; y = targetY;
                        arrived = true;
                    } else {
                        x += (dx / dist) * speed * dt;
                        y += (dy / dist) * speed * dt;
                        angle = Math.atan2(dy, dx);
                    }
                } else {
                    return true; // done on arrival
                }
                break;

            case DAMAGE_NUMBER: case HEAL_NUMBER: case GOLD_PICKUP:
                x += vx * dt;
                y += vy * dt;
                vy += 30 * dt; // slight gravity
                break;

            case MELEE_SLASH: case IMPACT_BURST: case FIRE_SPLASH:
                // purely time-based
                break;
        }
        return false;
    }

    // --- Render ---

    public void render(GraphicsContext gc) {
        double t = age / lifetime; // 0..1
        double alpha = Math.max(0, 1.0 - t);

        switch (type) {
            case ARROW: case TOWER_ARROW:
                renderArrow(gc, alpha);
                break;
            case FIREBALL:
                renderFireball(gc, alpha);
                break;
            case NECRO_BOLT:
                renderNecroBolt(gc, alpha);
                break;
            case HOLY_SMITE:
                renderHolySmite(gc, alpha);
                break;
            case MELEE_SLASH:
                renderMeleeSlash(gc, t);
                break;
            case DAMAGE_NUMBER:
                renderDamageNumber(gc, alpha);
                break;
            case HEAL_NUMBER: case GOLD_PICKUP:
                renderFloatingText(gc, alpha);
                break;
            case IMPACT_BURST:
                renderImpactBurst(gc, t);
                break;
            case FIRE_SPLASH:
                renderFireSplash(gc, t);
                break;
            case TOWER_MAGIC:
                renderMagicBolt(gc, alpha);
                break;
            case TOWER_SIEGE:
                renderSiegeBoulder(gc, alpha);
                break;
        }
    }

    private void renderArrow(GraphicsContext gc, double alpha) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));

        // Arrow shaft
        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.setLineWidth(2);
        gc.strokeLine(-10, 0, 4, 0);

        // Arrowhead
        gc.setFill(Color.color(1, 1, 1, alpha));
        gc.fillPolygon(new double[]{4, 8, 4}, new double[]{-3, 0, 3}, 3);

        // Fletching
        gc.setStroke(Color.color(0.8, 0.3, 0.2, alpha * 0.7));
        gc.setLineWidth(1);
        gc.strokeLine(-10, 0, -8, -3);
        gc.strokeLine(-10, 0, -8, 3);

        gc.restore();
    }

    private void renderFireball(GraphicsContext gc, double alpha) {
        // Trail particles
        for (int i = 0; i < 3; i++) {
            double trailX = x - Math.cos(angle) * (i * 6) + (Math.random() - 0.5) * 4;
            double trailY = y - Math.sin(angle) * (i * 6) + (Math.random() - 0.5) * 4;
            double trailAlpha = alpha * (1.0 - i * 0.3);
            double trailR = 3 - i * 0.5;
            gc.setFill(Color.color(1, 0.5, 0, trailAlpha * 0.5));
            gc.fillOval(trailX - trailR, trailY - trailR, trailR * 2, trailR * 2);
        }

        // Core fireball
        gc.setFill(Color.color(secondaryColor.getRed(), secondaryColor.getGreen(), secondaryColor.getBlue(), alpha));
        gc.fillOval(x - 5, y - 5, 10, 10);

        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillOval(x - 3, y - 3, 6, 6);

        // Bright center
        gc.setFill(Color.color(1, 1, 0.8, alpha * 0.8));
        gc.fillOval(x - 1.5, y - 1.5, 3, 3);
    }

    private void renderNecroBolt(GraphicsContext gc, double alpha) {
        // Dark trail
        for (int i = 0; i < 3; i++) {
            double trailX = x - Math.cos(angle) * (i * 5) + (Math.random() - 0.5) * 6;
            double trailY = y - Math.sin(angle) * (i * 5) + (Math.random() - 0.5) * 6;
            double trailAlpha = alpha * (1.0 - i * 0.3);
            gc.setFill(Color.color(0.1, 0.02, 0.2, trailAlpha * 0.4));
            gc.fillOval(trailX - 4, trailY - 4, 8, 8);
        }

        // Core
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillOval(x - 4, y - 4, 8, 8);

        // Inner glow
        gc.setFill(Color.color(0.7, 0.4, 1.0, alpha * 0.6));
        gc.fillOval(x - 2, y - 2, 4, 4);
    }

    private void renderHolySmite(GraphicsContext gc, double alpha) {
        // Golden glow trail
        for (int i = 0; i < 2; i++) {
            double trailX = x - Math.cos(angle) * (i * 6);
            double trailY = y - Math.sin(angle) * (i * 6);
            double trailAlpha = alpha * (1.0 - i * 0.4);
            gc.setFill(Color.color(1, 0.97, 0.9, trailAlpha * 0.3));
            gc.fillOval(trailX - 6, trailY - 6, 12, 12);
        }

        // Core light
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillOval(x - 4, y - 4, 8, 8);

        // Cross shape
        gc.setStroke(Color.color(1, 1, 0.9, alpha * 0.7));
        gc.setLineWidth(1.5);
        gc.strokeLine(x - 6, y, x + 6, y);
        gc.strokeLine(x, y - 6, x, y + 6);
    }

    private void renderMeleeSlash(GraphicsContext gc, double t) {
        double alpha = 1.0 - t;
        double sweep = 120; // degrees of arc
        double radius = 25 + t * 10;

        gc.save();
        gc.translate(x, y);

        // Slash arc — sweeps through 120 degrees
        double startAngle = Math.toDegrees(angle) - sweep / 2;

        // Outer glow
        gc.setStroke(Color.color(secondaryColor.getRed(), secondaryColor.getGreen(), secondaryColor.getBlue(), alpha * 0.4));
        gc.setLineWidth(6);
        gc.strokeArc(-radius - 2, -radius - 2, (radius + 2) * 2, (radius + 2) * 2,
                -startAngle - sweep * t, sweep * 0.7, javafx.scene.shape.ArcType.OPEN);

        // Main slash
        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.setLineWidth(3);
        gc.strokeArc(-radius, -radius, radius * 2, radius * 2,
                -startAngle - sweep * t, sweep * 0.5, javafx.scene.shape.ArcType.OPEN);

        gc.restore();
    }

    private void renderDamageNumber(GraphicsContext gc, double alpha) {
        int fontSize = crit ? 16 : 12;
        gc.setFont(Font.font("Monospaced", crit ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));

        // Shadow
        gc.setFill(Color.color(0, 0, 0, alpha * 0.5));
        gc.fillText(text, x + 1, y + 1);

        // Text
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillText(text, x, y);

        if (crit) {
            gc.setFill(Color.color(1, 1, 0, alpha * 0.6));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
            gc.fillText("CRIT!", x - 4, y - 12);
        }
    }

    private void renderFloatingText(GraphicsContext gc, double alpha) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0, 0, 0, alpha * 0.4));
        gc.fillText(text, x + 1, y + 1);
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillText(text, x, y);
    }

    private void renderImpactBurst(GraphicsContext gc, double t) {
        double alpha = 1.0 - t;
        double radius = 5 + t * 20;

        // Expanding ring
        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.6));
        gc.setLineWidth(2 - t * 1.5);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Inner flash
        if (t < 0.3) {
            double flashAlpha = (1.0 - t / 0.3) * 0.4;
            gc.setFill(Color.color(1, 1, 1, flashAlpha));
            double flashR = 6 * (1.0 - t / 0.3);
            gc.fillOval(x - flashR, y - flashR, flashR * 2, flashR * 2);
        }

        // Spark particles
        for (int i = 0; i < 5; i++) {
            double sparkAngle = (Math.PI * 2 / 5) * i + t * 2;
            double sparkDist = radius * 0.8;
            double sx = x + Math.cos(sparkAngle) * sparkDist;
            double sy = y + Math.sin(sparkAngle) * sparkDist;
            double sparkR = 1.5 * alpha;
            gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.8));
            gc.fillOval(sx - sparkR, sy - sparkR, sparkR * 2, sparkR * 2);
        }
    }

    private void renderFireSplash(GraphicsContext gc, double t) {
        double alpha = (1.0 - t) * 0.5;
        double radius = 20 + t * 40;

        // Expanding fire ring
        gc.setStroke(Color.color(1, 0.42, 0.2, alpha));
        gc.setLineWidth(3 - t * 2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Inner heat glow
        gc.setFill(Color.color(1, 0.8, 0, alpha * 0.3));
        double innerR = radius * 0.6;
        gc.fillOval(x - innerR, y - innerR, innerR * 2, innerR * 2);
    }

    private void renderMagicBolt(GraphicsContext gc, double alpha) {
        // Sparkle trail
        for (int i = 0; i < 3; i++) {
            double trailX = x - Math.cos(angle) * (i * 5) + (Math.random() - 0.5) * 5;
            double trailY = y - Math.sin(angle) * (i * 5) + (Math.random() - 0.5) * 5;
            double trailAlpha = alpha * (1.0 - i * 0.3);
            gc.setFill(Color.color(secondaryColor.getRed(), secondaryColor.getGreen(), secondaryColor.getBlue(), trailAlpha * 0.4));
            gc.fillOval(trailX - 3, trailY - 3, 6, 6);
        }

        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillOval(x - 4, y - 4, 8, 8);

        // Star sparkle
        gc.setStroke(Color.color(1, 1, 1, alpha * 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(x - 5, y, x + 5, y);
        gc.strokeLine(x, y - 5, x, y + 5);
    }

    private void renderSiegeBoulder(GraphicsContext gc, double alpha) {
        // Shadow
        gc.setFill(Color.color(0, 0, 0, alpha * 0.3));
        gc.fillOval(x - 5, y + 2, 10, 6);

        // Boulder
        double rot = age * 300; // spinning
        gc.save();
        gc.translate(x, y);
        gc.rotate(rot);
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillRect(-5, -5, 10, 10);
        gc.setStroke(Color.color(0.4, 0.35, 0.25, alpha));
        gc.setLineWidth(1);
        gc.strokeRect(-5, -5, 10, 10);
        gc.restore();
    }

    // Getters
    public boolean isExpired() { return age >= lifetime; }
    public double getX() { return x; }
    public double getY() { return y; }
    public EffectType getType() { return type; }
}
