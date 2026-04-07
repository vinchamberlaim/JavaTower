package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;
import javatower.entities.BonePile;
import javatower.util.Constants;
import java.util.List;
import java.util.ArrayList;

/**
 * Lich — Tier-8 ranged summoner mini-boss.
 * <p>
 * AI behaviour:
 * <ul>
 *   <li><b>Kiting</b> — maintains preferred distance (~200 px); flees if hero
 *       closes within 100 px, strafes perpendicular at preferred range.</li>
 *   <li><b>Ranged attack</b> — fires every 1.8 s when hero is within attack range.</li>
 *   <li><b>Bone-pile AI</b> — actively seeks nearby bone piles when summon
 *       cooldown is nearly ready; moves toward them for mass resurrection.</li>
 *   <li><b>Mass Resurrection</b> — if ≥ 2 bone piles within 400 – 500 px,
 *       converts <em>all</em> of them into an undead horde (2–4 minions per pile).</li>
 *   <li><b>Single summon</b> — fallback if only 1 bone pile is nearby.</li>
 * </ul>
 * </p>
 * <p>
 * <b>CIS096 relevance:</b> concrete polymorphic specialization of {@link Enemy}.
 * The class overrides base AI with unique pathing + summoning logic while still
 * reusing shared combat, stat-scaling, and elite-modifier behaviour.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see Enemy
 * @see BonePile
 */
