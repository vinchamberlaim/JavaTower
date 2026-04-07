package javatower.entities;

import java.util.List;
import javatower.util.Constants;
import javatower.util.Difficulty;
import javatower.factories.EnemyFactory;

/**
 * Abstract base class for every undead enemy in JavaTower.
 * <p>
 * Enemies use real-time pixel-coordinate movement, attack the {@link Hero}
 * on a cooldown timer, and optionally consume {@link BonePile}s left by
 * dead siblings to grow stronger. Each concrete enemy subclass (Zombie,
 * Lich, NecromancerKing, etc.) overrides {@link #update(double, Hero)}
 * and {@link #specialAbility()} to implement unique AI behaviour.
 * </p>
 * <p>
 * An <b>Elite Modifier</b> system ({@link EliteModifier}) can be applied
 * to any enemy to grant bonus stats and special traits (shields,
 * regeneration, explosion on death).
 * </p>
 * <p>
 * <b>CIS096 relevance:</b> core demonstration of inheritance + polymorphism.
 * The game loop stores enemies as {@code List<Enemy>} and calls shared methods,
 * while each subclass provides distinct runtime behaviour via overrides.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see EnemyType
 * @see EliteModifier
 * @see BonePile
 */
public abstract class Enemy extends Entity {
    /**
     * Enumerates all enemy types with base stats that scale per wave.
     * <p>
     * Each type has a <b>tier</b> (1–10) indicating overall strength.
     * Stats ({@code hp, atk, def, exp, gold}) are multiplied by
     * {@code 1 + waveLevel * 0.05} at spawn time.
     * </p>
     */
    public enum EnemyType {
        /** Tier 1 — slow, tanky melee fodder. */
        ZOMBIE(1, 30, 5, 2, 10, 5),
        /** Tier 2 — ranged attacker. */
        SKELETON(2, 25, 8, 1, 15, 8),
        /** Tier 3 — fast melee rusher. */
        GHOUL(3, 40, 7, 3, 20, 12),
        /** Tier 4 — balanced melee. */
        WIGHT(4, 60, 10, 5, 30, 18),
        /** Tier 5 — phasing ghost, medium range. */
        WRAITH(5, 35, 12, 2, 25, 15),
        /** Tier 6 — self-resurrecting mini-boss. */
        REVENANT(6, 80, 12, 8, 40, 25),
        /** Tier 7 — heavily-armoured slow melee. */
        DEATH_KNIGHT(7, 120, 15, 12, 60, 40),
        /** Tier 8 — ranged summoner mini-boss with mass-resurrection. */
        LICH(8, 70, 20, 6, 80, 50),
        /** Tier 8 — eldritch cult caster. */
        CULTIST(8, 85, 22, 7, 95, 58),
        /** Tier 9 — amphibious abyssal brute. */
        DEEP_ONE(9, 170, 20, 14, 120, 82),
        /** Tier 10 — unstable cosmic horror juggernaut. */
        SHOGGOTH(10, 320, 28, 18, 180, 130),
        /** Tier 9 — colossal melee mini-boss. */
        BONE_COLOSSUS(9, 200, 18, 15, 100, 70),
        /** Tier 10 — the final boss with dual-weapon AI. */
        NECROMANCER_KING(10, 500, 30, 20, 500, 200);

        public final int tier, hp, atk, def, exp, gold;
        EnemyType(int tier, int hp, int atk, int def, int exp, int gold) {
            this.tier = tier; this.hp = hp; this.atk = atk; this.def = def;
            this.exp = exp; this.gold = gold;
        }
    }

    private EnemyType type;
    /** XP awarded to the hero when this enemy is killed. */
    private int experienceValue;
    /** Gold awarded to the hero when this enemy is killed. */
    private int goldValue;
    /** Wave level at which this enemy was spawned (scales stats). */
    private int waveLevel;
    /** True if this enemy is the final boss of the run. */
    private boolean isBoss;
    /** True if this enemy is a mid-run mini-boss (Revenant, Lich, Bone Colossus). */
    private boolean isMiniBoss;
    /** Wraith ability — can move through obstacles. */
    private boolean canPhase;
    /** Revenant ability — can self-resurrect once. */
    private boolean canResurrect;
    /** Lich / Necromancer King ability — can summon minions. */
    private boolean canSummon;
    /** Reference to all living enemies (for collision avoidance and summoning). */
    private List<Enemy> siblings;
    /** Reference to the global bone-pile list (for consumption and summoning). */
    private List<BonePile> bonePiles;

    /** Movement speed in pixels per second (varies by type). */
    private double speed;
    /** Seconds between consecutive attacks. */
    private double attackCooldown;
    /** Accumulator tracking time since last attack. */
    private double attackTimer;
    /** Maximum range in pixels at which this enemy will attack the hero. */
    private double attackRange;

