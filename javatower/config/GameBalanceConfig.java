package javatower.config;

/**
 * Centralized game balance configuration.
 * All numeric constants that affect gameplay balance are defined here
 * for easy tweaking and testing.
 * 
 * This makes it easy to:
 * - Adjust difficulty without hunting through code
 * - Create mod support
 * - Balance test different values
 * - Document what each number means
 */
public class GameBalanceConfig {
    
    // Prevent instantiation
    private GameBalanceConfig() {}
    
    // ========== HERO CONFIGURATION ==========
    
    public static class Hero {
        public static final int STARTING_HEALTH = 100;
        public static final int STARTING_ATTACK = 10;
        public static final int STARTING_DEFENCE = 5;
        public static final int STARTING_GOLD = 50;
        public static final int STARTING_MANA = 50;
        public static final int STARTING_CRIT_CHANCE = 5;
        
        public static final double BASE_ATTACK_COOLDOWN = 0.6;
        public static final double BASE_MOVE_SPEED = 120.0;
        public static final double MIN_ATTACK_COOLDOWN = 0.15;
        
        // Level up scaling
        public static final int HEALTH_PER_LEVEL = 10;
        public static final int ATTACK_PER_LEVEL = 2;
        public static final int DEFENCE_PER_LEVEL = 1;
        public static final int MANA_PER_LEVEL = 5;
        public static final double XP_MULTIPLIER_PER_LEVEL = 1.2;
        
        // Inventory expansion
        public static final int INVENTORY_EXPAND_LEVEL_INTERVAL = 3;
    }
    
    // ========== ENEMY CONFIGURATION ==========
    
    public static class Enemy {
        // Wave scaling
        public static final double WAVE_SCALE_FACTOR = 0.05; // +5% per wave
        
        // Enemy type base stats
        public static class Zombie {
            public static final int TIER = 1;
            public static final int HP = 30;
            public static final int ATK = 5;
            public static final int DEF = 2;
            public static final int XP = 10;
            public static final int GOLD = 5;
            public static final double SPEED = 40.0;
        }
        
        public static class Skeleton {
            public static final int TIER = 2;
            public static final int HP = 25;
            public static final int ATK = 8;
            public static final int DEF = 1;
            public static final int XP = 15;
            public static final int GOLD = 8;
            public static final double SPEED = 60.0;
            public static final double ATTACK_RANGE = 150.0;
        }
        
        public static class Ghoul {
            public static final int TIER = 3;
            public static final int HP = 40;
            public static final int ATK = 7;
            public static final int DEF = 3;
            public static final int XP = 20;
            public static final int GOLD = 12;
            public static final double SPEED = 90.0;
        }
        
        public static class Wight {
            public static final int TIER = 4;
            public static final int HP = 60;
            public static final int ATK = 10;
            public static final int DEF = 5;
            public static final int XP = 30;
            public static final int GOLD = 18;
            public static final double SPEED = 60.0;
        }
        
        public static class Wraith {
            public static final int TIER = 5;
            public static final int HP = 35;
            public static final int ATK = 12;
            public static final int DEF = 2;
            public static final int XP = 25;
            public static final int GOLD = 15;
            public static final double SPEED = 90.0;
            public static final double ATTACK_RANGE = 80.0;
        }
        
        public static class Revenant {
            public static final int TIER = 6;
            public static final int HP = 80;
            public static final int ATK = 12;
            public static final int DEF = 8;
            public static final int XP = 40;
            public static final int GOLD = 25;
            public static final double SPEED = 60.0;
        }
        
        public static class DeathKnight {
            public static final int TIER = 7;
            public static final int HP = 120;
            public static final int ATK = 15;
            public static final int DEF = 12;
            public static final int XP = 60;
            public static final int GOLD = 40;
            public static final double SPEED = 40.0;
        }
        
        public static class Lich {
            public static final int TIER = 8;
            public static final int HP = 70;
            public static final int ATK = 20;
            public static final int DEF = 6;
            public static final int XP = 80;
            public static final int GOLD = 50;
            public static final double SPEED = 40.0;
            public static final double ATTACK_RANGE = 180.0;
        }
        
        public static class BoneColossus {
            public static final int TIER = 9;
            public static final int HP = 200;
            public static final int ATK = 18;
            public static final int DEF = 15;
            public static final int XP = 100;
            public static final int GOLD = 70;
            public static final double SPEED = 28.0; // 70% of slow
        }
        
