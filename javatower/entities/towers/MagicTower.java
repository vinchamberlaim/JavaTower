package javatower.entities.towers;

import javatower.entities.Tower;
import javatower.entities.Enemy;
import java.util.List;

/**
 * Magic Tower: medium, can hit flying/phasing enemies.
 */
public class MagicTower extends Tower {
    public MagicTower(int x, int y) {
        super(TowerType.MAGIC, 5, 15, 2, 75);
        setPosition(x, y);
        setName("Magic Tower");
    }

    @Override
    public void attack(List<Enemy> enemies) {
        Enemy target = selectTarget(enemies);
        if (target != null) {
            target.takeDamage(getDamage());
            recordDamage(getDamage());
            if (!target.isAlive()) recordKill();
        }
    }

    @Override
    public Enemy selectTarget(List<Enemy> enemies) {
        double rangePx = getRangePixels();
        // Prioritize phasing enemies within range
        for (Enemy e : enemies) {
            if (e.isAlive() && e.canPhase() && distanceTo(e) <= rangePx) return e;
        }
        return selectByMode(enemies);
    }

    @Override
    public void upgrade() {
        if (getUpgradeLevel() < 3) {
            setUpgradeLevel(getUpgradeLevel() + 1);
            setDamage(getDamage() + 8);
            if (getUpgradeLevel() == 2) setRange(getRange() + 1);
            setUpgradeCost(getUpgradeCost() + 75);
        }
    }
}
