package javatower.entities.enemies;

import javatower.entities.Enemy;

/**
 * Massive Lovecraftian horror for late waves.
 */
public class Shoggoth extends Enemy {
    public Shoggoth(int waveLevel) {
        super(EnemyType.SHOGGOTH, waveLevel);
        setName("Shoggoth");
    }

    @Override
    public void specialAbility() {}
}
