package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;
import javatower.util.Constants;

/**
 * Necromancer King — final boss with a dual-weapon system.
 *
 * Weapons:
 *   Sword (melee): no min range, maxRange = 50px, damage = 1.5x base ATK
 *   Bow   (ranged): minRange = 80px, maxRange = 260px, damage = 0.7x base ATK
 *
 * Dead zone (50-80px): neither weapon is effective — king repositions.
 * If hero is in sword range → heavy melee hit.
 * If hero is between bow min/max → ranged chip damage.
 * King prefers to kite at bow distance, strafing to stay evasive.
 * Also summons minions from nearby bone piles.
 */
public class NecromancerKing extends Enemy {

    // ==================== Weapon: Sword (melee) ====================
    /** Whether the Necromancer King currently has a sword equipped. */
    private boolean hasSword = true;
    /** Sword minimum effective range (0 = point-blank). */
    private static final double SWORD_MIN_RANGE = 0;
    /** Sword maximum effective range in pixels. */
    private static final double SWORD_MAX_RANGE = 50;
    /** Sword damage multiplier applied to base ATK. */
    private static final double SWORD_DAMAGE_MULT = 1.5;
    /** Seconds between consecutive sword swings. */
    private static final double SWORD_COOLDOWN = 1.0;

    // ==================== Weapon: Bow (ranged) ====================
    /** Whether the Necromancer King currently has a bow equipped. */
    private boolean hasBow = true;
    /** Bow minimum effective range — cannot fire at closer targets. */
    private static final double BOW_MIN_RANGE = 80;
    /** Bow maximum effective range in pixels. */
    private static final double BOW_MAX_RANGE = 260;
    /** Bow damage multiplier applied to base ATK. */
    private static final double BOW_DAMAGE_MULT = 0.7;
    /** Seconds between consecutive bow shots. */
    private static final double BOW_COOLDOWN = 1.8;

    // ==================== AI Movement ====================
    /** Distance the King prefers to maintain (mid-bow range). */
    private static final double PREFERRED_DIST = 180;
    /** Distance at which the King will flee away from the hero. */
    private static final double FLEE_DIST = SWORD_MAX_RANGE + 10;

    // ==================== Timers ====================
    /** Accumulator for sword attack cooldown. */
    private double swordTimer = 0;
    /** Accumulator for bow attack cooldown. */
    private double bowTimer = 0;
    /** Accumulator for minion-summon cooldown. */
    private double summonTimer = 0;
    /** Seconds between summon attempts. */
    private static final double SUMMON_COOLDOWN = 8.0;

    /** Cached base attack value for applying weapon multipliers. */
    private int baseAtk;

    public NecromancerKing(int waveLevel) {
        super(EnemyType.NECROMANCER_KING, waveLevel);
        setName("Necromancer King");
        baseAtk = getAttack();
    }

    /** Equip or remove sword. */
    public void setSword(boolean has) { this.hasSword = has; }
    /** Equip or remove bow. */
    public void setBow(boolean has) { this.hasBow = has; }
    public boolean hasSword() { return hasSword; }
    public boolean hasBow() { return hasBow; }

    @Override
    public void update(double dt, Hero hero) {
        if (!isAlive() || hero == null) return;

        // Enrage below 25% HP
        if (getHealthPercent() < 25) {
            baseAtk = getType().atk * 2;
        }

        swordTimer += dt;
        bowTimer += dt;
        summonTimer += dt;

        double dist = distanceTo(hero);

        // ── Movement AI ──────────────────────────────────────

        if (hasBow) {
            // Has bow → kite to stay in bow range
            if (dist < FLEE_DIST) {
                // Too close — flee to get back into bow range
                fleeFrom(hero, dt);
            } else if (dist > BOW_MAX_RANGE + 30) {
                // Too far — close in
                smoothMoveToward(hero.getX(), hero.getY(), dt);
            } else if (dist >= BOW_MIN_RANGE && dist <= BOW_MAX_RANGE) {
                // In bow sweet spot — strafe
                strafe(hero, dt);
            } else {
                // Dead zone (between sword max and bow min) — back away
                fleeFrom(hero, dt);
            }
        } else if (hasSword) {
            // Only sword → beeline toward hero
            if (dist > SWORD_MAX_RANGE) {
                smoothMoveToward(hero.getX(), hero.getY(), dt);
            }
            // In sword range → stand and fight (no movement)
        } else {
            // No weapons — just approach
            smoothMoveToward(hero.getX(), hero.getY(), dt);
        }

        // ── Attack ───────────────────────────────────────────

        // Sword: if in sword range and has sword
        if (hasSword && dist >= SWORD_MIN_RANGE && dist <= SWORD_MAX_RANGE
                && swordTimer >= SWORD_COOLDOWN) {
            int dmg = (int)(baseAtk * SWORD_DAMAGE_MULT);
            hero.takeDamage(dmg);
            swordTimer = 0;
            onAttack(hero);
        }
        // Bow: if in bow range and has bow
        else if (hasBow && dist >= BOW_MIN_RANGE && dist <= BOW_MAX_RANGE
                && bowTimer >= BOW_COOLDOWN) {
            int dmg = (int)(baseAtk * BOW_DAMAGE_MULT);
            hero.takeDamage(dmg);
            bowTimer = 0;
            onAttack(hero);
        }

        // ── Summon from bone piles ───────────────────────────
        if (summonTimer >= SUMMON_COOLDOWN) {
            if (summonFromBones(300) != null) {
                summonTimer = 0;
            }
        }
    }

    /** Flee directly away from the hero. */
    private void fleeFrom(Hero hero, double dt) {
        double dx = getX() - hero.getX();
        double dy = getY() - hero.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.1) return;
        double fleeX = getX() + (dx / len) * 300;
        double fleeY = getY() + (dy / len) * 300;
        double r = getRadius();
        fleeX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, fleeX));
        fleeY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, fleeY));
        smoothMoveToward(fleeX, fleeY, dt);
    }

    /** Strafe perpendicular to the hero at current distance. */
    private void strafe(Hero hero, double dt) {
        double dx = getX() - hero.getX();
        double dy = getY() - hero.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.1) return;
        double strafeX = getX() + (-dy / len) * 60;
        double strafeY = getY() + (dx / len) * 60;
        double r = getRadius();
        strafeX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, strafeX));
        strafeY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, strafeY));
        smoothMoveToward(strafeX, strafeY, dt);
    }

    @Override
    public void specialAbility() {}
}
