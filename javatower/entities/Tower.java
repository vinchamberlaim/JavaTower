package javatower.entities;

import java.util.List;
import javatower.util.Constants;

/**
 * Abstract base class for all towers in JavaTower.
 * Uses real-time cooldown-based attacking with pixel-range detection.
 */
public abstract class Tower extends Entity {
    public enum TowerType {
        ARROW, MAGIC, SIEGE, SUPPORT
    }

    private TowerType type;
    private int range;          // abstract range units
    private int damage;
    private double attackCooldown; // seconds between attacks
    private double attackTimer;    // time since last attack
    private int upgradeLevel;
    private int upgradeCost;
    private Enemy lastTarget;
    private boolean firedThisFrame;

    public Tower(TowerType type, int range, int damage, int attackSpeed, int upgradeCost) {
        this.type = type;
        this.range = range;
        this.damage = damage;
        this.attackCooldown = attackSpeed; // 1 old turn = 1 second cooldown
        this.attackTimer = 0;
        this.upgradeCost = upgradeCost;
        this.upgradeLevel = 0;
        setAlive(true);
    }

    @Override
    public void takeTurn() {}

    /**
     * Real-time update: attack enemies on cooldown.
     */
    public void update(double dt, List<Enemy> enemies) {
        if (!isAlive()) return;
        firedThisFrame = false;
        attackTimer += dt;
        if (attackTimer >= attackCooldown) {
            Enemy target = selectTarget(enemies);
            if (target != null) {
                lastTarget = target;
                firedThisFrame = true;
            }
            attack(enemies);
            attackTimer = 0;
        }
    }

    /**
     * Returns the tower's effective range in pixels.
     */
    public double getRangePixels() {
        return range * Constants.TOWER_RANGE_UNIT;
    }

    public abstract void attack(List<Enemy> enemies);
    public abstract Enemy selectTarget(List<Enemy> enemies);
    public abstract void upgrade();

    // Getters and setters
    public TowerType getType() { return type; }
    public int getRange() { return range; }
    public int getDamage() { return damage; }
    public double getAttackCooldown() { return attackCooldown; }
    public int getUpgradeLevel() { return upgradeLevel; }
    public int getUpgradeCost() { return upgradeCost; }
    public void setUpgradeLevel(int level) { this.upgradeLevel = level; }
    public void setUpgradeCost(int cost) { this.upgradeCost = cost; }
    public void setDamage(int damage) { this.damage = damage; }
    public void setRange(int range) { this.range = range; }
    public void setAttackCooldown(double cd) { this.attackCooldown = cd; }
    public Enemy getLastTarget() { return lastTarget; }
    public boolean didFireThisFrame() { return firedThisFrame; }
}
