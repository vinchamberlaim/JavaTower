package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Revenant extends Enemy {
    private boolean resurrected = false;

    public Revenant(int waveLevel) {
        super(EnemyType.REVENANT, waveLevel);
        setName("Revenant");
    }

    @Override
    public void update(double dt, Hero hero) {
        if (!isAlive() && !resurrected) {
            // Resurrect once at half HP
            setCurrentHealth(getMaxHealth() / 2);
            setAlive(true);
            resurrected = true;
        }
        super.update(dt, hero);
    }

    @Override
    public void specialAbility() {}
}