    // ========== ELITE MODIFIER SYSTEM ==========
    /** The elite modifier currently active on this enemy ({@code NONE} for regular mobs). */
    private EliteModifier eliteModifier = EliteModifier.NONE;
    /** Timer for HP regeneration (Regenerating elites regen 2 % HP/s). */
    private double hpRegenTimer = 0;
    /** Whether the Shielded elite’s one-time damage absorb is still active. */
    private boolean hasShield = false;
    /** Whether this Explosive elite will detonate on death. */
    private boolean willExplode = false;
    /** Whether this enemy was summoned by a Lich (not part of wave spawn count). */
    private boolean isSummoned = false;

    /**
     * Constructs an enemy of the given type at the specified wave level.
     * Base stats from {@link EnemyType} are multiplied by
     * {@code 1 + waveLevel * 0.05}, and movement / attack / radius
     * defaults are assigned from the type’s switch block.
     *
     * @param type      the enemy archetype (determines base stats and abilities)
     * @param waveLevel the wave number used to scale difficulty
     */
    public Enemy(EnemyType type, int waveLevel) {
        this.type = type;
        this.waveLevel = waveLevel;
        // Increased scaling from 0.05 to 0.12 for better difficulty curve
        double scale = 1.0 + waveLevel * 0.12;
        Difficulty diff = Difficulty.getCurrent();
        setMaxHealth((int)(type.hp * scale * diff.enemyHpMul));
        setCurrentHealth(getMaxHealth());
        setAttack((int)(type.atk * scale * diff.enemyAtkMul));
        setDefence((int)(type.def * scale));
        this.experienceValue = (int)(type.exp * scale);
        this.goldValue = (int)(type.gold * scale);
        setAlive(true);

        // Defaults
        this.speed = Constants.ENEMY_SPEED_NORMAL;
        this.attackCooldown = 1.0;
        this.attackTimer = 0;
        this.attackRange = Constants.MELEE_RANGE;

        // Set special flags and sizes by type
        switch (type) {
            case ZOMBIE:
                speed = Constants.ENEMY_SPEED_SLOW;
                setRadius(Constants.ENEMY_RADIUS_SMALL);
                break;
            case SKELETON:
                speed = Constants.ENEMY_SPEED_NORMAL;
                attackRange = 150; // ranged
                setRadius(Constants.ENEMY_RADIUS_SMALL);
                break;
            case GHOUL:
                speed = Constants.ENEMY_SPEED_FAST;
                setRadius(Constants.ENEMY_RADIUS_SMALL);
                break;
            case WIGHT:
                speed = Constants.ENEMY_SPEED_NORMAL;
                setRadius(Constants.ENEMY_RADIUS_MEDIUM);
                break;
            case WRAITH:
                canPhase = true;
                speed = Constants.ENEMY_SPEED_FAST;
                attackRange = 80;
                setRadius(Constants.ENEMY_RADIUS_MEDIUM);
                break;
            case REVENANT:
                canResurrect = true;
                speed = Constants.ENEMY_SPEED_NORMAL;
                setRadius(Constants.ENEMY_RADIUS_MEDIUM);
                isMiniBoss = true;
                break;
            case DEATH_KNIGHT:
                speed = Constants.ENEMY_SPEED_SLOW;
                setRadius(Constants.ENEMY_RADIUS_LARGE);
                break;
            case LICH:
                canSummon = true;
                speed = Constants.ENEMY_SPEED_SLOW;
                attackRange = 180;
                setRadius(Constants.ENEMY_RADIUS_LARGE);
                isMiniBoss = true;
                break;
            case CULTIST:
                speed = Constants.ENEMY_SPEED_NORMAL;
                attackRange = 200;
                setRadius(Constants.ENEMY_RADIUS_MEDIUM);
                break;
            case DEEP_ONE:
                speed = Constants.ENEMY_SPEED_NORMAL * 0.9;
                attackRange = 95;
                setRadius(Constants.ENEMY_RADIUS_LARGE);
                break;
            case SHOGGOTH:
                speed = Constants.ENEMY_SPEED_SLOW * 0.8;
                attackRange = 120;
                setRadius(Constants.ENEMY_RADIUS_BOSS);
                isMiniBoss = true;
                break;
            case BONE_COLOSSUS:
                speed = Constants.ENEMY_SPEED_SLOW * 0.7;
                setRadius(Constants.ENEMY_RADIUS_BOSS);
                isMiniBoss = true;
                break;
            case NECROMANCER_KING:
                canSummon = true;
                isBoss = true;
                speed = Constants.ENEMY_SPEED_NORMAL;
                attackRange = 160;
                setRadius(Constants.ENEMY_RADIUS_BOSS);
                break;
            default:
                setRadius(Constants.ENEMY_RADIUS_SMALL);
                break;
        }
    }

