package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class Wight extends Enemy {
    public Wight(int waveLevel) {
        super(EnemyType.WIGHT, waveLevel);
        setName("Wight");
    }

    @Override
    protected void onAttack(Hero hero) {
        // Drain mana on hit
        hero.useMana(5);
    }

    @Override
    public void specialAbility() {}
}
