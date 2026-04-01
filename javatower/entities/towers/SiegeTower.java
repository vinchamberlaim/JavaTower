package javatower.entities.towers;

import javatower.entities.Tower;
import javatower.entities.Enemy;
import java.util.List;

/**
 * Siege Tower: slow, high damage, AOE.
 */
public class SiegeTower extends Tower {
    public SiegeTower(int x, int y) {
        super(TowerType.SIEGE, 3, 30, 3, 100);
        setPosition(x, y);
        setName("Siege Tower");
    }

    @Override
    public void attack(List<Enemy> enemies) {
        double rangePx = getRangePixels();
        // AOE: damage all enemies in range
        for (Enemy e : enemies) {
            if (e.isAlive() && distanceTo(e) <= rangePx) {
                e.takeDamage(getDamage());
                recordDamage(getDamage());
                if (!e.isAlive()) recordKill();
            }
        }
    }

    @Override
    public Enemy selectTarget(List<Enemy> enemies) {
        return selectByMode(enemies);
    }

    @Override
    public void upgrade() {
        if (getUpgradeLevel() < 3) {
            setUpgradeLevel(getUpgradeLevel() + 1);
            setDamage(getDamage() + 15);
            if (getUpgradeLevel() == 3) setRange(getRange() + 1);
            setUpgradeCost(getUpgradeCost() + 100);
        }
    }
}
