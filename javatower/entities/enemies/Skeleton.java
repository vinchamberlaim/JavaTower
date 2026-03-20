package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Skeleton extends Enemy {
    public Skeleton(int waveLevel) {
        super(EnemyType.SKELETON, waveLevel);
        setName("Skeleton");
    }

    @Override
    public void specialAbility() {}
}
