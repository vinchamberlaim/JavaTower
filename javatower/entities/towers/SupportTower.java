package javatower.entities.towers;

import javatower.entities.Tower;
import javatower.entities.Enemy;
import java.util.List;

/**
 * Support Tower: buffs nearby towers/hero.
 */
public class SupportTower extends Tower {
    public SupportTower(int x, int y) {
        super(TowerType.SUPPORT, 2, 0, 2, 80);
        setPosition(x, y);
        setName("Support Tower");
    }

    @Override
    public void attack(List<Enemy> enemies) {
        // Support tower does not attack
    }

    @Override
    public Enemy selectTarget(List<Enemy> enemies) {
        return null;
    }

    @Override
    public void upgrade() {
        if (getUpgradeLevel() < 3) {
            setUpgradeLevel(getUpgradeLevel() + 1);
            setRange(getRange() + 1);
            setUpgradeCost(getUpgradeCost() + 80);
        }
    }
}
