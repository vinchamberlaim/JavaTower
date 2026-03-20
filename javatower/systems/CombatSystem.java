package javatower.systems;

import java.util.List;
import javatower.entities.Hero;
import javatower.entities.Enemy;
import javatower.entities.Tower;

/**
 * Retained for compatibility. Combat is now handled in real-time by
 * Hero.update(), Enemy.update(), and Tower.update().
 */
public class CombatSystem {
    private Hero hero;
    private List<Enemy> enemies;
    private List<Tower> towers;

    public CombatSystem(Hero hero, List<Enemy> enemies, List<Tower> towers) {
        this.hero = hero;
        this.enemies = enemies;
        this.towers = towers;
    }

    public boolean isCombatOver() {
        return hero.getCurrentHealth() <= 0 || enemies.isEmpty();
    }

    public List<Enemy> getEnemies() { return enemies; }
    public List<Tower> getTowers() { return towers; }
}