    /**
     * Real-time update: move toward hero, attack if in range.
     * @param dt delta time in seconds
     * @param hero the target hero
     */
    public void update(double dt, Hero hero) {
        if (!isAlive() || hero == null) return;
        attackTimer += dt;

        // Bone pile consumption: tier 3+ enemies grow by eating piles (#20)
        if (type.tier >= 3 && bonePiles != null) {
            tryConsumeBonePile();
        }

        double dist = distanceTo(hero);

        if (dist <= attackRange) {
            // In range: attack on cooldown
            if (attackTimer >= attackCooldown) {
                hero.takeDamage(getAttack());
                attackTimer = 0;
                onAttack(hero);
            }
        } else {
            // Move toward hero
            smoothMoveToward(hero.getX(), hero.getY(), dt);
        }
    }

    /**
     * Called when the enemy attacks. Override for special on-hit effects.
     */
    protected void onAttack(Hero hero) {}

    /**
     * Smooth pixel movement toward a target, with collision avoidance.
     */
    protected void smoothMoveToward(double targetX, double targetY, double dt) {
        double dx = targetX - getX();
        double dy = targetY - getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;

        // Normalize direction
        double nx = dx / dist;
        double ny = dy / dist;
        double step = speed * dt;
        if (step > dist) step = dist;

        double newX = getX() + nx * step;
        double newY = getY() + ny * step;

        // Collision with siblings
        if (siblings != null) {
            for (Enemy e : siblings) {
                if (e == this || !e.isAlive()) continue;
                double edx = newX - e.getX();
                double edy = newY - e.getY();
                double eDist = Math.sqrt(edx * edx + edy * edy);
                double minSep = getRadius() + e.getRadius();

                if (eDist < minSep) {
                    if ((isBoss || isMiniBoss) && !e.isBoss() && !e.isMiniBoss()) {
                        // Boss tramples smaller enemy (not other bosses/mini-bosses)
                        e.setCurrentHealth(0);
                        e.setAlive(false);
                    } else {
                        // Push away from sibling
                        if (eDist > 0.1) {
                            double pushX = edx / eDist;
                            double pushY = edy / eDist;
                            newX += pushX * (minSep - eDist) * 0.5;
                            newY += pushY * (minSep - eDist) * 0.5;
                        } else {
                            // Nudge randomly if exactly overlapping
                            newX += (Math.random() - 0.5) * 2;
                            newY += (Math.random() - 0.5) * 2;
                        }
                    }
                }
            }
        }

        // Clamp to screen bounds
        double r = getRadius();
        newX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, newX));
        newY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, newY));

        setPosition(newX, newY);
    }

    /** Not used in real-time mode, but kept for compatibility. */
    @Override
    public void takeTurn() {}

    public abstract void specialAbility();

    // Setters for real-time fields
    public void setSiblings(List<Enemy> siblings) { this.siblings = siblings; }
    public void setBonePiles(List<BonePile> bonePiles) { this.bonePiles = bonePiles; }
    protected List<BonePile> getBonePiles() { return bonePiles; }
    protected List<Enemy> getSiblings() { return siblings; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setAttackRange(double range) { this.attackRange = range; }
    public void setAttackCooldown(double cd) { this.attackCooldown = cd; }

    /**
     * If this enemy is close to a bone pile, consume it to grow stronger.
     * Radius +20%, ATK +15%, HP +20%. Creates urgency to kill enemies fast.
     * Also ensures enemy stays on screen after growing.
     */
    private void tryConsumeBonePile() {
        double consumeRange = getRadius() + 20;
        for (int i = bonePiles.size() - 1; i >= 0; i--) {
            BonePile bp = bonePiles.get(i);
            if (bp.isEmpty()) continue;
            double dx = getX() - bp.getX();
            double dy = getY() - bp.getY();
            if (Math.sqrt(dx * dx + dy * dy) <= consumeRange) {
                bp.consume(bp.getBoneCount()); // consume entire pile
                if (bp.isEmpty()) bonePiles.remove(i);
                // Grow stronger (NERFED: slower growth, cap max radius)
                double newRadius = Math.min(getRadius() * 1.08, Constants.ENEMY_RADIUS_MAX); // 8% growth (was 20%)
                setRadius(newRadius);
                setAttack((int)(getAttack() * 1.05)); // 5% attack (was 15%)
                setMaxHealth((int)(getMaxHealth() * 1.08)); // 8% HP (was 20%)
                setCurrentHealth(Math.min(getCurrentHealth() + (int)(getMaxHealth() * 0.1), getMaxHealth())); // 10% heal (was 20%)
                
                // CRITICAL FIX: Adjust position to stay on screen after growing
                double r = getRadius();
                double newX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, getX()));
                double newY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, getY()));
                setPosition(newX, newY);
                
                break; // only consume one pile per frame
            }
        }
    }

    /**
     * Summoner hook: consume a nearby bone pile and spawn a minion.
     * Returns the new enemy, or null if no pile was close enough.
     */
    protected Enemy summonFromBones(double range) {
        if (bonePiles == null || siblings == null) return null;
        for (int i = 0; i < bonePiles.size(); i++) {
            BonePile bp = bonePiles.get(i);
            double dx = getX() - bp.getX();
            double dy = getY() - bp.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= range && !bp.isEmpty()) {
                int tier = bp.consume(bp.getBoneCount()); // consume entire pile
                if (bp.isEmpty()) bonePiles.remove(i);
                // Spawn a minion at the bone pile location
                EnemyType spawnType = tier >= 5 ? EnemyType.SKELETON : EnemyType.ZOMBIE;
                Enemy minion = EnemyFactory.createEnemy(spawnType, getWaveLevel());
                minion.setPosition(bp.getX(), bp.getY());
                minion.setSiblings(siblings);
                minion.setBonePiles(bonePiles);
                minion.setSummoned(true); // Mark as summoned, not part of wave count
                siblings.add(minion);
                return minion;
            }
        }
        return null;
    }

    // ========== ELITE MODIFIER SYSTEM ==========
    
    /**
     * Applies an {@link EliteModifier} to this enemy, scaling HP, damage,
     * speed, and reward values and enabling special traits.
     *
     * @param modifier the elite modifier to apply
     */
    public void applyEliteModifier(EliteModifier modifier) {
        this.eliteModifier = modifier;
        if (modifier == EliteModifier.NONE) return;
        
        // Apply stat multipliers
        int newMaxHp = (int)(getMaxHealth() * modifier.getHpMultiplier());
        setMaxHealth(newMaxHp);
        setCurrentHealth(newMaxHp);
        setAttack((int)(getAttack() * modifier.getDamageMultiplier()));
        speed *= modifier.getSpeedMultiplier();
        experienceValue = (int)(experienceValue * modifier.getRewardMultiplier());
        goldValue = (int)(goldValue * modifier.getRewardMultiplier());
        
        // Apply special flags
        if (modifier == EliteModifier.SHIELDED) {
            hasShield = true;
        }
        if (modifier == EliteModifier.EXPLOSIVE) {
            willExplode = true;
        }
    }
    
    public EliteModifier getEliteModifier() { return eliteModifier; }
    public boolean isElite() { return eliteModifier != EliteModifier.NONE; }
    public boolean hasShield() { return hasShield; }
    public boolean willExplode() { return willExplode; }
    public boolean isSummoned() { return isSummoned; }
    public void setSummoned(boolean summoned) { this.isSummoned = summoned; }
    
    public void breakShield() { hasShield = false; }
    
    public void updateRegen(double dt) {
        if (eliteModifier != EliteModifier.REGENERATING) return;
        if (!isAlive()) return;
        
        hpRegenTimer += dt;
        if (hpRegenTimer >= 1.0) { // Regen every second
            hpRegenTimer = 0;
            int regenAmount = (int)(getMaxHealth() * 0.02); // 2% per second
            setCurrentHealth(Math.min(getMaxHealth(), getCurrentHealth() + regenAmount));
        }
    }
    
    /**
     * Override of {@link Entity#takeDamage(int)} — Shielded elites
     * absorb the first hit entirely (shield breaks, 0 damage taken).
     *
     * @param damage raw incoming damage before defence
     * @return actual damage dealt after shield / defence reduction
     */
    @Override
    public int takeDamage(int damage) {
        // Shield absorbs first hit
        if (hasShield) {
            hasShield = false;
            return 0; // No damage taken
        }
        return super.takeDamage(damage);
    }
    
    @Override
    public String getName() {
        return eliteModifier.getDisplayName(type.name());
    }

    // Getters
    public EnemyType getType() { return type; }
    public EnemyType getEnemyType() { return type; }
    public int getExperienceValue() { return experienceValue; }
    public int getGoldValue() { return goldValue; }
    public int getWaveLevel() { return waveLevel; }
    public boolean isBoss() { return isBoss; }
    public boolean isMiniBoss() { return isMiniBoss; }
    public boolean canPhase() { return canPhase; }
    public boolean canResurrect() { return canResurrect; }
    public boolean canSummon() { return canSummon; }
    public double getSpeed() { return speed; }
    public double getAttackRange() { return attackRange; }
}