public class Lich extends Enemy {
    /** Seconds between summon attempts. */
    private double summonCooldown = 10.0;
    /** Accumulator for summon cooldown. */
    private double summonTimer = 0;
    /** Secondary attack timer for ranged attacks (separate from base). */
    private double attackTimer2 = 0;
    /** Ideal distance the Lich tries to maintain from the hero.  */
    private static final double PREFERRED_DIST = 200;
    /** Distance threshold below which the Lich will flee from the hero. */
    private static final double FLEE_DIST = 100;
    /** Maximum distance the Lich will travel to reach a bone pile. */
    private static final double BONE_SEEK_RANGE = 350;
    /** Maximum undead a single Lich can summon per wave (prevents crash from thousands of enemies). */
    private static final int MAX_SUMMONS_PER_LICH = 12;
    /** Counter for how many undead this Lich has summoned this wave. */
    private int summonCount = 0;

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
                // Try MASS RESURRECTION if ready and close enough
                if (summonTimer >= summonCooldown && bpDist <= 300) {
                    int nearbyBones = countNearbyBonePiles(400);
                    if (nearbyBones >= 2) {
                        // Raise UNDEAD HORDE!
                        int raised = massResurrection(400);
                        if (raised > 0) {
                            summonTimer = 0;
                        }
                    } else if (summonFromBones(250) != null) {
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
                    fleeX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, fleeX));
                    fleeY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, fleeY));
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
                    strafeX = Math.max(r, Math.min(Constants.WORLD_WIDTH - r, strafeX));
                    strafeY = Math.max(r, Math.min(Constants.WORLD_HEIGHT - r, strafeY));
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
            // Try mass resurrection if ANY bones nearby, otherwise normal summon
            int nearbyBones = countNearbyBonePiles(500); // Increased range
            if (nearbyBones >= 2) { // Lower threshold - just 2 bones triggers horde!
                // MASS RESURRECTION - turn ALL nearby bones to UNDEAD HORDE!
                int raised = massResurrection(500);
                if (raised > 0) {
                    summonTimer = 0;
                }
            } else if (summonFromBones(250) != null) {
                summonTimer = 0;
            } else {
                // No usable bones nearby: still summon directly so Lich remains active.
                int raised = summonDirectMinions(hero, 1 + (int)(Math.random() * 2));
                if (raised > 0) {
                    summonTimer = 0;
                }
            }
        }
    }

    /**
     * Fallback summon that does not require bone piles.
     * Keeps Lich gameplay interesting on maps/runs with low corpse density.
     */
    private int summonDirectMinions(Hero hero, int desiredCount) {
        if (getSiblings() == null || hero == null) return 0;
        if (summonCount >= MAX_SUMMONS_PER_LICH) return 0;

        int maxAllowed = Math.min(desiredCount, MAX_SUMMONS_PER_LICH - summonCount);
        int raised = 0;
        for (int i = 0; i < maxAllowed; i++) {
            EnemyType spawnType = Math.random() < 0.65 ? EnemyType.SKELETON : EnemyType.ZOMBIE;
            Enemy undead = javatower.factories.EnemyFactory.createEnemy(spawnType, getWaveLevel());

            // Spawn between hero and lich, with random spread.
            double midX = (getX() + hero.getX()) * 0.5;
            double midY = (getY() + hero.getY()) * 0.5;
            double angle = Math.random() * Math.PI * 2;
            double dist = 20 + Math.random() * 60;
            undead.setPosition(midX + Math.cos(angle) * dist, midY + Math.sin(angle) * dist);

            undead.setSiblings(getSiblings());
            undead.setBonePiles(getBonePiles());
            undead.setSummoned(true);
            getSiblings().add(undead);
            summonCount++;
            raised++;
        }
        return raised;
    }
    
    /** Count bone piles within range for mass resurrection decision. */
    private int countNearbyBonePiles(double range) {
        if (getBonePiles() == null) return 0;
        int count = 0;
        for (BonePile bp : getBonePiles()) {
            if (bp.isEmpty()) continue;
            double dx = getX() - bp.getX();
            double dy = getY() - bp.getY();
            if (Math.sqrt(dx * dx + dy * dy) <= range) count++;
        }
        return count;
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
    
    /**
     * MASS RESURRECTION: Converts ALL bone piles within range to UNDEAD HORDE!
     * This is the Lich's ultimate ability - raises an army from all nearby bones!
     * Capped at MAX_SUMMONS_PER_LICH to prevent performance issues.
     * @param range Maximum distance to consume bone piles
     * @return Number of undead raised
     */
    public int massResurrection(double range) {
        if (getBonePiles() == null || getSiblings() == null) return 0;
        
        // Check if this Lich has hit its summon cap
        if (summonCount >= MAX_SUMMONS_PER_LICH) return 0;
        
        int undeadRaised = 0;
        List<BonePile> pilesToConsume = new ArrayList<>();
        
        // Find ALL bone piles within range (increased range for horde effect)
        for (BonePile bp : getBonePiles()) {
            if (bp.isEmpty()) continue;
            double dx = getX() - bp.getX();
            double dy = getY() - bp.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= range) {
                pilesToConsume.add(bp);
            }
        }
        
        // Consume piles and spawn horde (capped)
        for (BonePile bp : pilesToConsume) {
            if (summonCount >= MAX_SUMMONS_PER_LICH) break; // Hit cap, stop summoning
            
            int tier = bp.consume(bp.getBoneCount());
            
            // HORDE SIZE: More undead per pile based on tier (but capped)
            // Tier 1-2: 2 undead | Tier 3-4: 3 undead | Tier 5+: 4 undead
            int hordeSize = Math.min(2 + (tier / 2), MAX_SUMMONS_PER_LICH - summonCount);
            
            for (int i = 0; i < hordeSize; i++) {
                // MIX: 70% Skeletons, 30% Zombies for variety
                EnemyType spawnType = (Math.random() < 0.7) ? EnemyType.SKELETON : EnemyType.ZOMBIE;
                Enemy undead = javatower.factories.EnemyFactory.createEnemy(spawnType, getWaveLevel());
                
                // WIDER spread for horde effect (50-100px radius)
                double angle = Math.random() * Math.PI * 2;
                double distance = 30 + Math.random() * 70;
                double offsetX = Math.cos(angle) * distance;
                double offsetY = Math.sin(angle) * distance;
                
                undead.setPosition(bp.getX() + offsetX, bp.getY() + offsetY);
                undead.setSiblings(getSiblings());
                undead.setBonePiles(getBonePiles());
                undead.setSummoned(true); // Mark as summoned, not part of wave count
                getSiblings().add(undead);
                undeadRaised++;
                summonCount++;
            }
        }
        
        // Remove empty piles
        getBonePiles().removeIf(BonePile::isEmpty);
        
        // Log the horde size
        if (undeadRaised > 0) {
            System.out.println("[Lich] ☠️ MASS RESURRECTION! Raised UNDEAD HORDE of " + undeadRaised + " creatures!");
        }
        
        return undeadRaised;
    }
}
