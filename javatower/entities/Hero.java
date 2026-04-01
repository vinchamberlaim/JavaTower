package javatower.entities;

import javatower.systems.Inventory;
import javatower.systems.SkillTree;
import javatower.systems.SkillProgression;
import javatower.systems.SetBonusManager;
import javatower.entities.Item;
import javatower.entities.Item.WeaponClass;
import javatower.entities.Item.EquipmentSet;
import javatower.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the player-controlled hero character.
 */
public class Hero extends Entity {
    private Item weapon, offhand, helmet, chest, legs, boots, gloves, amulet;
    private Item[] rings = new Item[10];

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

    // Arrow-key continuous movement
    private boolean moveUp, moveDown, moveLeft, moveRight;
    
    // Ultimate ability state
    private boolean ultimateActive = false;
    
    // Dodge/Roll mechanics
    private boolean isRolling = false;
    private double rollTimer = 0;
    private double rollDuration = 0.35; // 350ms roll
    private double rollCooldown = 1.5;  // 1.5s cooldown
    private double rollCooldownTimer = 0;
    private double rollSpeed = 3.0;     // 3x speed during roll
    private double rollDirectionX = 0;
    private double rollDirectionY = 0;

    // Kill stats
    private int totalKills = 0;
    private int totalDamageDealt = 0;
    private int totalGoldEarned = 0;
    private int totalXPEarned = 0;

