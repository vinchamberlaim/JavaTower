package javatower.entities.towers;

import javatower.entities.Tower;
import javatower.entities.Enemy;
import java.util.List;

/**
 * Arrow Tower: fast, single target, low damage.
 */
public class ArrowTower extends Tower {
    public ArrowTower(int x, int y) {
        super(TowerType.ARROW, 4, 10, 1, 50);
        setPosition(x, y);
        setName("Arrow Tower");
    }

    @Override
    public void attack(List<Enemy> enemies) {
        Enemy target = selectTarget(enemies);
        if (target != null) {
            int prevHp = target.getCurrentHealth();
            target.takeDamage(getDamage());
            recordDamage(getDamage());
            if (!target.isAlive()) recordKill();
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
            setDamage(getDamage() + 5);
            if (getUpgradeLevel() == 2) setRange(getRange() + 1);
            setUpgradeCost(getUpgradeCost() + 50);
        }
    }
}
