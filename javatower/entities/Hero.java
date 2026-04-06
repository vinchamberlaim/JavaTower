package javatower.entities;

import javatower.systems.Inventory;
import javatower.systems.SkillTree;
import javatower.systems.SkillProgression;
import javatower.systems.SetBonusManager;
import javatower.entities.Item;
import javatower.entities.Item.WeaponClass;
import javatower.entities.Item.EquipmentSet;
import javatower.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The player-controlled hero character.
 * <p>
 * The hero supports:
 * <ul>
 *   <li><b>Equipment</b> — 8 fixed slots (weapon, offhand, helmet, chest, legs,
 *       boots, gloves, amulet) plus up to 10 ring slots.</li>
 *   <li><b>Three skill trees</b> — Combat, Magic, and Utility, each with 10
 *       branching nodes unlockable with skill points earned on level-up.</li>
 *   <li><b>Weapon-class progression</b> — Melee, Ranged, Necromancy, Holy, and
 *       Defence skills that level through use ({@link SkillProgression}).</li>
 *   <li><b>Set bonuses</b> — Holy, Death, Fire, and Knight equipment sets that
 *       activate at 2-piece and 4-piece thresholds via {@link SetBonusManager}.</li>
 *   <li><b>Dodge/Roll</b> — a short invincibility dash bound to SHIFT.</li>
 *   <li><b>Ultimate (RAGE)</b> — charged by dealing/taking damage; grants
 *       +50 % ATK, +50 % DEF, +25 % crit for 10 s.</li>
 * </ul>
 * Movement is real-time: click-to-move or WASD/arrow-key continuous.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see Enemy
 * @see SkillTree
 * @see SkillProgression
 * @see SetBonusManager
 */
public class Hero extends Entity {
    // ==================== Equipment Slots ====================
    /** Currently equipped weapon (may be two-handed, blocking offhand). */
    private Item weapon, offhand, helmet, chest, legs, boots, gloves, amulet;
    /** Up to 10 ring slots, each providing stat bonuses. */
    private Item[] rings = new Item[10];

    // ==================== Progression ====================
    /** Current hero level (starts at 1, uncapped). */
    private int level = 1;
    /** Current XP toward the next level-up. */
    private int experience = 0;
    /** XP threshold for the next level (grows by 20 % each level). */
    private int experienceToNextLevel = 100;
    /** Current gold balance. */
    private int gold = 50;
    /** Current mana pool. */
    private int mana = 50;
    /** Base maximum mana (before equipment bonuses). */
    private int maxMana = 50;
    /** Base crit chance percentage (before equipment/skill bonuses). */
    private int critChance = 5;
    /** Unspent skill points (earned on level-up). */
    private int skillPoints = 0;

    // ==================== Movement ====================
    /** Base movement speed in pixels per second. */
    private double moveSpeed = Constants.HERO_SPEED;
    /** Base seconds between auto-attacks. */
    private double attackCooldown = 0.6;
    /** Accumulator tracking time since last auto-attack. */
    private double attackTimer = 0;
    /** 1-second tick timer for passive HP/mana regeneration. */
    private double regenTimer = 0;
    /** Click-to-move target X coordinate (world space). */
    private double targetX, targetY;
    /** Whether the hero is currently moving toward the click target. */
    private boolean moving = false;

    private Inventory inventory;
    private SkillTree warriorTree, paladinTree, necromancerTree, pyromancerTree, archerTree;
    private SkillProgression skillProgression;
    
    // ==================== Skill Tree Special Abilities ====================
    /** Archer: Multishot fires extra arrows. */
    private int archerMultishotCount = 0;
    /** Archer: Bonus attack range from skills. */
    private double archerSkillRangeBonus = 0;
    /** Necromancer: Extra summon damage %. */
    private double necroSummonBonus = 0;
    /** Paladin: Extra heal power %. */
    private double paladinHealBonus = 0;
    /** Pyromancer: Extra fire damage %. */
    private double pyroFireBonus = 0;
    /** Pyromancer: Fire Elemental is summoned (unlocked by skill tree). */
    private boolean pyroElementalActive = false;
    /** Warrior: Extra damage reduction. */
    private int warriorArmorBonus = 0;

    // ==================== Arrow-key Continuous Movement ====================
    /** Flags set by key-press / key-release events to enable WASD movement. */
    private boolean moveUp, moveDown, moveLeft, moveRight;

    // ==================== Ultimate Ability ====================
    /** Whether the hero is currently in RAGE mode (+50 % ATK/DEF). */
    private boolean ultimateActive = false;
    // ==================== Auto-Cast Equipment Set Spells ====================
    /** Cooldown timers for each set's auto-spell. */
    private double holySpellTimer = 0;
    private double deathSpellTimer = 0;
    private double fireSpellTimer = 0;
    private double knightSpellTimer = 0;

    /** Cooldown durations (seconds) for set auto-spells. */
    public static final double HOLY_SPELL_CD   = 6.0;
    public static final double DEATH_SPELL_CD  = 5.0;
    public static final double FIRE_SPELL_CD   = 4.0;
    public static final double KNIGHT_SPELL_CD = 5.0;

    // ==================== Weapon Class Spell Timers ====================
    /** Cooldown timers for weapon class passive spells. */
    private double necroBoneShieldTimer = 0;
    private double necroSummonTimer = 0;
    private double pyroElementalTimer = 0;
    private double archerVolleyTimer = 0;
    private double meleeWhirlwindTimer = 0;
    private double holyConsecrationTimer = 0;
    private double defenceShieldBlockTimer = 0;

    /** Cooldown durations (seconds) for weapon class spells. */
    /** Cooldown in seconds for the Necromancy Bone Shield passive (4+ NECROMANCY items). */
    public static final double NECRO_BONESHIELD_CD = 8.0;
    /** Cooldown in seconds for the Necromancy Summon Army passive (5+ NECROMANCY items). */
    public static final double NECRO_SUMMON_CD = 12.0;
    /** Cooldown in seconds for the Pyromancer Fire Elemental — unlocked via f4c skill node. */
    public static final double PYRO_ELEMENTAL_CD = 8.0;
    /** Cooldown in seconds for the Archer Volley passive (any multishot bonus active). */
    public static final double ARCHER_VOLLEY_CD = 6.0;
    public static final double MELEE_WHIRLWIND_CD = 5.0;
    public static final double HOLY_CONSECRATION_CD = 7.0;
    public static final double DEFENCE_SHIELDBLOCK_CD = 10.0;

    // Class spell active flags
    private boolean boneShieldActive = false;
    private double boneShieldDuration = 0;
    private boolean shieldBlockActive = false;
    private double shieldBlockDuration = 0;
    private boolean holyResurrectionUsed = false; // once per floor

    // Getters/setters for spell timers
    public double getHolySpellTimer() { return holySpellTimer; }
    public double getDeathSpellTimer() { return deathSpellTimer; }
    public double getFireSpellTimer() { return fireSpellTimer; }
    public double getKnightSpellTimer() { return knightSpellTimer; }
    public void setHolySpellTimer(double v) { holySpellTimer = v; }
    public void setDeathSpellTimer(double v) { deathSpellTimer = v; }
    public void setFireSpellTimer(double v) { fireSpellTimer = v; }
    public void setKnightSpellTimer(double v) { knightSpellTimer = v; }

