package javatower.entities.enemies;

import javatower.entities.Enemy;
import javatower.entities.Hero;

public class BoneColossus extends Enemy {
    public BoneColossus(int waveLevel) {
        super(EnemyType.BONE_COLOSSUS, waveLevel);
        setName("Bone Colossus");
    }

    @Override
    public void specialAbility() {}
}
