package javatower.entities.enemies;

import javatower.entities.Enemy;

/**
 * Eldritch cult caster.
 */
public class Cultist extends Enemy {
    public Cultist(int waveLevel) {
        super(EnemyType.CULTIST, waveLevel);
        setName("Cultist");
    }

    @Override
    public void specialAbility() {}
}
