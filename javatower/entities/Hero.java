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
    private SkillTree combatTree, magicTree, utilityTree;
    private SkillProgression skillProgression;

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
    private double archerVolleyTimer = 0;
    private double meleeWhirlwindTimer = 0;
    private double holyConsecrationTimer = 0;
    private double defenceShieldBlockTimer = 0;

    /** Cooldown durations (seconds) for weapon class spells. */
    public static final double NECRO_BONESHIELD_CD = 8.0;
    public static final double NECRO_SUMMON_CD = 12.0;
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
     * Initializes the three skill trees with branching nodes.
     * Costs: Tier 1 = 1pt, Tier 2 = 1pt, Tier 3 = 2pt, Tier 4 = 2pt, Tier 5 = 4pt
     * Bigger trees with more choices and special passives!
     */
    private void initSkillTrees() {
        // ========== COMBAT TREE (10 nodes) ==========
        combatTree = new SkillTree("Combat");
        
        // Tier 1 - Basics
        combatTree.addNode(new javatower.systems.SkillNode("c1", "Sharpen", "⚔️ +4 Attack", "combat", 1,
                null, Map.of("attack", 4), null));
        
        // Tier 2 - Branching
        combatTree.addNode(new javatower.systems.SkillNode("c2a", "Precision", "🎯 +6% Crit", "combat", 1,
                java.util.List.of("c1"), Map.of("critChance", 6), null));
        combatTree.addNode(new javatower.systems.SkillNode("c2b", "Power Strike", "💪 +6 Attack", "combat", 1,
                java.util.List.of("c1"), Map.of("attack", 6), null));
        
        // Tier 3 - Specialization
        combatTree.addNode(new javatower.systems.SkillNode("c3a", "Berserker", "🔥 +8 ATK, +15 HP, +5 Speed", "combat", 2,
                java.util.List.of("c2a"), Map.of("attack", 8, "maxHealth", 15), null));
        combatTree.addNode(new javatower.systems.SkillNode("c3b", "Assassin", "🗡️ +10% Crit, +4 ATK", "combat", 2,
                java.util.List.of("c2a"), Map.of("critChance", 10, "attack", 4), null));
        combatTree.addNode(new javatower.systems.SkillNode("c3c", "Brawler", "👊 +6 ATK, +8 DEF, +20 HP", "combat", 2,
                java.util.List.of("c2b"), Map.of("attack", 6, "defence", 8, "maxHealth", 20), null));
        combatTree.addNode(new javatower.systems.SkillNode("c3d", "Weapon Master", "⚔️ +10 Attack", "combat", 2,
                java.util.List.of("c2b"), Map.of("attack", 10), null));
        
        // Tier 4 - Advanced
        combatTree.addNode(new javatower.systems.SkillNode("c4a", "Frenzy", "🔥🔥 +12 ATK, +10% Speed", "combat", 2,
                java.util.List.of("c3a", "c3b"), Map.of("attack", 12, "speed", 10), null));
        combatTree.addNode(new javatower.systems.SkillNode("c4b", "Executioner", "💀 +5% Crit, +6 ATK", "combat", 2,
                java.util.List.of("c3b", "c3d"), Map.of("critChance", 5, "attack", 6), null));
        
        // Tier 5 - Ultimate
        combatTree.addNode(new javatower.systems.SkillNode("c5", "Warlord", "👑 +10 ATK, +8 DEF, +25 HP", "combat", 4,
                java.util.List.of("c4a", "c4b"), Map.of("attack", 10, "defence", 8, "maxHealth", 25), null));

        // ========== MAGIC TREE (10 nodes) ==========
        magicTree = new SkillTree("Magic");
        
        // Tier 1
        magicTree.addNode(new javatower.systems.SkillNode("m1", "Arcane Mind", "🔮 +20 Mana", "magic", 1,
                null, Map.of("maxMana", 20), null));
        
        // Tier 2
        magicTree.addNode(new javatower.systems.SkillNode("m2a", "Inner Light", "✨ +25 HP", "magic", 1,
                java.util.List.of("m1"), Map.of("maxHealth", 25), null));
        magicTree.addNode(new javatower.systems.SkillNode("m2b", "Mana Flow", "💧 +15 Mana, +5 HP", "magic", 1,
                java.util.List.of("m1"), Map.of("maxMana", 15, "maxHealth", 5), null));
        
        // Tier 3
        magicTree.addNode(new javatower.systems.SkillNode("m3a", "Healing Aura", "💚 +35 HP, +10 Mana", "magic", 2,
                java.util.List.of("m2a"), Map.of("maxHealth", 35, "maxMana", 10), null));
        magicTree.addNode(new javatower.systems.SkillNode("m3b", "Protective Ward", "🛡️ +20 HP, +5 DEF", "magic", 2,
                java.util.List.of("m2a"), Map.of("maxHealth", 20, "defence", 5), null));
        magicTree.addNode(new javatower.systems.SkillNode("m3c", "Mana Surge", "⚡ +30 Mana", "magic", 2,
                java.util.List.of("m2b"), Map.of("maxMana", 30), null));
        magicTree.addNode(new javatower.systems.SkillNode("m3d", "Spell Focus", "🔥 +10 Mana, Abilities cost 10% less", "magic", 2,
                java.util.List.of("m2b"), Map.of("maxMana", 10), null));
        
        // Tier 4
        magicTree.addNode(new javatower.systems.SkillNode("m4a", "Saint", "✨✨ +30 HP, +20 Mana", "magic", 2,
                java.util.List.of("m3a", "m3b"), Map.of("maxHealth", 30, "maxMana", 20), null));
        magicTree.addNode(new javatower.systems.SkillNode("m4b", "Sorcerer", "🔮🔮 +40 Mana, +5% Damage", "magic", 2,
                java.util.List.of("m3c", "m3d"), Map.of("maxMana", 40, "attack", 2), null));
        
        // Tier 5
        magicTree.addNode(new javatower.systems.SkillNode("m5", "Archmage", "🌟 +25 Mana, +25 HP, +10 Attack", "magic", 4,
                java.util.List.of("m4a", "m4b"), Map.of("maxMana", 25, "maxHealth", 25, "attack", 10), null));

        // ========== UTILITY TREE (10 nodes) ==========
        utilityTree = new SkillTree("Utility");
        
        // Tier 1
        utilityTree.addNode(new javatower.systems.SkillNode("u1", "Vitality", "❤️ +25 HP", "utility", 1,
                null, Map.of("maxHealth", 25), null));
        
        // Tier 2
        utilityTree.addNode(new javatower.systems.SkillNode("u2a", "Thick Skin", "🛡️ +5 DEF", "utility", 1,
                java.util.List.of("u1"), Map.of("defence", 5), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u2b", "Swiftness", "💨 +20 Speed", "utility", 1,
                java.util.List.of("u1"), Map.of("speed", 20), null));
        
        // Tier 3
        utilityTree.addNode(new javatower.systems.SkillNode("u3a", "Iron Wall", "🏰 +8 DEF, +15 HP", "utility", 2,
                java.util.List.of("u2a"), Map.of("defence", 8, "maxHealth", 15), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u3b", "Fortress", "🏯 +5 DEF, +15 HP", "utility", 2,
                java.util.List.of("u2a"), Map.of("defence", 5, "maxHealth", 15), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u3c", "Agility", "🤸 +25 Speed, +5% Crit", "utility", 2,
                java.util.List.of("u2b"), Map.of("speed", 25, "critChance", 5), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u3d", "Evasion", "💫 +15 Speed, +5% Dodge", "utility", 2,
                java.util.List.of("u2b"), Map.of("speed", 15, "critChance", 5), null));
        
        // Tier 4
        utilityTree.addNode(new javatower.systems.SkillNode("u4a", "Juggernaut", "🐢 +10 DEF, +30 HP", "utility", 2,
                java.util.List.of("u3a", "u3b"), Map.of("defence", 10, "maxHealth", 30), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u4b", "Wind Walker", "🌪️ +30 Speed, +10 Mana", "utility", 2,
                java.util.List.of("u3c", "u3d"), Map.of("speed", 30, "maxMana", 10), null));
        
        // Tier 5
        utilityTree.addNode(new javatower.systems.SkillNode("u5", "Legend", "⭐ +8 DEF, +30 HP", "utility", 4,
                java.util.List.of("u4a", "u4b"), Map.of("defence", 8, "maxHealth", 30), null));
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
                int summonDamage = (int)(getEffectiveAttack() * 0.5); // Skeletons deal 50% hero damage
                nearest.takeDamage(summonDamage);
            }
            necroSummonTimer = NECRO_SUMMON_CD;
        }
        
        // ========== ARCHER SPELLS ==========
        // Volley - periodic ranged AoE
        if (SetBonusManager.getArcherExtraProjectiles(eq) > 0 && archerVolleyTimer <= 0) {
            // Volley fires arrows at multiple enemies
            int arrowCount = SetBonusManager.getArcherExtraProjectiles(eq) + 1;
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
        
        // Apply Defence class damage reduction
        double damageReduction = SetBonusManager.getDefenceDamageReduction(eq);
        damage = (int)(damage * (1.0 - damageReduction));
        
        // Effective defence = base + items + skill bonus + Defence class HP bonus
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
        // Holy class heal bonus
        holyMult = SetBonusManager.getHolyHealPower(eq);
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
        if (mana >= cost) {
            mana -= cost;
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
        if (ultimateActive) crit += 25;
        return crit;
    }

    /** Base max mana + equipped mana bonuses. */
    public int getEffectiveMaxMana() {
        return maxMana + getEquipmentStat("mana");
    }

    /** Base max HP + equipped health bonuses + Defence class bonus. */
    public int getEffectiveMaxHealth() {
        return getMaxHealth() + getEquipmentStat("health") + SetBonusManager.getDefenceHealthBonus(getEquippedItems());
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

    /** Effective attack range — melee base, plus ranged weapon range stat, ranged skill bonus, and Archer class bonus. */
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
        
        // Archer class range bonus
        Item[] eq = getEquippedItems();
        range *= (1.0 + SetBonusManager.getArcherRangeBonus(eq));
        
        return range;
    }

    /** Effective attack cooldown — base minus item speed bonus, melee skill, Knight 4pc, and Melee class bonus. */
    public double getEffectiveCooldown() {
        double cd = attackCooldown;
        Item[] eq = getEquippedItems();
        // Item "speed" stat: each point = 0.02s faster
        cd -= getEquipmentStat("speed") * 0.02;
        cd -= skillProgression.getMeleeSpeedBonus();
        cd -= SetBonusManager.getKnightSpeedBonus(eq);
        // Melee class attack speed bonus
        cd -= SetBonusManager.getMeleeAttackSpeed(eq);
        return Math.max(0.15, cd); // minimum 0.15s
    }
    public SkillTree getCombatTree() { return combatTree; }
    public SkillTree getMagicTree() { return magicTree; }
    public SkillTree getUtilityTree() { return utilityTree; }

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