        public static class NecromancerKing {
            public static final int TIER = 10;
            public static final int HP = 500;
            public static final int ATK = 30;
            public static final int DEF = 20;
            public static final int XP = 500;
            public static final int GOLD = 200;
            public static final double SPEED = 60.0;
            public static final double ATTACK_RANGE = 160.0;
        }
    }
    
    // ========== TOWER CONFIGURATION ==========
    
    public static class Tower {
        public static class Arrow {
            public static final int RANGE = 4; // in tiles
            public static final int DAMAGE = 15;
            public static final double COOLDOWN = 0.8;
            public static final int COST = 25;
            public static final int UPGRADE_COST_BASE = 20;
        }
        
        public static class Magic {
            public static final int RANGE = 3;
            public static final int DAMAGE = 12;
            public static final double COOLDOWN = 1.2;
            public static final int COST = 40;
            public static final int UPGRADE_COST_BASE = 30;
        }
        
        public static class Siege {
            public static final int RANGE = 6;
            public static final int DAMAGE = 40;
            public static final double COOLDOWN = 2.0;
            public static final int COST = 60;
            public static final int UPGRADE_COST_BASE = 50;
        }
        
        public static class Support {
            public static final int RANGE = 3;
            public static final int HEAL_AMOUNT = 5;
            public static final double COOLDOWN = 2.0;
            public static final int COST = 35;
            public static final int UPGRADE_COST_BASE = 30;
        }
        
        public static final int MAX_UPGRADE_LEVEL = 3;
        public static final double UPGRADE_STAT_MULTIPLIER = 1.3;
        public static final double UPGRADE_COST_MULTIPLIER = 1.5;
    }
    
    // ========== WAVE CONFIGURATION ==========
    
    public static class Wave {
        public static final int TOTAL_WAVES = 30;
        public static final double DELAY_BETWEEN_WAVES = 3.0;
        public static final int BOSS_WAVE_INTERVAL = 5;
        
        // Enemy count formula: BASE + (wave * MULTIPLIER)
        public static final int BASE_ENEMY_COUNT = 5;
        public static final int ENEMY_COUNT_MULTIPLIER = 2;
    }
    
    // ========== SKILL CONFIGURATION ==========
    
    public static class Skills {
        public static class SkillQ {
            public static final int MANA_COST = 15;
            public static final double DAMAGE_MULTIPLIER = 1.8;
            public static final double RANGE = 40.0;
        }
        
        public static class SkillW {
            public static final int MANA_COST = 25;
            public static final double DAMAGE_MULTIPLIER = 2.0;
            public static final double RANGE_MULTIPLIER = 2.5;
        }
    }
    
    // ========== COMBAT CONFIGURATION ==========
    
    public static class Combat {
        public static final double MELEE_RANGE = 40.0;
        public static final double CRIT_DAMAGE_MULTIPLIER = 1.5;
        public static final double FIRE_SPLASH_FRACTION = 0.3;
        public static final double FIRE_SPLASH_RADIUS_BONUS = 40.0;
        
        // Set bonuses
        public static final double FIRE_2PC_DAMAGE_BONUS = 1.25;
        public static final double FIRE_4PC_SPLASH_FRACTION = 0.3;
        public static final double HOLY_2PC_HEAL_BONUS = 1.2;
        public static final double HOLY_4PC_UNDEAD_DAMAGE_BONUS = 1.5;
        public static final double DEATH_4PC_LIFE_STEAL = 0.15;
        public static final int DEATH_4PC_MANA_REGEN = 2;
        public static final double KNIGHT_2PC_DEFENCE_BONUS = 1.25;
        public static final double KNIGHT_4PC_SPEED_BONUS = 0.1;
        public static final int HOLY_4PC_PASSIVE_REGEN = 1;
    }
    
    // ========== ITEM CONFIGURATION ==========
    
    public static class Item {
        // Rarity multipliers
        public static final double COMMON_MULTIPLIER = 1.0;
        public static final double UNCOMMON_MULTIPLIER = 1.2;
        public static final double RARE_MULTIPLIER = 1.5;
        public static final double EPIC_MULTIPLIER = 2.0;
        public static final double LEGENDARY_MULTIPLIER = 3.0;
        
        // Forge costs
        public static final int FORGE_COST_COMMON = 10;
        public static final int FORGE_COST_UNCOMMON = 25;
        public static final int FORGE_COST_RARE = 60;
        public static final int FORGE_COST_EPIC = 150;
        
        // Shop pricing
        public static final double SELL_PRICE_DIVISOR = 3.0;
    }
}