    public void tickSpellTimers(double dt) {
        if (holySpellTimer > 0) holySpellTimer -= dt;
        if (deathSpellTimer > 0) deathSpellTimer -= dt;
        if (fireSpellTimer > 0) fireSpellTimer -= dt;
        if (knightSpellTimer > 0) knightSpellTimer -= dt;
        
        // Weapon class spell timers
        if (necroBoneShieldTimer > 0) necroBoneShieldTimer -= dt;
        if (necroSummonTimer > 0) necroSummonTimer -= dt;
        if (pyroElementalTimer > 0) pyroElementalTimer -= dt;
        if (archerVolleyTimer > 0) archerVolleyTimer -= dt;
        if (meleeWhirlwindTimer > 0) meleeWhirlwindTimer -= dt;
        if (holyConsecrationTimer > 0) holyConsecrationTimer -= dt;
        if (defenceShieldBlockTimer > 0) defenceShieldBlockTimer -= dt;
        
        // Duration tracking
        if (boneShieldDuration > 0) {
            boneShieldDuration -= dt;
            if (boneShieldDuration <= 0) boneShieldActive = false;
        }
        if (shieldBlockDuration > 0) {
            shieldBlockDuration -= dt;
            if (shieldBlockDuration <= 0) shieldBlockActive = false;
        }
    }
    // ==================== Dodge / Roll ====================
    /** True while the hero is mid-roll (invincible). */
    private boolean isRolling = false;
    /** Remaining time in the current roll (seconds). */
    private double rollTimer = 0;
    /** Total roll duration in seconds. */
    private double rollDuration = 0.35;
    /** Cooldown between rolls in seconds. */
    private double rollCooldown = 1.5;
    /** Remaining cooldown before the next roll can start. */
    private double rollCooldownTimer = 0;
    /** Speed multiplier during a roll (3× normal). */
    private double rollSpeed = 3.0;
    /** Normalised roll direction vector components. */
    private double rollDirectionX = 0;
    private double rollDirectionY = 0;

    // ==================== Lifetime Kill Stats ====================
    /** Total enemies killed this run. */
    private int totalKills = 0;
    /** Cumulative damage dealt this run. */
    private int totalDamageDealt = 0;
    /** Cumulative gold earned this run. */
    private int totalGoldEarned = 0;
    /** Cumulative XP earned this run. */
    private int totalXPEarned = 0;

    // ==================== Per-frame Attack Result Cache ====================
    /** Damage dealt by the most recent auto-attack (for floating numbers). */
    private int lastDamageDealt;
    /** Whether the last attack was a critical hit. */
    private boolean lastAttackCrit;
    /** Weapon class used in the last attack (for visual effect selection). */
    private WeaponClass lastAttackWeaponClass = WeaponClass.MELEE;
    /** The enemy targeted by the last auto-attack. */
    private Enemy lastAttackTarget;
    /** Flag set for exactly one frame after an auto-attack lands. */
    private boolean attackedThisFrame;
    /** Flag set for exactly one frame after the hero levels up. */
    private boolean leveledUpThisFrame;

    /**
     * Constructs a new hero with default base stats and initialises
     * the three skill trees (Combat, Magic, Utility).
     *
     * @param name the hero’s display name
     */
    public Hero(String name) {
        setName(name);
        setMaxHealth(100);
        setCurrentHealth(100);
        setAttack(10);
        setDefence(5);
        setAlive(true);
        setRadius(Constants.HERO_RADIUS);
        inventory = new Inventory(3, 3);
        skillProgression = new SkillProgression();
        initSkillTrees();
    }

