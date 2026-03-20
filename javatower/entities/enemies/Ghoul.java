package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Ghoul extends Enemy {
    public Ghoul(int waveLevel) {
        super(EnemyType.GHOUL, waveLevel);
        setName("Ghoul");
    }

    @Override
    public void specialAbility() {}
}
