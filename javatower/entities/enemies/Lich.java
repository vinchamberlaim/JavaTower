package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;
import javatower.entities.BonePile;
import javatower.util.Constants;
import java.util.List;

public class Lich extends Enemy {
    private double summonCooldown = 10.0;
    private double summonTimer = 0;
    private double attackTimer2 = 0;
    private static final double PREFERRED_DIST = 200;
    private static final double FLEE_DIST = 100;
    private static final double BONE_SEEK_RANGE = 350; // max distance to seek bone piles

    public Lich(int waveLevel) {
        super(EnemyType.LICH, waveLevel);
        setName("Lich");
    }

    @Override
    public void update(double dt, Hero hero) {
        if (!isAlive() || hero == null) return;

        attackTimer2 += dt;
        summonTimer += dt;

        double dist = distanceTo(hero);

        // (#26) Bone pile AI: When summon is ready, seek nearest bone pile to summon from
        boolean seekingBones = false;
        if (summonTimer >= summonCooldown * 0.7) {
            BonePile nearest = findNearestBonePile();
            if (nearest != null) {
                double bpDist = Math.sqrt(Math.pow(getX() - nearest.getX(), 2) + Math.pow(getY() - nearest.getY(), 2));
                if (bpDist > 200) {
                    // Move toward bone pile if not close enough to summon
                    smoothMoveToward(nearest.getX(), nearest.getY(), dt);
                    seekingBones = true;
                }
                // Try to summon if ready and close enough
                if (summonTimer >= summonCooldown && bpDist <= 250) {
                    if (summonFromBones(250) != null) {
                        summonTimer = 0;
                    }
                }
            }
        }

        if (!seekingBones) {
            // Flee if hero is too close
            if (dist < FLEE_DIST) {
                double dx = getX() - hero.getX();
                double dy = getY() - hero.getY();
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 0.1) {
                    double fleeX = getX() + (dx / len) * 250;
                    double fleeY = getY() + (dy / len) * 250;
                    double r = getRadius();
                    fleeX = Math.max(r, Math.min(Constants.SCREEN_WIDTH - r, fleeX));
                    fleeY = Math.max(r, Math.min(Constants.SCREEN_HEIGHT - r, fleeY));
                    smoothMoveToward(fleeX, fleeY, dt);
                }
            }
            // Close in if too far
            else if (dist > PREFERRED_DIST + 30) {
                smoothMoveToward(hero.getX(), hero.getY(), dt);
            }
            // Strafe at preferred distance
            else {
                double dx = getX() - hero.getX();
                double dy = getY() - hero.getY();
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 0.1) {
                    double strafeX = getX() + (-dy / len) * 50;
                    double strafeY = getY() + (dx / len) * 50;
                    double r = getRadius();
                    strafeX = Math.max(r, Math.min(Constants.SCREEN_WIDTH - r, strafeX));
                    strafeY = Math.max(r, Math.min(Constants.SCREEN_HEIGHT - r, strafeY));
                    smoothMoveToward(strafeX, strafeY, dt);
                }
            }
        }

        // Ranged attack
        if (dist <= getAttackRange() && attackTimer2 >= 1.8) {
            hero.takeDamage(getAttack());
            attackTimer2 = 0;
            onAttack(hero);
        }

        // Fallback summon attempt (if not handled above)
        if (summonTimer >= summonCooldown) {
            if (summonFromBones(250) != null) {
                summonTimer = 0;
            }
        }
    }

    /** Find the nearest non-empty bone pile within seek range. */
    private BonePile findNearestBonePile() {
        List<BonePile> piles = getBonePiles();
        if (piles == null) return null;
        BonePile nearest = null;
        double nearestDist = BONE_SEEK_RANGE;
        for (BonePile bp : piles) {
            if (bp.isEmpty()) continue;
            double dx = getX() - bp.getX();
            double dy = getY() - bp.getY();
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = bp;
            }
        }
        return nearest;
    }

    @Override
    public void specialAbility() {}
}
