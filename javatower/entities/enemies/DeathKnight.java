package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class DeathKnight extends Enemy {
    public DeathKnight(int waveLevel) {
        super(EnemyType.DEATH_KNIGHT, waveLevel);
        setName("Death Knight");
    }

    @Override
    public void specialAbility() {}
}
