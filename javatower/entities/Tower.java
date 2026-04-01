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

    public enum TargetMode {
        NEAREST, STRONGEST, WEAKEST, FIRST
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

    // Kill tracking (#37)
    private int killCount = 0;
    private int totalDamageDealt = 0;

    // Targeting mode (#31)
    private TargetMode targetMode = TargetMode.NEAREST;

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
        if (attackTimer >= getEffectiveAttackCooldown()) {
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

    // Kill tracking (#37)
    public int getKillCount() { return killCount; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public void recordDamage(int damage) { totalDamageDealt += damage; }
    public void recordKill() { killCount++; }

    // Targeting mode (#31)
    public TargetMode getTargetMode() { return targetMode; }
    public void cycleTargetMode() {
        TargetMode[] modes = TargetMode.values();
        targetMode = modes[(targetMode.ordinal() + 1) % modes.length];
    }
    
    // ========== SYNERGY SYSTEM ==========
    
    private javatower.systems.TowerSynergyManager.SynergyType activeSynergy = 
        javatower.systems.TowerSynergyManager.SynergyType.NONE;
    private double synergyDamageMultiplier = 1.0;
    private double synergySpeedMultiplier = 1.0;
    private double synergyRangeMultiplier = 1.0;
    private boolean pierceEnabled = false;
    private boolean gravityWellEnabled = false;
    private boolean volleyEnabled = false;
    
    public void clearSynergyBonus() {
        activeSynergy = javatower.systems.TowerSynergyManager.SynergyType.NONE;
        synergyDamageMultiplier = 1.0;
        synergySpeedMultiplier = 1.0;
        synergyRangeMultiplier = 1.0;
        pierceEnabled = false;
        gravityWellEnabled = false;
        volleyEnabled = false;
    }
    
    public void setActiveSynergy(javatower.systems.TowerSynergyManager.SynergyType synergy) {
        this.activeSynergy = synergy;
    }
    
    public javatower.systems.TowerSynergyManager.SynergyType getActiveSynergy() {
        return activeSynergy;
    }
    
    public void setSynergyDamageMultiplier(double mult) {
        this.synergyDamageMultiplier = mult;
    }
    
    public void setSynergySpeedMultiplier(double mult) {
        this.synergySpeedMultiplier = mult;
    }
    
    public void setSynergyRangeMultiplier(double mult) {
        this.synergyRangeMultiplier = mult;
    }
    
    public void setPierceEnabled(boolean enabled) {
        this.pierceEnabled = enabled;
    }
    
    public void setGravityWellEnabled(boolean enabled) {
        this.gravityWellEnabled = enabled;
    }
    
    public void setVolleyEnabled(boolean enabled) {
        this.volleyEnabled = enabled;
    }
    
    // Effective stats with synergy
    public int getEffectiveDamage() {
        return (int)(damage * synergyDamageMultiplier);
    }
    
    public double getEffectiveAttackCooldown() {
        return attackCooldown / synergySpeedMultiplier;
    }
    
    public double getEffectiveRangePixels() {
        return getRangePixels() * synergyRangeMultiplier;
    }
    
    public boolean isPierceEnabled() { return pierceEnabled; }
    public boolean isGravityWellEnabled() { return gravityWellEnabled; }
    public boolean isVolleyEnabled() { return volleyEnabled; }
    
    public boolean hasSynergy() {
        return activeSynergy != javatower.systems.TowerSynergyManager.SynergyType.NONE;
    }

    /**
     * Selects a target from enemies in range using the current targeting mode.
     * Subclasses can call this instead of doing their own selection.
     */
    protected Enemy selectByMode(List<Enemy> enemies) {
        double rangePx = getEffectiveRangePixels();
        Enemy pick = null;
        double bestVal = 0;
        boolean first = true;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dist = distanceTo(e);
            if (dist > rangePx) continue;
            if (first) { pick = e; bestVal = (targetMode == TargetMode.NEAREST || targetMode == TargetMode.WEAKEST) ? dist : e.getCurrentHealth(); first = false; }
            switch (targetMode) {
                case NEAREST:
                    if (dist < bestVal) { bestVal = dist; pick = e; }
                    break;
                case STRONGEST:
                    if (e.getCurrentHealth() > bestVal) { bestVal = e.getCurrentHealth(); pick = e; }
                    break;
                case WEAKEST:
                    if (e.getCurrentHealth() < bestVal) { bestVal = e.getCurrentHealth(); pick = e; }
                    break;
                case FIRST:
                    pick = e; // first alive in list
                    return pick;
            }
        }
        return pick;
    }
}