    // Last attack result (for visual effect spawning)
    private int lastDamageDealt;
    private boolean lastAttackCrit;
    private WeaponClass lastAttackWeaponClass = WeaponClass.MELEE;
    private Enemy lastAttackTarget;
    private boolean attackedThisFrame;
    private boolean leveledUpThisFrame;

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
        initSkillTrees();
    }

    /**
     * Initializes the three skill trees with branching nodes.
     */
    private void initSkillTrees() {
        // --- COMBAT TREE ---
        combatTree = new SkillTree("Combat");
        combatTree.addNode(new javatower.systems.SkillNode("c1", "Sharpen", "+3 Attack", "combat", 1,
                null, Map.of("attack", 3), null));
        combatTree.addNode(new javatower.systems.SkillNode("c2", "Precision", "+5% Crit", "combat", 1,
                java.util.List.of("c1"), Map.of("critChance", 5), null));
        combatTree.addNode(new javatower.systems.SkillNode("c3", "Berserker", "+5 ATK, +10 HP", "combat", 2,
                java.util.List.of("c2"), Map.of("attack", 5, "maxHealth", 10), null));
        combatTree.addNode(new javatower.systems.SkillNode("c4", "Lethal Strike", "+8% Crit", "combat", 2,
                java.util.List.of("c2"), Map.of("critChance", 8), null));
        combatTree.addNode(new javatower.systems.SkillNode("c5", "Warlord", "+8 ATK, +5 DEF", "combat", 3,
                java.util.List.of("c3", "c4"), Map.of("attack", 8, "defence", 5), null));

        // --- MAGIC TREE ---
        magicTree = new SkillTree("Magic");
        magicTree.addNode(new javatower.systems.SkillNode("m1", "Arcane Mind", "+15 Mana", "magic", 1,
                null, Map.of("maxMana", 15), null));
        magicTree.addNode(new javatower.systems.SkillNode("m2", "Inner Light", "+20 HP", "magic", 1,
                java.util.List.of("m1"), Map.of("maxHealth", 20), null));
        magicTree.addNode(new javatower.systems.SkillNode("m3", "Mana Surge", "+25 Mana", "magic", 2,
                java.util.List.of("m1"), Map.of("maxMana", 25), null));
        magicTree.addNode(new javatower.systems.SkillNode("m4", "Healing Aura", "+30 HP", "magic", 2,
                java.util.List.of("m2"), Map.of("maxHealth", 30), null));
        magicTree.addNode(new javatower.systems.SkillNode("m5", "Archmage", "+20 Mana, +20 HP", "magic", 3,
                java.util.List.of("m3", "m4"), Map.of("maxMana", 20, "maxHealth", 20), null));

        // --- UTILITY TREE ---
        utilityTree = new SkillTree("Utility");
        utilityTree.addNode(new javatower.systems.SkillNode("u1", "Thick Skin", "+4 DEF", "utility", 1,
                null, Map.of("defence", 4), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u2", "Fleet Foot", "+15 Speed", "utility", 1,
                java.util.List.of("u1"), Map.of("speed", 15), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u3", "Iron Wall", "+6 DEF, +15 HP", "utility", 2,
                java.util.List.of("u1"), Map.of("defence", 6, "maxHealth", 15), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u4", "Nimble", "+10% Crit, +10 Speed", "utility", 2,
                java.util.List.of("u2"), Map.of("critChance", 10, "speed", 10), null));
        utilityTree.addNode(new javatower.systems.SkillNode("u5", "Survivor", "+8 DEF, +25 HP", "utility", 3,
                java.util.List.of("u3", "u4"), Map.of("defence", 8, "maxHealth", 25), null));
    }

    @Override
    public void takeTurn() {}

    /**
     * Real-time update: move toward click target, auto-attack nearest enemy.
     */
    public void update(double dt, List<Enemy> enemies) {
        if (!isAlive()) return;
        
        // Handle roll cooldown
        if (rollCooldownTimer > 0) {
            rollCooldownTimer -= dt;
        }
        
        // Handle active roll
        if (isRolling) {
            rollTimer -= dt;
            if (rollTimer <= 0) {
                isRolling = false;
                rollCooldownTimer = rollCooldown;
            } else {
                // Continue rolling in direction
                double step = moveSpeed * rollSpeed * dt;
                double newX = Math.max(getRadius(), Math.min(Constants.SCREEN_WIDTH - getRadius(), getX() + rollDirectionX * step));
                double newY = Math.max(getRadius(), Math.min(Constants.SCREEN_HEIGHT - getRadius(), getY() + rollDirectionY * step));
                setPosition(newX, newY);
            }
            return; // Skip normal movement/attacks while rolling
        }
        
        attackTimer += dt;

        // Smooth movement toward click target
        if (moving) {
            double dx = targetX - getX();
            double dy = targetY - getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 3) {
                moving = false;
            } else {
                double step = (moveSpeed + getEquipmentStat("moveSpeed")) * dt;
                if (step > dist) step = dist;
                double nx = dx / dist;
                double ny = dy / dist;
                double newX = Math.max(getRadius(), Math.min(Constants.SCREEN_WIDTH - getRadius(), getX() + nx * step));
                double newY = Math.max(getRadius(), Math.min(Constants.SCREEN_HEIGHT - getRadius(), getY() + ny * step));
                setPosition(newX, newY);
            }
        }

        // Arrow-key continuous movement (overrides click-to-move while held)
        if (moveUp || moveDown || moveLeft || moveRight) {
            moving = false; // cancel click-to-move
            double adx = 0, ady = 0;
            if (moveUp) ady -= 1;
            if (moveDown) ady += 1;
            if (moveLeft) adx -= 1;
            if (moveRight) adx += 1;
            double alen = Math.sqrt(adx * adx + ady * ady);
            if (alen > 0) {
                adx /= alen;
                ady /= alen;
                double step = (moveSpeed + getEquipmentStat("moveSpeed")) * dt;
                double newX = Math.max(getRadius(), Math.min(Constants.SCREEN_WIDTH - getRadius(), getX() + adx * step));
                double newY = Math.max(getRadius(), Math.min(Constants.SCREEN_HEIGHT - getRadius(), getY() + ady * step));
                setPosition(newX, newY);
            }
        }

        // Passive regen (1 tick per second)
        regenTimer += dt;
        if (regenTimer >= 1.0) {
            regenTimer -= 1.0;
            // Holy 4pc HP regen
            int regen = SetBonusManager.getHolyPassiveRegen(getEquippedItems());
            if (regen > 0 && getCurrentHealth() < getEffectiveMaxHealth()) {
                setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + regen));
            }
            // Base mana regen: 1/sec (Magic tree m3 skill adds +1 extra)
            int baseManaRegen = 1;
            // Death 4pc mana regen bonus
            int manaRegen = baseManaRegen + SetBonusManager.getDeathManaRegen(getEquippedItems());
            if (mana < getEffectiveMaxMana()) {
                mana = Math.min(getEffectiveMaxMana(), mana + manaRegen);
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
                lastAttackTarget = nearest;
                attackedThisFrame = true;
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
        int baseDamage = getEffectiveAttack();
        // Apply weapon class skill modifier
        WeaponClass wc = weapon != null ? weapon.getWeaponClass() : WeaponClass.MELEE;
        double skillMult = skillProgression.getDamageMultiplier(wc);
        baseDamage = (int)(baseDamage * skillMult);

        // Set bonuses: Fire 2pc +25% damage, Holy 4pc +50% vs undead (all enemies are undead)
        Item[] eq = getEquippedItems();
        baseDamage = (int)(baseDamage * SetBonusManager.getFireDamageBonus(eq));
        baseDamage = (int)(baseDamage * SetBonusManager.getHolyUndeadDamageBonus(eq));

        boolean crit = (Math.random() * 100) < getEffectiveCritChance();
        int damage = crit ? (int)(baseDamage * 1.5) : baseDamage;
        int dealt = target.takeDamage(damage);
        lastDamageDealt = dealt;
        lastAttackCrit = crit;
        lastAttackWeaponClass = wc;

        // Death 4pc: life steal
        double lifeSteal = SetBonusManager.getDeathLifeSteal(eq);
        if (lifeSteal > 0) {
            int healAmount = (int)(dealt * lifeSteal);
            if (healAmount > 0) {
                setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + healAmount));
            }
        }

        // Train weapon skill — 1 XP per hit
        skillProgression.addXP(wc, 1.0);

        return dealt;
    }

    public int getLastDamageDealt() { return lastDamageDealt; }
    public boolean wasAttackCrit() { return lastAttackCrit; }
    public WeaponClass getLastAttackWeaponClass() { return lastAttackWeaponClass; }
    public Enemy getLastAttackTarget() { return lastAttackTarget; }
    public boolean didAttackThisFrame() { return attackedThisFrame; }
    public void clearFrameFlags() { attackedThisFrame = false; leveledUpThisFrame = false; }
    public boolean didLevelUpThisFrame() { return leveledUpThisFrame; }

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
        setCurrentHealth(getEffectiveMaxHealth());
        setAttack(getAttack() + 2);
        setDefence(getDefence() + 1);
        maxMana += 5;
        mana = getEffectiveMaxMana();
        skillPoints++;
        level++;
        experienceToNextLevel = (int)(experienceToNextLevel * 1.2);
        leveledUpThisFrame = true;

        // Expand inventory every 3 levels
        if (level % 3 == 0) {
            int newW = inventory.getWidth() + 1;
            int newH = inventory.getHeight() + 1;
            inventory.expand(newW, newH);
        }
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
     * Two-handed weapons block the offhand slot.
     * Rings support up to 10 slots.
     */
    public Item equipItem(Item item) {
        if (item == null) return null;
        Item previous = null;
        switch (item.getSlot()) {
            case WEAPON:
                previous = weapon;
                weapon = item;
                // Two-handed weapon clears offhand
                if (item.isTwoHanded() && offhand != null) {
                    // Return offhand to inventory handled by caller
                    Item displacedOffhand = offhand;
                    offhand = null;
                    // Store displaced for caller — attach to previous if possible
                    if (previous == null) previous = displacedOffhand;
                }
                break;
            case OFFHAND:
                // Can't equip offhand if weapon is two-handed
                if (weapon != null && weapon.isTwoHanded()) return null;
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
            case GLOVES:
                previous = gloves;
                gloves = item;
                break;
            case AMULET:
                previous = amulet;
                amulet = item;
                break;
            case RING:
                // Find first empty ring slot (up to 10)
                for (int i = 0; i < rings.length; i++) {
                    if (rings[i] == null) {
                        rings[i] = item;
                        return null; // no previous
                    }
                }
                // All 10 full — replace last ring
                previous = rings[rings.length - 1];
                rings[rings.length - 1] = item;
                break;
            default:
                break;
        }
        return previous;
    }

    /**
     * Unequip item from a specific slot. Returns the removed item.
     * @param slotName one of: weapon, offhand, helmet, chest, legs, boots, gloves, amulet, ring0-ring9
     */
    public Item unequipSlot(String slotName) {
        Item removed = null;
        switch (slotName) {
            case "weapon": removed = weapon; weapon = null; break;
            case "offhand": removed = offhand; offhand = null; break;
            case "helmet": removed = helmet; helmet = null; break;
            case "chest": removed = chest; chest = null; break;
            case "legs": removed = legs; legs = null; break;
            case "boots": removed = boots; boots = null; break;
            case "gloves": removed = gloves; gloves = null; break;
            case "amulet": removed = amulet; amulet = null; break;
            default:
                if (slotName.startsWith("ring")) {
                    int idx = Integer.parseInt(slotName.substring(4));
                    if (idx >= 0 && idx < rings.length) {
                        removed = rings[idx];
                        rings[idx] = null;
                    }
                }
                break;
        }
        return removed;
    }

    /** Returns number of equipped rings. */
    public int getEquippedRingCount() {
        int count = 0;
        for (Item r : rings) if (r != null) count++;
        return count;
    }

    /**
     * Applies damage to the hero, reduced by defence + Defence skill bonus. Trains Defence skill.
     * During roll: INVINCIBLE (no damage taken).
     */
    @Override
    public int takeDamage(int damage) {
        // Invincible during roll
        if (isRolling) return 0;
        
        // Effective defence = base + items + skill bonus
        int totalDef = getEffectiveDefence() + skillProgression.getDefenceBonus();
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
        // Add flat heal power from equipment
        amount += getEquipmentHealBonus();
        double holyMult = skillProgression.getHolyHealBonus();
        // Holy 2pc set bonus: +20% heal
        holyMult *= SetBonusManager.getHolyHealBonus(getEquippedItems());
        int boostedAmount = (int)(amount * holyMult);
        setCurrentHealth(Math.min(getEffectiveMaxHealth(), getCurrentHealth() + boostedAmount));
        skillProgression.addXP(WeaponClass.HOLY, 1.0);
    }

    /**
     * Returns the first equipped item of a given weapon class, or null.
     */
    public Item getEquippedOfClass(WeaponClass wc) {
        for (Item item : getEquippedItems()) {
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

    // ========== Equipment-aware effective stats ==========

    /**
     * Sums a named stat across all equipped items.
     */
    public int getEquipmentStat(String stat) {
        int total = 0;
        for (Item item : getEquippedItems()) {
            if (item != null) {
                Integer val = item.getStatBonuses().get(stat);
                if (val != null) total += val;
            }
        }
        return total;
    }

    /** Base attack + all equipped item attack bonuses (+50% during ultimate). */
    public int getEffectiveAttack() {
        int attack = getAttack() + getEquipmentStat("attack");
        if (ultimateActive) attack = (int)(attack * 1.5);
        return attack;
    }

    /** Base defence + all equipped item defence bonuses (+50% during ultimate). */
    public int getEffectiveDefence() {
        int def = getDefence() + getEquipmentStat("defence");
        if (ultimateActive) def = (int)(def * 1.5);
        return def;
    }

    /** Base crit chance + all equipped item crit bonuses (+25% during ultimate). */
    public int getEffectiveCritChance() {
        int crit = critChance + getEquipmentStat("critChance");
        if (ultimateActive) crit += 25;
        return crit;
    }

    /** Base max mana + equipped mana bonuses. */
    public int getEffectiveMaxMana() {
        return maxMana + getEquipmentStat("mana");
    }

    /** Base max HP + equipped health bonuses. */
    public int getEffectiveMaxHealth() {
        return getMaxHealth() + getEquipmentStat("health");
    }

    /** Total heal power bonus from items (flat). */
    public int getEquipmentHealBonus() {
        return getEquipmentStat("heal");
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
    public void setLevel(int level) { this.level = level; }
    public void setGold(int gold) { this.gold = gold; }
    public void setMana(int mana) { this.mana = mana; }
    public void setExperience(int experience) { this.experience = experience; }
    public Inventory getInventory() { return inventory; }
    public SkillProgression getSkillProgression() { return skillProgression; }

    /** Returns all equipped items (non-null only). */
    public Item[] getEquippedItems() {
        List<Item> list = new ArrayList<>();
        Item[] fixed = { weapon, offhand, helmet, chest, legs, boots, gloves, amulet };
        for (Item i : fixed) if (i != null) list.add(i);
        for (Item r : rings) if (r != null) list.add(r);
        return list.toArray(new Item[0]);
    }

    // Equipment slot getters
    public Item getWeapon() { return weapon; }
    public Item getOffhand() { return offhand; }
    public Item getHelmet() { return helmet; }
    public Item getChest() { return chest; }
    public Item getLegs() { return legs; }
    public Item getBoots() { return boots; }
    public Item getGloves() { return gloves; }
    public Item getAmulet() { return amulet; }
    public Item[] getRings() { return rings; }

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

    /** Effective attack cooldown — base minus item speed bonus, melee skill, and Knight 4pc. */
    public double getEffectiveCooldown() {
        double cd = attackCooldown;
        // Item "speed" stat: each point = 0.02s faster
        cd -= getEquipmentStat("speed") * 0.02;
        cd -= skillProgression.getMeleeSpeedBonus();
        cd -= SetBonusManager.getKnightSpeedBonus(getEquippedItems());
        return Math.max(0.15, cd); // minimum 0.15s
    }
    public SkillTree getCombatTree() { return combatTree; }
    public SkillTree getMagicTree() { return magicTree; }
    public SkillTree getUtilityTree() { return utilityTree; }

    // Arrow-key movement setters
    public void setMoveUp(boolean v) { moveUp = v; }
    public void setMoveDown(boolean v) { moveDown = v; }
    public void setMoveLeft(boolean v) { moveLeft = v; }
    public void setMoveRight(boolean v) { moveRight = v; }

    // Kill stats
    public void recordKill(int damage, int gold, int xp) {
        totalKills++;
        totalDamageDealt += damage;
        totalGoldEarned += gold;
        totalXPEarned += xp;
    }
    public int getTotalKills() { return totalKills; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalGoldEarned() { return totalGoldEarned; }
    public int getTotalXPEarned() { return totalXPEarned; }
    
    // ========== Dodge/Roll Methods ==========
    
    /**
     * Activates a dodge/roll in the specified direction.
     * @param dirX X direction (-1 to 1)
     * @param dirY Y direction (-1 to 1)
     * @return true if roll was activated, false if on cooldown
     */
    public boolean roll(double dirX, double dirY) {
        if (isRolling || rollCooldownTimer > 0) return false;
        
        // Normalize direction
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len < 0.001) return false;
        
        rollDirectionX = dirX / len;
        rollDirectionY = dirY / len;
        isRolling = true;
        rollTimer = rollDuration;
        return true;
    }
    
    /**
     * Roll in current movement direction (for WASD controls).
     */
    public boolean rollInMovementDirection() {
        double dx = 0, dy = 0;
        if (moveUp) dy -= 1;
        if (moveDown) dy += 1;
        if (moveLeft) dx -= 1;
        if (moveRight) dx += 1;
        
        // If no movement keys, roll toward last click target
        if (dx == 0 && dy == 0 && moving) {
            dx = targetX - getX();
            dy = targetY - getY();
        }
        
        return roll(dx, dy);
    }
    
    /**
     * Instantly roll toward cursor position.
     */
    public boolean rollTo(double targetX, double targetY) {
        return roll(targetX - getX(), targetY - getY());
    }
    
    public boolean isRolling() { return isRolling; }
    public double getRollCooldownPercent() { 
        if (rollCooldownTimer <= 0) return 1.0;
        return 1.0 - (rollCooldownTimer / rollCooldown);
    }
    public double getRollDurationRemaining() { return rollTimer; }
    public boolean canRoll() { return !isRolling && rollCooldownTimer <= 0; }
    
    // Ultimate ability methods
    public void setUltimateActive(boolean active) { this.ultimateActive = active; }
    public boolean isUltimateActive() { return ultimateActive; }
    
    // Equipment getters omitted for brevity
}