    /**
     * Initializes class-based skill trees that synergize with equipment sets.
     * Trees: Warrior (Knight), Paladin (Holy), Necromancer (Death), Pyromancer (Fire), Archer (Ranged)
     * Costs: Tier 1 = 1pt, Tier 2 = 1pt, Tier 3 = 2pt, Tier 4 = 3pt, Tier 5 = 4pt
     */
    private void initSkillTrees() {
        Hero self = this;
        
        // ========== WARRIOR TREE (synergizes with Knight set) ==========
        warriorTree = new SkillTree("Warrior");
        
        // Tier 1 - Foundation
        warriorTree.addNode(new javatower.systems.SkillNode("w1", "Toughness", "🛡️ +30 HP, +3 DEF", "warrior", 1,
                null, Map.of("maxHealth", 30, "defence", 3), null));
        
        // Tier 2 - Branching
        warriorTree.addNode(new javatower.systems.SkillNode("w2a", "Iron Skin", "🏰 +8 DEF, +15 HP", "warrior", 1,
                java.util.List.of("w1"), Map.of("defence", 8, "maxHealth", 15), null));
        warriorTree.addNode(new javatower.systems.SkillNode("w2b", "Battle Stance", "⚔️ +6 ATK, +20 HP", "warrior", 1,
                java.util.List.of("w1"), Map.of("attack", 6, "maxHealth", 20), null));
        
        // Tier 3 - Specialization
        warriorTree.addNode(new javatower.systems.SkillNode("w3a", "Bulwark", "🏯 +12 DEF, +25 HP", "warrior", 2,
                java.util.List.of("w2a"), Map.of("defence", 12, "maxHealth", 25), null));
        warriorTree.addNode(new javatower.systems.SkillNode("w3b", "Shield Wall", "🛡️🛡️ +10 DEF, +5% Block", "warrior", 2,
                java.util.List.of("w2a"), Map.of("defence", 10), () -> self.warriorArmorBonus += 5));
        warriorTree.addNode(new javatower.systems.SkillNode("w3c", "Berserker", "🔥 +10 ATK, +5% Crit", "warrior", 2,
                java.util.List.of("w2b"), Map.of("attack", 10, "critChance", 5), null));
        warriorTree.addNode(new javatower.systems.SkillNode("w3d", "Cleave", "💪 +8 ATK, attacks hit nearby", "warrior", 2,
                java.util.List.of("w2b"), Map.of("attack", 8), null));
        
        // Tier 4 - Advanced
        warriorTree.addNode(new javatower.systems.SkillNode("w4a", "Fortress", "🏰🏰 +15 DEF, +40 HP", "warrior", 3,
                java.util.List.of("w3a", "w3b"), Map.of("defence", 15, "maxHealth", 40), () -> self.warriorArmorBonus += 10));
        warriorTree.addNode(new javatower.systems.SkillNode("w4b", "Warlord", "👑 +12 ATK, +8 DEF, +30 HP", "warrior", 3,
                java.util.List.of("w3c", "w3d"), Map.of("attack", 12, "defence", 8, "maxHealth", 30), null));
        
        // Tier 5 - Ultimate
        warriorTree.addNode(new javatower.systems.SkillNode("w5", "Unstoppable", "⭐ +20 DEF, +60 HP, 15% DR", "warrior", 4,
                java.util.List.of("w4a", "w4b"), Map.of("defence", 20, "maxHealth", 60), () -> self.warriorArmorBonus += 15));

        // ========== PALADIN TREE (synergizes with Holy set) ==========
        paladinTree = new SkillTree("Paladin");
        
        // Tier 1
        paladinTree.addNode(new javatower.systems.SkillNode("p1", "Inner Light", "✨ +20 HP, +15 Mana", "paladin", 1,
                null, Map.of("maxHealth", 20, "maxMana", 15), null));
        
        // Tier 2
        paladinTree.addNode(new javatower.systems.SkillNode("p2a", "Blessing", "💚 +10% Heal Power", "paladin", 1,
                java.util.List.of("p1"), Map.of("maxHealth", 10), () -> self.paladinHealBonus += 0.10));
        paladinTree.addNode(new javatower.systems.SkillNode("p2b", "Holy Strength", "⚔️ +5 ATK, +25 HP", "paladin", 1,
                java.util.List.of("p1"), Map.of("attack", 5, "maxHealth", 25), null));
        
        // Tier 3
        paladinTree.addNode(new javatower.systems.SkillNode("p3a", "Aura of Protection", "🛡️ +6 DEF, +20% Heal", "paladin", 2,
                java.util.List.of("p2a"), Map.of("defence", 6), () -> self.paladinHealBonus += 0.20));
        paladinTree.addNode(new javatower.systems.SkillNode("p3b", "Divine Favor", "✨✨ +30 HP, +20 Mana", "paladin", 2,
                java.util.List.of("p2a"), Map.of("maxHealth", 30, "maxMana", 20), null));
        paladinTree.addNode(new javatower.systems.SkillNode("p3c", "Smite", "⚡ +8 ATK, Holy damage boost", "paladin", 2,
                java.util.List.of("p2b"), Map.of("attack", 8), null));
        paladinTree.addNode(new javatower.systems.SkillNode("p3d", "Zealot", "🔥 +6 ATK, +5% Crit", "paladin", 2,
                java.util.List.of("p2b"), Map.of("attack", 6, "critChance", 5), null));
        
        // Tier 4
        paladinTree.addNode(new javatower.systems.SkillNode("p4a", "Holy Shield", "🛡️✨ +10 DEF, +35% Heal", "paladin", 3,
                java.util.List.of("p3a", "p3b"), Map.of("defence", 10, "maxHealth", 20), () -> self.paladinHealBonus += 0.35));
        paladinTree.addNode(new javatower.systems.SkillNode("p4b", "Crusader", "⚔️✨ +10 ATK, +35 HP", "paladin", 3,
                java.util.List.of("p3c", "p3d"), Map.of("attack", 10, "maxHealth", 35), null));
        
        // Tier 5
        paladinTree.addNode(new javatower.systems.SkillNode("p5", "Divine Champion", "👼 +50 HP, +50% Heal, Holy Aura", "paladin", 4,
                java.util.List.of("p4a", "p4b"), Map.of("maxHealth", 50, "attack", 8), () -> self.paladinHealBonus += 0.50));

        // ========== NECROMANCER TREE (synergizes with Death set) ==========
        necromancerTree = new SkillTree("Necromancer");
        
        // Tier 1
        necromancerTree.addNode(new javatower.systems.SkillNode("n1", "Dark Arts", "💀 +25 Mana, +10 HP", "necromancer", 1,
                null, Map.of("maxMana", 25, "maxHealth", 10), null));
        
        // Tier 2
        necromancerTree.addNode(new javatower.systems.SkillNode("n2a", "Soul Harvest", "👻 +15% Life Steal", "necromancer", 1,
                java.util.List.of("n1"), Map.of("maxMana", 10), () -> self.necroSummonBonus += 0.15));
        necromancerTree.addNode(new javatower.systems.SkillNode("n2b", "Bone Armor", "🦴 +6 DEF, +15 HP", "necromancer", 1,
                java.util.List.of("n1"), Map.of("defence", 6, "maxHealth", 15), null));
        
        // Tier 3
        necromancerTree.addNode(new javatower.systems.SkillNode("n3a", "Raise Dead", "💀💀 +25% Summon Damage", "necromancer", 2,
                java.util.List.of("n2a"), Map.of("maxMana", 15), () -> self.necroSummonBonus += 0.25));
        necromancerTree.addNode(new javatower.systems.SkillNode("n3b", "Life Tap", "🩸 +20 Mana, +5 ATK", "necromancer", 2,
                java.util.List.of("n2a"), Map.of("maxMana", 20, "attack", 5), null));
        necromancerTree.addNode(new javatower.systems.SkillNode("n3c", "Corpse Explosion", "💥 +6 ATK, AoE on kill", "necromancer", 2,
                java.util.List.of("n2b"), Map.of("attack", 6), null));
        necromancerTree.addNode(new javatower.systems.SkillNode("n3d", "Unholy Armor", "🦴🦴 +10 DEF, +20 HP", "necromancer", 2,
                java.util.List.of("n2b"), Map.of("defence", 10, "maxHealth", 20), null));
        
        // Tier 4
        necromancerTree.addNode(new javatower.systems.SkillNode("n4a", "Army of Dead", "☠️ +50% Summon DMG", "necromancer", 3,
                java.util.List.of("n3a", "n3b"), Map.of("maxMana", 25), () -> self.necroSummonBonus += 0.50));
        necromancerTree.addNode(new javatower.systems.SkillNode("n4b", "Death Coil", "💀⚡ +10 ATK, lifesteal", "necromancer", 3,
                java.util.List.of("n3c", "n3d"), Map.of("attack", 10, "maxHealth", 25), null));
        
        // Tier 5
        necromancerTree.addNode(new javatower.systems.SkillNode("n5", "Lich King", "👑💀 +100% Summon, +30 Mana", "necromancer", 4,
                java.util.List.of("n4a", "n4b"), Map.of("maxMana", 30, "attack", 10), () -> self.necroSummonBonus += 1.0));

        // ========== PYROMANCER TREE (synergizes with Fire set) ==========
        pyromancerTree = new SkillTree("Pyromancer");
        
        // Tier 1
        pyromancerTree.addNode(new javatower.systems.SkillNode("f1", "Ignite", "🔥 +5 ATK, +10% Fire", "pyromancer", 1,
                null, Map.of("attack", 5), () -> self.pyroFireBonus += 0.10));
        
        // Tier 2
        pyromancerTree.addNode(new javatower.systems.SkillNode("f2a", "Heat Wave", "🌊🔥 +15% Fire, +10 Mana", "pyromancer", 1,
                java.util.List.of("f1"), Map.of("maxMana", 10), () -> self.pyroFireBonus += 0.15));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f2b", "Burning Soul", "🔥💪 +6 ATK, +15 Mana", "pyromancer", 1,
                java.util.List.of("f1"), Map.of("attack", 6, "maxMana", 15), null));
        
        // Tier 3
        pyromancerTree.addNode(new javatower.systems.SkillNode("f3a", "Fireball", "🔥💥 +25% Fire, AoE", "pyromancer", 2,
                java.util.List.of("f2a"), Map.of("attack", 4), () -> self.pyroFireBonus += 0.25));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f3b", "Fire Shield", "🛡️🔥 +5 DEF, burn attackers", "pyromancer", 2,
                java.util.List.of("f2a"), Map.of("defence", 5, "maxHealth", 15), null));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f3c", "Combustion", "💥 +8 ATK, +5% Crit", "pyromancer", 2,
                java.util.List.of("f2b"), Map.of("attack", 8, "critChance", 5), null));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f3d", "Mana Burn", "⚡🔥 +25 Mana, +4 ATK", "pyromancer", 2,
                java.util.List.of("f2b"), Map.of("maxMana", 25, "attack", 4), null));
        
        // Tier 4
        pyromancerTree.addNode(new javatower.systems.SkillNode("f4a", "Meteor", "☄️ +50% Fire, huge AoE", "pyromancer", 3,
                java.util.List.of("f3a", "f3b"), Map.of("attack", 8), () -> self.pyroFireBonus += 0.50));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f4b", "Flame Cloak", "🔥🔥 +10 ATK, +10% Crit", "pyromancer", 3,
                java.util.List.of("f3c", "f3d"), Map.of("attack", 10, "critChance", 10), null));
        pyromancerTree.addNode(new javatower.systems.SkillNode("f4c", "Fire Elemental", "🔥🧞 Summon a Fire Elemental", "pyromancer", 3,
                java.util.List.of("f3d"), Map.of("attack", 6, "maxMana", 20), () -> self.pyroElementalActive = true));
        
        // Tier 5
        pyromancerTree.addNode(new javatower.systems.SkillNode("f5", "Inferno", "🌋 +100% Fire, +15 ATK", "pyromancer", 4,
                java.util.List.of("f4a", "f4b"), Map.of("attack", 15), () -> self.pyroFireBonus += 1.0));

        // ========== ARCHER TREE (synergizes with Ranged weapons) ==========
        archerTree = new SkillTree("Archer");
        
        // Tier 1
        archerTree.addNode(new javatower.systems.SkillNode("a1", "Keen Eye", "👁️ +40 Range, +3 ATK", "archer", 1,
                null, Map.of("attack", 3), () -> self.archerSkillRangeBonus += 40));
        
        // Tier 2
        archerTree.addNode(new javatower.systems.SkillNode("a2a", "Steady Aim", "🎯 +8% Crit, +30 Range", "archer", 1,
                java.util.List.of("a1"), Map.of("critChance", 8), () -> self.archerSkillRangeBonus += 30));
        archerTree.addNode(new javatower.systems.SkillNode("a2b", "Quick Draw", "⚡ +5 ATK, faster attacks", "archer", 1,
                java.util.List.of("a1"), Map.of("attack", 5), null));
        
        // Tier 3
        archerTree.addNode(new javatower.systems.SkillNode("a3a", "Eagle Eye", "🦅 +60 Range, +5% Crit", "archer", 2,
                java.util.List.of("a2a"), Map.of("critChance", 5), () -> self.archerSkillRangeBonus += 60));
        archerTree.addNode(new javatower.systems.SkillNode("a3b", "Multishot", "🏹🏹 Fire 2 extra arrows", "archer", 2,
                java.util.List.of("a2a"), Map.of("attack", 4), () -> self.archerMultishotCount += 2));
        archerTree.addNode(new javatower.systems.SkillNode("a3c", "Piercing Arrow", "➡️🏹 +10 ATK, pierce armor", "archer", 2,
                java.util.List.of("a2b"), Map.of("attack", 10), null));
        archerTree.addNode(new javatower.systems.SkillNode("a3d", "Rapid Fire", "⚡⚡ +6 ATK, +10% speed", "archer", 2,
                java.util.List.of("a2b"), Map.of("attack", 6, "speed", 10), null));
        
        // Tier 4
        archerTree.addNode(new javatower.systems.SkillNode("a4a", "Sniper", "🎯🎯 +100 Range, +10% Crit", "archer", 3,
                java.util.List.of("a3a", "a3b"), Map.of("critChance", 10), () -> self.archerSkillRangeBonus += 100));
        archerTree.addNode(new javatower.systems.SkillNode("a4b", "Arrow Storm", "🌧️🏹 +3 extra arrows", "archer", 3,
                java.util.List.of("a3b", "a3c"), Map.of("attack", 8), () -> self.archerMultishotCount += 3));
        
        // Tier 5
        archerTree.addNode(new javatower.systems.SkillNode("a5", "Legolas", "🧝 +150 Range, +5 arrows, +15% Crit", "archer", 4,
                java.util.List.of("a4a", "a4b"), Map.of("critChance", 15, "attack", 10), () -> { self.archerSkillRangeBonus += 150; self.archerMultishotCount += 5; }));
    }
    
    // ---- Getters for skill-tree passive callback fields ----------------
    // These fields are mutated by SkillNode Runnable callbacks when nodes are
    // unlocked, and READ by combat/spell formulae — a clean example of the
    // STRATEGY pattern: each node injects its own behaviour via a lambda.
    public int getArcherMultishotCount()   { return archerMultishotCount; }
    public double getArcherSkillRangeBonus() { return archerSkillRangeBonus; }
    public double getNecroSummonBonus()    { return necroSummonBonus; }
    public double getPaladinHealBonus()    { return paladinHealBonus; }
    public double getPyroFireBonus()       { return pyroFireBonus; }
    /** @return true when the f4c "Fire Elemental" skill node has been unlocked. */
    public boolean isPyroElementalActive() { return pyroElementalActive; }
    public int getWarriorArmorBonus()      { return warriorArmorBonus; }

    /**
     * Resets all skill-tree passive callback fields to their default (zero/false) values.
     *
     * <p><b>OOP note — Encapsulation:</b> The respec button in {@link SkillTreePanel}
     * must not manipulate these private fields directly. Instead it calls this single
     * public method, keeping the mutation logic inside the class that owns the data
     * and enforcing the invariant that a respec always starts from a clean state.</p>
     *
     * <p>Call order during respec:
     * <ol>
     *   <li>{@code hero.resetSkillTreeSpecialBonuses()} — zero out all passives</li>
     *   <li>Re-apply every unlocked {@link javatower.systems.SkillNode}'s Runnable callback</li>
     * </ol>
     * This prevents ghost-stacking (e.g. {@code necroSummonBonus} doubling on repeated
     * respec without this guard).
     * </p>
     */
    public void resetSkillTreeSpecialBonuses() {
        archerMultishotCount = 0;
        archerSkillRangeBonus = 0;
        necroSummonBonus = 0;
        paladinHealBonus = 0;
        pyroFireBonus = 0;
        pyroElementalActive = false;
        warriorArmorBonus = 0;
    }

    @Override
    public void takeTurn() {}

    /**
     * Real-time update: move toward click target, auto-attack nearest enemy.
     */
    public void update(double dt, List<Enemy> enemies) {
        if (!isAlive()) return;
        
        // Handle roll cooldown
        if (rollCooldownTimer > 0) {
            rollCooldownTimer -= dt;
        }
        
        // Handle active roll
        if (isRolling) {
            rollTimer -= dt;
            if (rollTimer <= 0) {
                isRolling = false;
                rollCooldownTimer = rollCooldown;
            } else {
                // Continue rolling in direction
                double step = moveSpeed * rollSpeed * dt;
                double newX = Math.max(getRadius(), Math.min(Constants.WORLD_WIDTH - getRadius(), getX() + rollDirectionX * step));
                double newY = Math.max(getRadius(), Math.min(Constants.WORLD_HEIGHT - getRadius(), getY() + rollDirectionY * step));
                setPosition(newX, newY);
            }
            return; // Skip normal movement/attacks while rolling
        }
        
        attackTimer += dt;
        
        // Tick all spell timers and process auto-cast class spells
        tickSpellTimers(dt);
        processClassAutoSpells(dt, enemies);

        // Smooth movement toward click target
        if (moving) {
            double dx = targetX - getX();
            double dy = targetY - getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 3) {
                moving = false;
            } else {
                double step = (moveSpeed + getEquipmentStat("moveSpeed")) * dt;
                if (step > dist) step = dist;
                double nx = dx / dist;
                double ny = dy / dist;
                double newX = Math.max(getRadius(), Math.min(Constants.WORLD_WIDTH - getRadius(), getX() + nx * step));
                double newY = Math.max(getRadius(), Math.min(Constants.WORLD_HEIGHT - getRadius(), getY() + ny * step));
                setPosition(newX, newY);
            }
        }

        // Arrow-key continuous movement (overrides click-to-move while held)
        if (moveUp || moveDown || moveLeft || moveRight) {
            moving = false; // cancel click-to-move
            double adx = 0, ady = 0;
            if (moveUp) ady -= 1;
            if (moveDown) ady += 1;
            if (moveLeft) adx -= 1;
            if (moveRight) adx += 1;
            double alen = Math.sqrt(adx * adx + ady * ady);
            if (alen > 0) {
                adx /= alen;
                ady /= alen;
                double step = (moveSpeed + getEquipmentStat("moveSpeed")) * dt;
                double newX = Math.max(getRadius(), Math.min(Constants.WORLD_WIDTH - getRadius(), getX() + adx * step));
                double newY = Math.max(getRadius(), Math.min(Constants.WORLD_HEIGHT - getRadius(), getY() + ady * step));
                setPosition(newX, newY);
            }
        }

        // Passive regen (1 tick per second)
        regenTimer += dt;
        if (regenTimer >= 1.0) {
            regenTimer -= 1.0;
            // Holy 4pc HP regen
            int regen = SetBonusManager.getHolyPassiveRegen(getEquippedItems());
            if (regen > 0 && getCurrentHealth() < getEffectiveMaxHealth()) {
                setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + regen));
            }
            // Base mana regen: 1/sec (Magic tree m3 skill adds +1 extra)
            int baseManaRegen = 1;
            // Death 4pc mana regen bonus
            int manaRegen = baseManaRegen + SetBonusManager.getDeathManaRegen(getEquippedItems());
            if (mana < getEffectiveMaxMana()) {
                mana = Math.min(getEffectiveMaxMana(), mana + manaRegen);
            }
        }

        // Auto-attack nearest enemy in range (melee or ranged depending on weapon)
        if (enemies != null && attackTimer >= getEffectiveCooldown()) {
            double effectiveRange = getEffectiveRange();
            Enemy nearest = null;
            double minDist = Double.MAX_VALUE;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                double d = distanceTo(e);
                if (d < minDist) {
                    minDist = d;
                    nearest = e;
                }
            }
            if (nearest != null && minDist <= effectiveRange + nearest.getRadius()) {
                attackEnemy(nearest);
                lastAttackTarget = nearest;
                attackedThisFrame = true;
                attackTimer = 0;

                // Fire 4pc: AoE splash — nearby enemies take 30% of damage dealt
                double splashFrac = SetBonusManager.getFireSplashFraction(getEquippedItems());
                if (splashFrac > 0 && lastDamageDealt > 0) {
                    int splashDmg = (int)(lastDamageDealt * splashFrac);
                    if (splashDmg > 0) {
                        for (Enemy splash : enemies) {
                            if (splash != nearest && splash.isAlive()
                                    && distanceTo(splash) <= effectiveRange + splash.getRadius() + 40) {
                                splash.takeDamage(splashDmg);
                            }
                        }
                    }
                }

                if (!nearest.isAlive()) {
                    gainExperience(nearest.getExperienceValue());
                    gainGold(nearest.getGoldValue());
                }
            }
        }
    }

    /**
     * Set a movement target (from mouse click).
     */
    public void moveTo(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.moving = true;
    }

    /**
     * Calculates and performs an auto-attack on the target enemy.
     * <p>
     * Applies weapon-class skill multiplier, set bonuses (Fire 2pc, Holy 4pc),
     * class-based bonuses (Necromancy, Archer, Melee, Holy, Defence),
     * crit chance, Death 4pc life-steal, and trains the relevant weapon skill.
     * </p>
     *
     * @param target the enemy to strike
     * @return actual damage dealt after all modifiers
     */
    public int attackEnemy(Enemy target) {
        int baseDamage = getEffectiveAttack();
        Item[] eq = getEquippedItems();
        
        // Apply weapon class skill modifier
        WeaponClass wc = weapon != null ? weapon.getWeaponClass() : WeaponClass.MELEE;
        double skillMult = skillProgression.getDamageMultiplier(wc);
        baseDamage = (int)(baseDamage * skillMult);

        // Set bonuses: Fire 2pc +25% damage, Holy 4pc +50% vs undead (all enemies are undead)
        baseDamage = (int)(baseDamage * SetBonusManager.getFireDamageBonus(eq));
        baseDamage = (int)(baseDamage * SetBonusManager.getHolyUndeadDamageBonus(eq));

        // Pyromancer tree boosts fire-oriented damage when using Fire set pieces.
        if (SetBonusManager.hasTwoPiece(eq, Item.EquipmentSet.FIRE)) {
            baseDamage = (int)(baseDamage * (1.0 + pyroFireBonus));
        }
        
        // Class-based Holy bonus vs undead
        baseDamage = (int)(baseDamage * SetBonusManager.getHolyUndeadBonus(eq));

        // Melee Berserk: damage increases as HP decreases
        if (SetBonusManager.hasMeleeBerserk(eq)) {
            double healthPercent = (double)getCurrentHealth() / getEffectiveMaxHealth();
            double berserkMult = 1.0 + (1.0 - healthPercent) * 0.5; // up to +50% damage
            baseDamage = (int)(baseDamage * berserkMult);
        }

        // Apply crit with Archer bonus
        int critChance = getEffectiveCritChance() + SetBonusManager.getArcherCritBonus(eq);
        boolean crit = (Math.random() * 100) < critChance;
        int damage = crit ? (int)(baseDamage * 1.5) : baseDamage;
        
        // Divine Shield - chance to double damage
        if (Math.random() * 100 < SetBonusManager.getHolyDivineShieldChance(eq)) {
            damage *= 2;
        }
        
        int dealt = target.takeDamage(damage);
        lastDamageDealt = dealt;
        lastAttackCrit = crit;
        lastAttackWeaponClass = wc;

        // Death 4pc: life steal
        double lifeSteal = SetBonusManager.getDeathLifeSteal(eq);
        if (lifeSteal > 0) {
            int healAmount = (int)(dealt * lifeSteal);
            if (healAmount > 0) {
                setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + healAmount));
            }
        }
        
        // Necromancy class life drain
        double necroDrain = SetBonusManager.getNecromancyLifeDrain(eq);
        if (necroDrain > 0) {
            int healAmount = (int)(dealt * necroDrain);
            if (healAmount > 0) {
                setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + healAmount));
            }
        }

        // Train weapon skill — 1 XP per hit
        skillProgression.addXP(wc, 1.0);

        return dealt;
    }

    /**
     * Process auto-cast class spells based on equipped weapon class items.
     * More items of a class = more powerful passive effects.
     */
    public void processClassAutoSpells(double dt, List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) return;
        
        Item[] eq = getEquippedItems();
        
        // ========== NECROMANCY SPELLS ==========
        // Bone Shield - periodic damage absorption
        if (SetBonusManager.hasNecromancyBoneShield(eq) && necroBoneShieldTimer <= 0) {
            boneShieldActive = true;
            boneShieldDuration = 4.0; // 4 seconds of damage absorption
            necroBoneShieldTimer = NECRO_BONESHIELD_CD;
            // Bone shield absorbs next hit completely
        }
        
        // Summon Army - periodic skeleton spawn (simulated as damage to nearest enemy)
        if (SetBonusManager.hasNecromancySummonArmy(eq) && necroSummonTimer <= 0) {
            // Find nearest enemy and deal summon damage
            Enemy nearest = findNearestEnemy(enemies);
            if (nearest != null) {
                int summonDamage = (int)(getEffectiveAttack() * 0.5 * (1.0 + necroSummonBonus));
                nearest.takeDamage(summonDamage);
            }
            necroSummonTimer = NECRO_SUMMON_CD;
        }
        
        // ========== PYROMANCER SPELLS ==========
        // Fire Elemental - attacks nearby enemies periodically when unlocked via skill tree
        if (pyroElementalActive && pyroElementalTimer <= 0) {
            double pyroMult = 1.0 + pyroFireBonus;
            int elemDamage = (int)(getEffectiveAttack() * 0.6 * pyroMult);
            double elemRange = 110.0;
            int hits = 0;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                if (hits >= 3) break;
                if (distanceTo(e) <= elemRange + e.getRadius()) {
                    e.takeDamage(elemDamage);
                    hits++;
                }
            }
            pyroElementalTimer = PYRO_ELEMENTAL_CD;
        }

        // ========== ARCHER SPELLS ==========
        // Volley - periodic ranged AoE (triggers if any multishot bonus from skills or equipment)
        int totalMultishot = getTotalMultishotCount();
        if (totalMultishot > 0 && archerVolleyTimer <= 0) {
            // Volley fires arrows at multiple enemies
            int arrowCount = totalMultishot + 1;  // Base arrow + multishot arrows
            int arrowsFired = 0;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                double dist = distanceTo(e);
                if (dist <= getEffectiveRange() + e.getRadius()) {
                    int volleyDamage = (int)(getEffectiveAttack() * 0.4); // 40% damage per arrow
                    e.takeDamage(volleyDamage);
                    arrowsFired++;
                    if (arrowsFired >= arrowCount * 2) break; // Fire multiple arrows per volley
                }
            }
            archerVolleyTimer = ARCHER_VOLLEY_CD;
        }
        
        // ========== MELEE SPELLS ==========
        // Whirlwind - periodic AoE spin attack
        if (SetBonusManager.hasMeleeWhirlwind(eq) && meleeWhirlwindTimer <= 0) {
            // Damage all nearby enemies
            int whirlwindDamage = (int)(getEffectiveAttack() * 0.8);
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                double dist = distanceTo(e);
                if (dist <= Constants.MELEE_RANGE + 30 + e.getRadius()) { // Slightly larger range
                    e.takeDamage(whirlwindDamage);
                }
            }
            meleeWhirlwindTimer = MELEE_WHIRLWIND_CD;
        }
        
        // ========== HOLY SPELLS ==========
        // Consecration - periodic damage aura
        if (SetBonusManager.hasHolyConsecration(eq) && holyConsecrationTimer <= 0) {
            // Damage all nearby undead (all enemies are undead in this game)
            int consecrationDamage = (int)(getEffectiveAttack() * 0.3);
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                double dist = distanceTo(e);
                if (dist <= Constants.MELEE_RANGE + 40 + e.getRadius()) {
                    e.takeDamage(consecrationDamage);
                }
            }
            holyConsecrationTimer = HOLY_CONSECRATION_CD;
        }
        
        // ========== DEFENCE SPELLS ==========
        // Shield Block - periodic complete damage negation
        if (SetBonusManager.hasDefenceShieldBlock(eq) && defenceShieldBlockTimer <= 0) {
            shieldBlockActive = true;
            shieldBlockDuration = 2.0; // 2 seconds of blocking
            defenceShieldBlockTimer = DEFENCE_SHIELDBLOCK_CD;
        }
    }
    
    /**
     * Helper to find nearest living enemy.
     */
    private Enemy findNearestEnemy(List<Enemy> enemies) {
        Enemy nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double d = distanceTo(e);
            if (d < minDist) {
                minDist = d;
                nearest = e;
            }
        }
        return nearest;
    }

    public int getLastDamageDealt() { return lastDamageDealt; }
    public boolean wasAttackCrit() { return lastAttackCrit; }
    public WeaponClass getLastAttackWeaponClass() { return lastAttackWeaponClass; }
    public Enemy getLastAttackTarget() { return lastAttackTarget; }
    public boolean didAttackThisFrame() { return attackedThisFrame; }
    public void clearFrameFlags() { attackedThisFrame = false; leveledUpThisFrame = false; }
    public boolean didLevelUpThisFrame() { return leveledUpThisFrame; }

    /**
     * Gain experience and check for level up.
     * @param amount XP gained
     */
    public void gainExperience(int amount) {
        experience += amount;
        while (experience >= experienceToNextLevel) {
            experience -= experienceToNextLevel;
            levelUp();
        }
    }

    /**
     * Level up the hero, increasing stats and skill points.
     */
    public void levelUp() {
        setMaxHealth(getMaxHealth() + 10);
        setCurrentHealth(getEffectiveMaxHealth());
        setAttack(getAttack() + 2);
        setDefence(getDefence() + 1);
        maxMana += 5;
        mana = getEffectiveMaxMana();
        skillPoints++;
        level++;
        experienceToNextLevel = (int)(experienceToNextLevel * 1.2);
        leveledUpThisFrame = true;

        // Expand inventory every 3 levels
        if (level % 3 == 0) {
            int newW = inventory.getWidth() + 1;
            int newH = inventory.getHeight() + 1;
            inventory.expand(newW, newH);
        }
    }

    /**
     * Gain gold.
     * @param amount Amount to gain
     */
    public void gainGold(int amount) {
        gold += amount;
    }

    /**
     * Spend gold if enough is available.
     * @param amount Amount to spend
     * @return True if successful
     */
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    /**
     * Equip an item, returning the previously equipped item in that slot.
     * Two-handed weapons block the offhand slot.
     * Rings support up to 10 slots.
     */
    public Item equipItem(Item item) {
        if (item == null) return null;
        Item previous = null;
        switch (item.getSlot()) {
            case WEAPON:
                previous = weapon;
                weapon = item;
                // Two-handed weapon clears offhand
                if (item.isTwoHanded() && offhand != null) {
                    // Return offhand to inventory handled by caller
                    Item displacedOffhand = offhand;
                    offhand = null;
                    // Store displaced for caller — attach to previous if possible
                    if (previous == null) previous = displacedOffhand;
                }
                break;
            case OFFHAND:
                // Can't equip offhand if weapon is two-handed
                if (weapon != null && weapon.isTwoHanded()) return null;
                previous = offhand;
                offhand = item;
                break;
            case HELMET:
                previous = helmet;
                helmet = item;
                break;
            case CHEST:
                previous = chest;
                chest = item;
                break;
            case LEGS:
                previous = legs;
                legs = item;
                break;
            case BOOTS:
                previous = boots;
                boots = item;
                break;
            case GLOVES:
                previous = gloves;
                gloves = item;
                break;
            case AMULET:
                previous = amulet;
                amulet = item;
                break;
            case RING:
                // Find first empty ring slot (up to 10)
                for (int i = 0; i < rings.length; i++) {
                    if (rings[i] == null) {
                        rings[i] = item;
                        return null; // no previous
                    }
                }
                // All 10 full — replace last ring
                previous = rings[rings.length - 1];
                rings[rings.length - 1] = item;
                break;
            default:
                break;
        }
        return previous;
    }

    /**
     * Unequip item from a specific slot. Returns the removed item.
     * @param slotName one of: weapon, offhand, helmet, chest, legs, boots, gloves, amulet, ring0-ring9
     */
    public Item unequipSlot(String slotName) {
        Item removed = null;
        switch (slotName) {
            case "weapon": removed = weapon; weapon = null; break;
            case "offhand": removed = offhand; offhand = null; break;
            case "helmet": removed = helmet; helmet = null; break;
            case "chest": removed = chest; chest = null; break;
            case "legs": removed = legs; legs = null; break;
            case "boots": removed = boots; boots = null; break;
            case "gloves": removed = gloves; gloves = null; break;
            case "amulet": removed = amulet; amulet = null; break;
            default:
                if (slotName.startsWith("ring")) {
                    int idx = Integer.parseInt(slotName.substring(4));
                    if (idx >= 0 && idx < rings.length) {
                        removed = rings[idx];
                        rings[idx] = null;
                    }
                }
                break;
        }
        return removed;
    }

    /** Returns number of equipped rings. */
    public int getEquippedRingCount() {
        int count = 0;
        for (Item r : rings) if (r != null) count++;
        return count;
    }

    /**
     * Applies damage to the hero, reduced by defence + Defence skill bonus + Defence class bonus. Trains Defence skill.
     * During roll: INVINCIBLE (no damage taken).
     * Shield Block: Chance to negate damage.
     */
    @Override
    public int takeDamage(int damage) {
        // Invincible during roll
        if (isRolling) return 0;
        
        Item[] eq = getEquippedItems();
        
        // Shield Block active - negate damage
        if (shieldBlockActive) {
            return 0;
        }
        
        // Divine Shield chance to negate
        if (Math.random() * 100 < SetBonusManager.getHolyDivineShieldChance(eq)) {
            return 0; // Damage negated by divine intervention
        }
        
        // Apply class mitigation first (Defence class + Warrior passive DR from tree)
        double damageReduction = SetBonusManager.getDefenceDamageReduction(eq);
        double warriorReduction = Math.max(0.0, Math.min(0.60, warriorArmorBonus / 100.0));
        double totalReduction = Math.min(0.85, damageReduction + warriorReduction);
        damage = (int)(damage * (1.0 - totalReduction));
        
        // Effective defence = base + items + skill bonus
        int totalDef = getEffectiveDefence() + skillProgression.getDefenceBonus();
        // Knight 2pc: +25% defence
        totalDef = (int)(totalDef * SetBonusManager.getKnightDefenceBonus(eq));
        int reduced = Math.max(1, damage - totalDef);
        setCurrentHealth(getCurrentHealth() - reduced);
        
        // Reflect damage back to attacker (if we track the attacker)
        // This would need the attacker reference - stored for potential future use
        
        if (getCurrentHealth() <= 0) {
            // Holy Resurrection - revive once per floor
            if (SetBonusManager.hasHolyResurrection(eq) && !holyResurrectionUsed) {
                holyResurrectionUsed = true;
                setCurrentHealth(getEffectiveMaxHealth() / 2);
                // Could add visual effect here
            } else {
                setCurrentHealth(0);
                setAlive(false);
                onDeath();
            }
        }
        // Train defence skill when taking hits (0.5 XP per hit taken)
        if (getEquippedOfClass(WeaponClass.DEFENCE) != null) {
            skillProgression.addXP(WeaponClass.DEFENCE, 0.5);
        }
        return reduced;
    }

    /**
     * Heals the hero, boosted by Holy skill and Holy class items. Trains Holy skill.
     */
    @Override
    public void heal(int amount) {
        if (!isAlive()) return;
        Item[] eq = getEquippedItems();
        // Add flat heal power from equipment
        amount += getEquipmentHealBonus();
        double holyMult = skillProgression.getHolyHealBonus();
        // Holy 2pc set bonus: +20% heal
        holyMult *= SetBonusManager.getHolyHealBonus(eq);
        // Holy class-item scaling and Paladin tree scaling stack multiplicatively
        holyMult *= SetBonusManager.getHolyHealPower(eq);
        holyMult *= (1.0 + paladinHealBonus);
        int boostedAmount = (int)(amount * holyMult);
        setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + boostedAmount));
        skillProgression.addXP(WeaponClass.HOLY, 1.0);
    }

    /**
     * Returns the first equipped item of a given weapon class, or null.
     */
    public Item getEquippedOfClass(WeaponClass wc) {
        for (Item item : getEquippedItems()) {
            if (item != null && item.getWeaponClass() == wc) return item;
        }
        return null;
    }

    /**
     * Use mana if enough is available.
     * @param cost Mana cost
     * @return True if successful
     */
    public boolean useMana(int cost) {
        // Necromancy class can reduce mana costs for spell-heavy builds.
        double reduction = SetBonusManager.getNecromancyManaCostReduction(getEquippedItems());
        int effectiveCost = (int)Math.max(1, Math.round(cost * (1.0 - reduction)));
        if (mana >= effectiveCost) {
            mana -= effectiveCost;
            return true;
        }
        return false;
    }

    // ========== Equipment-aware effective stats ==========

    /**
     * Sums a named stat across all equipped items.
     */
    public int getEquipmentStat(String stat) {
        int total = 0;
        for (Item item : getEquippedItems()) {
            if (item != null) {
                Integer val = item.getStatBonuses().get(stat);
                if (val != null) total += val;
            }
        }
        return total;
    }

    /** Base attack + all equipped item attack bonuses (+50% during ultimate). */
    public int getEffectiveAttack() {
        int attack = getAttack() + getEquipmentStat("attack");
        if (ultimateActive) attack = (int)(attack * 1.5);
        return attack;
    }

    /** Base defence + all equipped item defence bonuses (+50% during ultimate). */
    public int getEffectiveDefence() {
        int def = getDefence() + getEquipmentStat("defence");
        if (ultimateActive) def = (int)(def * 1.5);
        return def;
    }

    /** Base crit chance + all equipped item crit bonuses (+25% during ultimate). */
    public int getEffectiveCritChance() {
        int crit = critChance + getEquipmentStat("critChance");
        crit += SetBonusManager.getArcherSetCritBonus(getEquippedItems());
        if (ultimateActive) crit += 25;
        return crit;
    }

    /** Base max mana + equipped mana bonuses. */
    public int getEffectiveMaxMana() {
        return maxMana + getEquipmentStat("mana");
    }

    /** Base max HP + equipped health bonuses + Defence class bonus. */
    public int getEffectiveMaxHealth() {
        // Support legacy "health" and newer "maxHealth" item stat keys.
        return getMaxHealth()
                + getEquipmentStat("health")
                + getEquipmentStat("maxHealth")
                + SetBonusManager.getDefenceHealthBonus(getEquippedItems());
    }

    /** Total heal power bonus from items (flat). */
    public int getEquipmentHealBonus() {
        return getEquipmentStat("heal");
    }


    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public int getGold() { return gold; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public int getCritChance() { return critChance; }
    public void setCritChance(int critChance) { this.critChance = critChance; }
    public double getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(double speed) { this.moveSpeed = speed; }
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }
    public void setLevel(int level) { this.level = level; }
    public void setGold(int gold) { this.gold = gold; }
    public void setMana(int mana) { this.mana = mana; }
    public void setExperience(int experience) { this.experience = experience; }
    public Inventory getInventory() { return inventory; }
    public SkillProgression getSkillProgression() { return skillProgression; }

    /** Returns all equipped items (non-null only). */
    public Item[] getEquippedItems() {
        List<Item> list = new ArrayList<>();
        Item[] fixed = { weapon, offhand, helmet, chest, legs, boots, gloves, amulet };
        for (Item i : fixed) if (i != null) list.add(i);
        for (Item r : rings) if (r != null) list.add(r);
        return list.toArray(new Item[0]);
    }

    // Equipment slot getters
    public Item getWeapon() { return weapon; }
    public Item getOffhand() { return offhand; }
    public Item getHelmet() { return helmet; }
    public Item getChest() { return chest; }
    public Item getLegs() { return legs; }
    public Item getBoots() { return boots; }
    public Item getGloves() { return gloves; }
    public Item getAmulet() { return amulet; }
    public Item[] getRings() { return rings; }

    /** Effective attack range — melee base, plus ranged weapon range stat, ranged skill bonus, skill tree bonus, and Archer class bonus. */
    public double getEffectiveRange() {
        double range = Constants.MELEE_RANGE;
        if (weapon != null) {
            Integer rangeBonus = weapon.getStatBonuses().get("range");
            if (rangeBonus != null) range += rangeBonus;
        }
        if (offhand != null) {
            Integer rangeBonus = offhand.getStatBonuses().get("range");
            if (rangeBonus != null) range += rangeBonus;
        }
        range += skillProgression.getRangedRangeBonus();
        
        // Archer skill tree range bonus
        range += archerSkillRangeBonus;
        
        // Archer SET range bonus (flat +50 at 2pc)
        Item[] eq = getEquippedItems();
        range += SetBonusManager.getArcherSetRangeBonus(eq);
        
        // Archer WeaponClass range bonus (percentage)
        range *= (1.0 + SetBonusManager.getArcherRangeBonus(eq));
        
        return range;
    }
    
    /** Total multishot arrow count from all sources (skill tree + equipment sets). */
    public int getTotalMultishotCount() {
        Item[] eq = getEquippedItems();
        return archerMultishotCount + 
               SetBonusManager.getArcherSetMultishotBonus(eq) + 
               SetBonusManager.getArcherExtraProjectiles(eq);
    }

    /** Effective attack cooldown — base minus item speed bonus, melee skill, Knight 4pc, and Melee class bonus. */
    public double getEffectiveCooldown() {
        double cd = attackCooldown;
        Item[] eq = getEquippedItems();
        // Item "speed" stat: each point = 0.02s faster
        cd -= getEquipmentStat("speed") * 0.02;
        cd -= skillProgression.getMeleeSpeedBonus();
        cd -= SetBonusManager.getKnightSpeedBonus(eq);
        // Archer 4pc grants additional attack speed.
        cd -= SetBonusManager.getArcherSetSpeedBonus(eq);
        // Melee class attack speed bonus
        cd -= SetBonusManager.getMeleeAttackSpeed(eq);
        return Math.max(0.15, cd); // minimum 0.15s
    }
    
    // Class-based skill tree getters
    public SkillTree getWarriorTree() { return warriorTree; }
    public SkillTree getPaladinTree() { return paladinTree; }
    public SkillTree getNecromancerTree() { return necromancerTree; }
    public SkillTree getPyromancerTree() { return pyromancerTree; }
    public SkillTree getArcherTree() { return archerTree; }
    
    // Legacy getters for compatibility (map to closest new tree)
    public SkillTree getCombatTree() { return warriorTree; }
    public SkillTree getMagicTree() { return paladinTree; }
    public SkillTree getUtilityTree() { return archerTree; }

    // Arrow-key movement setters
    public void setMoveUp(boolean v) { moveUp = v; }
    public void setMoveDown(boolean v) { moveDown = v; }
    public void setMoveLeft(boolean v) { moveLeft = v; }
    public void setMoveRight(boolean v) { moveRight = v; }

    // Kill stats
    public void recordKill(int damage, int gold, int xp) {
        totalKills++;
        totalDamageDealt += damage;
        totalGoldEarned += gold;
        totalXPEarned += xp;
    }
    public int getTotalKills() { return totalKills; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalGoldEarned() { return totalGoldEarned; }
    public int getTotalXPEarned() { return totalXPEarned; }
    
    // ========== Dodge/Roll Methods ==========
    
    /**
     * Activates a dodge/roll in the specified direction.
     * @param dirX X direction (-1 to 1)
     * @param dirY Y direction (-1 to 1)
     * @return true if roll was activated, false if on cooldown
     */
    public boolean roll(double dirX, double dirY) {
        if (isRolling || rollCooldownTimer > 0) return false;
        
        // Normalize direction
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len < 0.001) return false;
        
        rollDirectionX = dirX / len;
        rollDirectionY = dirY / len;
        isRolling = true;
        rollTimer = rollDuration;
        return true;
    }
    
    /**
     * Roll in current movement direction (for WASD controls).
     */
    public boolean rollInMovementDirection() {
        double dx = 0, dy = 0;
        if (moveUp) dy -= 1;
        if (moveDown) dy += 1;
        if (moveLeft) dx -= 1;
        if (moveRight) dx += 1;
        
        // If no movement keys, roll toward last click target
        if (dx == 0 && dy == 0 && moving) {
            dx = targetX - getX();
            dy = targetY - getY();
        }
        
        return roll(dx, dy);
    }
    
    /**
     * Instantly roll toward cursor position.
     */
    public boolean rollTo(double targetX, double targetY) {
        return roll(targetX - getX(), targetY - getY());
    }
    
    public boolean isRolling() { return isRolling; }
    public double getRollCooldownPercent() { 
        if (rollCooldownTimer <= 0) return 1.0;
        return 1.0 - (rollCooldownTimer / rollCooldown);
    }
    public double getRollDurationRemaining() { return rollTimer; }
    public boolean canRoll() { return !isRolling && rollCooldownTimer <= 0; }
    
    // Ultimate ability methods
    public void setUltimateActive(boolean active) { this.ultimateActive = active; }
    public boolean isUltimateActive() { return ultimateActive; }
    
    // ========== Class Spell Timer Getters (for UI) ==========
    public double getNecroBoneShieldTimer() { return necroBoneShieldTimer; }
    public double getNecroSummonTimer() { return necroSummonTimer; }
    public double getArcherVolleyTimer() { return archerVolleyTimer; }
    public double getMeleeWhirlwindTimer() { return meleeWhirlwindTimer; }
    public double getHolyConsecrationTimer() { return holyConsecrationTimer; }
    public double getDefenceShieldBlockTimer() { return defenceShieldBlockTimer; }
    
    public boolean isBoneShieldActive() { return boneShieldActive; }
    public boolean isShieldBlockActive() { return shieldBlockActive; }
    public double getBoneShieldDuration() { return boneShieldDuration; }
    public double getShieldBlockDuration() { return shieldBlockDuration; }
    
    /** Resets Holy Resurrection for new floor */
    public void resetHolyResurrection() { holyResurrectionUsed = false; }
    public boolean isHolyResurrectionUsed() { return holyResurrectionUsed; }
    
    // Equipment getters omitted for brevity
}
