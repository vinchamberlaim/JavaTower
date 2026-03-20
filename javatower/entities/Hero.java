package javatower.entities;

import javatower.systems.Inventory;
import javatower.systems.SkillTree;
import javatower.entities.Item;
import javatower.util.Constants;
import java.util.List;

/**
 * Represents the player-controlled hero character.
 */
public class Hero extends Entity {
    private Item weapon, offhand, helmet, chest, legs, boots, accessory1, accessory2;

    private int level = 1;
    private int experience = 0;
    private int experienceToNextLevel = 100;
    private int gold = 50;
    private int mana = 50;
    private int maxMana = 50;
    private int critChance = 5;
    private int skillPoints = 0;

    private double moveSpeed = Constants.HERO_SPEED;
    private double attackCooldown = 0.6;
    private double attackTimer = 0;
    private double targetX, targetY;
    private boolean moving = false;

    private Inventory inventory;
    private SkillTree combatTree, magicTree, utilityTree;

    public Hero(String name) {
        setName(name);
        setMaxHealth(100);
        setCurrentHealth(100);
        setAttack(10);
        setDefence(5);
        setAlive(true);
        setRadius(Constants.HERO_RADIUS);
        inventory = new Inventory(3, 3);
    }

    @Override
    public void takeTurn() {}

    /**
     * Real-time update: move toward click target, auto-attack nearest enemy.
     */
    public void update(double dt, List<Enemy> enemies) {
        if (!isAlive()) return;
        attackTimer += dt;

        // Smooth movement toward click target
        if (moving) {
            double dx = targetX - getX();
            double dy = targetY - getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 3) {
                moving = false;
            } else {
                double step = moveSpeed * dt;
                if (step > dist) step = dist;
                double nx = dx / dist;
                double ny = dy / dist;
                double newX = Math.max(getRadius(), Math.min(Constants.SCREEN_WIDTH - getRadius(), getX() + nx * step));
                double newY = Math.max(getRadius(), Math.min(Constants.SCREEN_HEIGHT - getRadius(), getY() + ny * step));
                setPosition(newX, newY);
            }
        }

        // Auto-attack nearest enemy in melee range
        if (enemies != null && attackTimer >= attackCooldown) {
            Enemy nearest = null;
            double minDist = Double.MAX_VALUE;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                double d = distanceTo(e);
                if (d < minDist) {
                    minDist = d;
                    nearest = e;
                }
            }
            if (nearest != null && minDist <= Constants.MELEE_RANGE + nearest.getRadius()) {
                attackEnemy(nearest);
                attackTimer = 0;
                if (!nearest.isAlive()) {
                    gainExperience(nearest.getExperienceValue());
                    gainGold(nearest.getGoldValue());
                }
            }
        }
    }

    /**
     * Set a movement target (from mouse click).
     */
    public void moveTo(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.moving = true;
    }

    public int attackEnemy(Enemy target) {
        int baseDamage = getAttack();
        boolean crit = (Math.random() * 100) < critChance;
        int damage = crit ? (int)(baseDamage * 1.5) : baseDamage;
        return target.takeDamage(damage);
    }

    /**
     * Gain experience and check for level up.
     * @param amount XP gained
     */
    public void gainExperience(int amount) {
        experience += amount;
        while (experience >= experienceToNextLevel) {
            experience -= experienceToNextLevel;
            levelUp();
        }
    }

    /**
     * Level up the hero, increasing stats and skill points.
     */
    public void levelUp() {
        setMaxHealth(getMaxHealth() + 10);
        setCurrentHealth(getMaxHealth());
        setAttack(getAttack() + 2);
        setDefence(getDefence() + 1);
        maxMana += 5;
        mana = maxMana;
        skillPoints++;
        level++;
        experienceToNextLevel = (int)(experienceToNextLevel * 1.2);
    }

    /**
     * Gain gold.
     * @param amount Amount to gain
     */
    public void gainGold(int amount) {
        gold += amount;
    }

    /**
     * Spend gold if enough is available.
     * @param amount Amount to spend
     * @return True if successful
     */
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    /**
     * Equip an item, returning the previously equipped item in that slot.
     * @param item Item to equip
     * @return Previously equipped item, or null
     */
    public Item equipItem(Item item) {
        if (item == null) return null;
        Item previous = null;
        switch (item.getSlot()) {
            case WEAPON:
                previous = weapon;
                weapon = item;
                break;
            case OFFHAND:
                previous = offhand;
                offhand = item;
                break;
            case HELMET:
                previous = helmet;
                helmet = item;
                break;
            case CHEST:
                previous = chest;
                chest = item;
                break;
            case LEGS:
                previous = legs;
                legs = item;
                break;
            case BOOTS:
                previous = boots;
                boots = item;
                break;
            case ACCESSORY:
                if (accessory1 == null) {
                    accessory1 = item;
                } else {
                    previous = accessory2;
                    accessory2 = item;
                }
                break;
            default:
                break;
        }
        return previous;
    }

    /**
     * Use mana if enough is available.
     * @param cost Mana cost
     * @return True if successful
     */
    public boolean useMana(int cost) {
        if (mana >= cost) {
            mana -= cost;
            return true;
        }
        return false;
    }


    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public int getGold() { return gold; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public int getCritChance() { return critChance; }
    public void setCritChance(int critChance) { this.critChance = critChance; }
    public double getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(double speed) { this.moveSpeed = speed; }
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }
    public Inventory getInventory() { return inventory; }
    public SkillTree getCombatTree() { return combatTree; }
    public SkillTree getMagicTree() { return magicTree; }
    public SkillTree getUtilityTree() { return utilityTree; }
    // Equipment getters omitted for brevity
}
