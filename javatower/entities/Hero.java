package javatower.entities;

import javatower.systems.Inventory;
import javatower.systems.SkillTree;
import javatower.systems.SkillProgression;
import javatower.systems.SetBonusManager;
import javatower.entities.Item;
import javatower.entities.Item.WeaponClass;
import javatower.entities.Item.EquipmentSet;
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
    private double regenTimer = 0;
    private double targetX, targetY;
    private boolean moving = false;

    private Inventory inventory;
    private SkillTree combatTree, magicTree, utilityTree;
    private SkillProgression skillProgression;

    public Hero(String name) {
        setName(name);
        setMaxHealth(100);
        setCurrentHealth(100);
        setAttack(10);
        setDefence(5);
        setAlive(true);
        setRadius(Constants.HERO_RADIUS);
        inventory = new Inventory(3, 3);
        skillProgression = new SkillProgression();
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

        // Passive HP regen from Holy 4pc set bonus
        regenTimer += dt;
        if (regenTimer >= 1.0) {
            regenTimer -= 1.0;
            int regen = SetBonusManager.getHolyPassiveRegen(getEquippedItems());
            if (regen > 0 && getCurrentHealth() < getMaxHealth()) {
                setCurrentHealth(Math.min(getMaxHealth(), getCurrentHealth() + regen));
            }
            // Death 4pc mana regen
            int manaRegen = SetBonusManager.getDeathManaRegen(getEquippedItems());
            if (manaRegen > 0 && mana < maxMana) {
                mana = Math.min(maxMana, mana + manaRegen);
            }
        }

        // Auto-attack nearest enemy in range (melee or ranged depending on weapon)
        if (enemies != null && attackTimer >= getEffectiveCooldown()) {
            double effectiveRange = getEffectiveRange();
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
            if (nearest != null && minDist <= effectiveRange + nearest.getRadius()) {
                attackEnemy(nearest);
                attackTimer = 0;

                // Fire 4pc: AoE splash — nearby enemies take 30% of damage dealt
                double splashFrac = SetBonusManager.getFireSplashFraction(getEquippedItems());
                if (splashFrac > 0 && lastDamageDealt > 0) {
                    int splashDmg = (int)(lastDamageDealt * splashFrac);
                    if (splashDmg > 0) {
                        for (Enemy splash : enemies) {
                            if (splash != nearest && splash.isAlive()
                                    && distanceTo(splash) <= effectiveRange + splash.getRadius() + 40) {
                                splash.takeDamage(splashDmg);
                            }
                        }
                    }
                }

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
        // Apply weapon class skill modifier
        WeaponClass wc = weapon != null ? weapon.getWeaponClass() : WeaponClass.MELEE;
        double skillMult = skillProgression.getDamageMultiplier(wc);
        baseDamage = (int)(baseDamage * skillMult);

        // Set bonuses: Fire 2pc +25% damage, Holy 4pc +50% vs undead (all enemies are undead)
        Item[] eq = getEquippedItems();
        baseDamage = (int)(baseDamage * SetBonusManager.getFireDamageBonus(eq));
        baseDamage = (int)(baseDamage * SetBonusManager.getHolyUndeadDamageBonus(eq));

        boolean crit = (Math.random() * 100) < critChance;
        int damage = crit ? (int)(baseDamage * 1.5) : baseDamage;
        int dealt = target.takeDamage(damage);
        lastDamageDealt = dealt;

        // Death 4pc: life steal
        double lifeSteal = SetBonusManager.getDeathLifeSteal(eq);
        if (lifeSteal > 0) {
            int healAmount = (int)(dealt * lifeSteal);
            if (healAmount > 0) {
                setCurrentHealth(Math.min(getMaxHealth(), getCurrentHealth() + healAmount));
            }
        }

        // Train weapon skill — 1 XP per hit
        skillProgression.addXP(wc, 1.0);

        return dealt;
    }

    /** Last damage dealt (for Fire 4pc AoE splash calculation in GameGUI). */
    private int lastDamageDealt;
    public int getLastDamageDealt() { return lastDamageDealt; }

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
     * Applies damage to the hero, reduced by defence + Defence skill bonus. Trains Defence skill.
     */
    @Override
    public int takeDamage(int damage) {
        // Defence skill gives extra flat reduction
        int totalDef = getDefence() + skillProgression.getDefenceBonus();
        // Knight 2pc: +25% defence
        totalDef = (int)(totalDef * SetBonusManager.getKnightDefenceBonus(getEquippedItems()));
        int reduced = Math.max(1, damage - totalDef);
        setCurrentHealth(getCurrentHealth() - reduced);
        if (getCurrentHealth() <= 0) {
            setCurrentHealth(0);
            setAlive(false);
            onDeath();
        }
        // Train defence skill when taking hits (0.5 XP per hit taken)
        if (getEquippedOfClass(WeaponClass.DEFENCE) != null) {
            skillProgression.addXP(WeaponClass.DEFENCE, 0.5);
        }
        return reduced;
    }

    /**
     * Heals the hero, boosted by Holy skill. Trains Holy skill.
     */
    @Override
    public void heal(int amount) {
        if (!isAlive()) return;
        double holyMult = skillProgression.getHolyHealBonus();
        // Holy 2pc set bonus: +20% heal
        holyMult *= SetBonusManager.getHolyHealBonus(getEquippedItems());
        int boostedAmount = (int)(amount * holyMult);
        setCurrentHealth(Math.min(getMaxHealth(), getCurrentHealth() + boostedAmount));
        skillProgression.addXP(WeaponClass.HOLY, 1.0);
    }

    /**
     * Returns the first equipped item of a given weapon class, or null.
     */
    public Item getEquippedOfClass(WeaponClass wc) {
        Item[] equipped = {weapon, offhand, helmet, chest, legs, boots, accessory1, accessory2};
        for (Item item : equipped) {
            if (item != null && item.getWeaponClass() == wc) return item;
        }
        return null;
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
    public SkillProgression getSkillProgression() { return skillProgression; }

    /** Returns all 8 equipment slots as an array (some may be null). */
    public Item[] getEquippedItems() {
        return new Item[] { weapon, offhand, helmet, chest, legs, boots, accessory1, accessory2 };
    }

    /** Effective attack range — melee base, plus ranged weapon range stat and ranged skill bonus. */
    public double getEffectiveRange() {
        double range = Constants.MELEE_RANGE;
        if (weapon != null) {
            Integer rangeBonus = weapon.getStatBonuses().get("range");
            if (rangeBonus != null) range += rangeBonus;
        }
        if (offhand != null) {
            Integer rangeBonus = offhand.getStatBonuses().get("range");
            if (rangeBonus != null) range += rangeBonus;
        }
        range += skillProgression.getRangedRangeBonus();
        return range;
    }

    /** Effective attack cooldown — base minus melee skill bonus and Knight 4pc bonus. */
    public double getEffectiveCooldown() {
        double cd = attackCooldown;
        cd -= skillProgression.getMeleeSpeedBonus();
        cd -= SetBonusManager.getKnightSpeedBonus(getEquippedItems());
        return Math.max(0.15, cd); // minimum 0.15s
    }
    public SkillTree getCombatTree() { return combatTree; }
    public SkillTree getMagicTree() { return magicTree; }
    public SkillTree getUtilityTree() { return utilityTree; }
    // Equipment getters omitted for brevity
}
