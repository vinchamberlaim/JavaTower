package javatower.entities;

import javafx.scene.paint.Color;

/**
 * Elite enemy modifiers that give special properties to enemies.
 * Applied randomly to enemies for variety.
 */
public enum EliteModifier {
    NONE("", Color.WHITE, 1.0, 1.0, 1.0, 1.0),
    
    // Tier 1: Common modifiers (30% chance)
    FAST("Swift", Color.web("#63b3ed"), 1.0, 1.5, 0.8, 1.0),      // +50% speed, -20% HP
    TANKY("Iron", Color.web("#a0aec0"), 1.5, 0.7, 1.0, 1.0),     // +50% HP, -30% speed
    
    // Tier 2: Uncommon modifiers (15% chance)
    VAMPIRIC("Vampiric", Color.web("#9b2c2c"), 1.2, 1.0, 1.0, 1.2), // +20% HP, heals 20% of damage dealt
    EXPLOSIVE("Volatile", Color.web("#dd6b20"), 1.0, 1.0, 1.0, 1.0),  // Explodes on death
    REGENERATING("Regen", Color.web("#48bb78"), 1.0, 1.0, 1.0, 1.0),  // Regenerates HP over time
    
    // Tier 3: Rare modifiers (5% chance)
    SHIELDED("Shielded", Color.web("#d69e2e"), 1.0, 1.0, 1.0, 1.0),   // Shield absorbs first hit
    SPLITTER("Splitter", Color.web("#805ad5"), 1.3, 1.0, 1.0, 1.0),   // Splits into 2 smaller on death
    
    // Tier 4: Epic modifier (1% chance)
    LEGENDARY("Legendary", Color.web("#ecc94b"), 2.0, 1.3, 1.5, 3.0);  // +100% HP, +30% speed, +50% damage, 3x rewards
    
    private final String prefix;
    private final Color glowColor;
    private final double hpMultiplier;
    private final double speedMultiplier;
    private final double damageMultiplier;
    private final double rewardMultiplier;
    
    EliteModifier(String prefix, Color glowColor, double hpMult, double speedMult, double dmgMult, double rewardMult) {
        this.prefix = prefix;
        this.glowColor = glowColor;
        this.hpMultiplier = hpMult;
        this.speedMultiplier = speedMult;
        this.damageMultiplier = dmgMult;
        this.rewardMultiplier = rewardMult;
    }
    
    public String getPrefix() { return prefix; }
    public Color getGlowColor() { return glowColor; }
    public double getHpMultiplier() { return hpMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getRewardMultiplier() { return rewardMultiplier; }
    
    /**
     * Gets the display name for an elite enemy.
     */
    public String getDisplayName(String baseName) {
        if (this == NONE) return baseName;
        return prefix + " " + baseName;
    }
    
    /**
     * Randomly selects a modifier based on rarity tiers.
     * @param wave Wave number (higher = more elites)
     * @return Selected modifier
     */
    public static EliteModifier randomModifier(int wave) {
        double roll = Math.random();
        
        // Increase elite chance based on wave
        double eliteChance = 0.05 + (wave * 0.005); // 5% base + 0.5% per wave
        
        if (roll > eliteChance) return NONE;
        
        // Determine which tier of elite
        double tierRoll = Math.random();
        
        if (tierRoll < 0.01) return LEGENDARY;        // 1%
        if (tierRoll < 0.06) {                        // 5% total for rare
            return Math.random() < 0.5 ? SHIELDED : SPLITTER;
        }
        if (tierRoll < 0.21) {                        // 15% total for uncommon
            double u = Math.random();
            if (u < 0.33) return VAMPIRIC;
            if (u < 0.66) return EXPLOSIVE;
            return REGENERATING;
        }
        // Remaining 30% for common
        return Math.random() < 0.5 ? FAST : TANKY;
    }
    
    /**
     * Returns a random non-NONE modifier (for Elite Wave modifier).
     */
    public static EliteModifier randomForced() {
        double tierRoll = Math.random();
        
        if (tierRoll < 0.01) return LEGENDARY;        // 1%
        if (tierRoll < 0.06) {                        // 5% total for rare
            return Math.random() < 0.5 ? SHIELDED : SPLITTER;
        }
        if (tierRoll < 0.21) {                        // 15% total for uncommon
            double u = Math.random();
            if (u < 0.33) return VAMPIRIC;
            if (u < 0.66) return EXPLOSIVE;
            return REGENERATING;
        }
        // Remaining 30% for common
        return Math.random() < 0.5 ? FAST : TANKY;
    }
}
