package javatower.entities.towers;

import javatower.entities.Tower;
import javatower.entities.Enemy;
import javatower.entities.Hero;
import java.util.List;

/**
 * Support Tower: heals the hero and buffs nearby towers within range.
 */
public class SupportTower extends Tower {
    private int healAmount = 3;
    private int damageBoost = 2;

    public SupportTower(int x, int y) {
        super(TowerType.SUPPORT, 2, 0, 2, 80);
        setPosition(x, y);
        setName("Support Tower");
    }

    @Override
    public void attack(List<Enemy> enemies) {
        // Support tower does not attack enemies directly
    }

    @Override
    public Enemy selectTarget(List<Enemy> enemies) {
        return null;
    }

    /**
     * Heals the hero if within range. Called each cycle from GameGUI.
     */
    public void supportHero(Hero hero) {
        if (hero == null || !hero.isAlive()) return;
        if (distanceTo(hero) <= getRangePixels()) {
            if (hero.getCurrentHealth() < hero.getEffectiveMaxHealth()) {
                hero.setCurrentHealth(Math.min(hero.getEffectiveMaxHealth(),
                        hero.getCurrentHealth() + healAmount));
            }
        }
    }

    /**
     * Buffs nearby towers — temporarily boosts their damage.
     * Returns the bonus damage to add for towers in range.
     */
    public int getDamageBoost() {
        return damageBoost;
    }

    /**
     * Checks whether the given tower is within this support tower's aura range.
     */
    public boolean isInAuraRange(Tower other) {
        return other != this && distanceTo(other) <= getRangePixels();
    }

    @Override
    public void upgrade() {
        if (getUpgradeLevel() < 3) {
            setUpgradeLevel(getUpgradeLevel() + 1);
            setRange(getRange() + 1);
            healAmount += 2;
            damageBoost += 1;
            setUpgradeCost(getUpgradeCost() + 80);
        }
    }

    public int getHealAmount() { return healAmount; }
}
