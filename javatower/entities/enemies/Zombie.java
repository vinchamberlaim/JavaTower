package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Zombie extends Enemy {
    public Zombie(int waveLevel) {
        super(EnemyType.ZOMBIE, waveLevel);
        setName("Zombie");
    }

    @Override
    public void specialAbility() {}
}
