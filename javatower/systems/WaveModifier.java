package javatower.systems;

/**
 * Wave modifiers that apply special effects to entire waves.
 * Randomly selected at the start of each wave.
 */
public enum WaveModifier {
    NONE("Normal Wave", "", 1.0, 1.0, 1.0, 1.0),
    
    // Difficulty modifiers
    SWARM("Swarm", "2x enemies, 50% HP", 2.0, 0.5, 1.0, 0.8),
    ELITE_WAVE("Elite Wave", "All enemies are elite", 1.0, 1.5, 1.0, 1.3),
    RUSH("Rush", "Enemies move 2x faster", 1.0, 1.0, 2.0, 1.0),
    ARMORED("Armored", "+10 defense to all", 1.0, 1.0, 1.0, 1.0),
    
    // Reward modifiers
    GOLD_RUSH("Gold Rush", "3x gold drops", 1.0, 1.0, 1.0, 3.0),
    XP_BOOST("XP Boost", "2x experience", 1.0, 1.0, 1.0, 1.0),
    
    // Challenge modifiers
    NO_TOWERS("No Towers", "Towers disabled", 1.0, 1.0, 1.0, 2.0),
    DARKNESS("Darkness", "Reduced vision", 1.0, 1.0, 1.0, 1.5),
    HORDE("Horde", "3x enemies, +50% rewards", 3.0, 1.0, 1.0, 1.5);
    
    private final String name;
    private final String description;
    private final double enemyCountMultiplier;
    private final double hpMultiplier;
    private final double speedMultiplier;
    private final double rewardMultiplier;
    
    WaveModifier(String name, String desc, double countMult, double hpMult, double speedMult, double rewardMult) {
        this.name = name;
        this.description = desc;
        this.enemyCountMultiplier = countMult;
        this.hpMultiplier = hpMult;
        this.speedMultiplier = speedMult;
        this.rewardMultiplier = rewardMult;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getEnemyCountMultiplier() { return enemyCountMultiplier; }
    public double getHpMultiplier() { return hpMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getRewardMultiplier() { return rewardMultiplier; }
    
    /**
     * Randomly selects a wave modifier.
     * @param wave Wave number (higher = more modifiers)
     * @return Selected modifier
     */
    public static WaveModifier randomModifier(int wave) {
        double roll = Math.random();
        
        // Chance increases with wave number
        double modifierChance = 0.10 + (wave * 0.005); // 10% base + 0.5% per wave
        
        if (roll > modifierChance) return NONE;
        
        // Pick random modifier (excluding NONE)
        WaveModifier[] modifiers = {SWARM, ELITE_WAVE, RUSH, ARMORED, GOLD_RUSH, XP_BOOST, NO_TOWERS, DARKNESS, HORDE};
        return modifiers[(int)(Math.random() * modifiers.length)];
    }
    
    public boolean hasModifier() {
        return this != NONE;
    }
}
