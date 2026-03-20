package javatower.entities;

/**
 * Abstract base class for all game entities in JavaTower.
 * Uses pixel-based double coordinates for smooth real-time movement.
 */
public abstract class Entity {
    private String name;
    private int maxHealth;
    private int currentHealth;
    private int attack;
    private int defence;
    private double x;
    private double y;
    private double radius = 16;
    private boolean alive;

    /**
     * Called each frame to update the entity.
     */
    public abstract void takeTurn();

    /**
     * Applies damage to the entity, reduced by defence. Minimum 1 damage.
     */
    public int takeDamage(int damage) {
        int reduced = Math.max(1, damage - defence);
        currentHealth -= reduced;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
            onDeath();
        }
        return reduced;
    }

    /**
     * Heals the entity.
     */
    public void heal(int amount) {
        if (!alive) return;
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    /**
     * Sets the entity's pixel position.
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Euclidean distance to another entity (pixels).
     */
    public double distanceTo(Entity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Euclidean distance to a point.
     */
    public double distanceTo(double ox, double oy) {
        double dx = this.x - ox;
        double dy = this.y - oy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns true if this entity overlaps with another (circles).
     */
    public boolean overlaps(Entity other) {
        return distanceTo(other) < (this.radius + other.radius);
    }

    public int getHealthPercent() {
        if (maxHealth == 0) return 0;
        return (int) ((currentHealth * 100.0) / maxHealth);
    }

    protected void onDeath() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefence() { return defence; }
    public void setDefence(int defence) { this.defence = defence; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
