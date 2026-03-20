package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;
import javatower.util.Constants;

public class Lich extends Enemy {
    private double summonCooldown = 10.0;
    private double summonTimer = 0;
    private double attackTimer2 = 0;
    private static final double PREFERRED_DIST = 200;
    private static final double FLEE_DIST = 100;

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

        // Ranged attack
        if (dist <= getAttackRange() && attackTimer2 >= 1.8) {
            hero.takeDamage(getAttack());
            attackTimer2 = 0;
            onAttack(hero);
        }

        // Summon from bone piles
        if (summonTimer >= summonCooldown) {
            if (summonFromBones(250) != null) {
                summonTimer = 0;
            }
        }
    }

    @Override
    public void specialAbility() {}
}
