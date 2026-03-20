package javatower.factories;

import javatower.entities.Tower;
import javatower.entities.Tower.TowerType;

/**
 * Factory for creating towers.
 */
public class TowerFactory {
    public static Tower createTower(TowerType type, int x, int y) {
        switch (type) {
            case ARROW:
                return new javatower.entities.towers.ArrowTower(x, y);
            case MAGIC:
                return new javatower.entities.towers.MagicTower(x, y);
            case SIEGE:
                return new javatower.entities.towers.SiegeTower(x, y);
            case SUPPORT:
                return new javatower.entities.towers.SupportTower(x, y);
            default:
                return null;
        }
    }
}
