package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Wraith extends Enemy {
    public Wraith(int waveLevel) {
        super(EnemyType.WRAITH, waveLevel);
        setName("Wraith");
    }

    @Override
    public void specialAbility() {}
}
