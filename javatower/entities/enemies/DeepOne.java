package javatower.entities.enemies;

import javatower.entities.Enemy;

/**
 * Abyssal Deep One bruiser.
 */
public class DeepOne extends Enemy {
    public DeepOne(int waveLevel) {
        super(EnemyType.DEEP_ONE, waveLevel);
        setName("Deep One");
    }

    @Override
    public void specialAbility() {}
}
