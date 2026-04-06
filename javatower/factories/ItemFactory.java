package javatower.factories;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javatower.entities.Item;
import javatower.entities.Item.Rarity;
import javatower.entities.Item.EquipmentSet;

/**
 * Factory for creating and generating items.
 *
 * <p><b>CIS096 relevance:</b> concrete implementation of the Factory pattern.
 * Callers request "an item" and do not depend on concrete constructors,
 * reducing coupling and making it easy to extend with new drops/boss loot.
 * </p>
 *
 * <p>Normal items drop commonly; equipment-set pieces are rarer and used to
 * drive class archetypes (Paladin, Necromancer, Pyromancer, Warrior, Archer).</p>
 */
public class ItemFactory {
    private static final Random rand = new Random();

    /** Creates a random normal (non-set) item. */
    public static Item createRandomItem(int itemLevel, Rarity rarity) {
        int type = rand.nextInt(36);
        switch (type) {
            case 0: return Item.createSword(itemLevel, rarity);
            case 1: return Item.createShield(itemLevel, rarity);
            case 2: return Item.createHelmet(itemLevel, rarity);
            case 3: return Item.createChestArmor(itemLevel, rarity);
            case 4: return Item.createRing(itemLevel, rarity);
            case 5: return Item.createBow(itemLevel, rarity);
            case 6: return Item.createNecroStaff(itemLevel, rarity);
            case 7: return Item.createHolyMace(itemLevel, rarity);
            case 8: return Item.createBoots(itemLevel, rarity);
            case 9: return Item.createLeggings(itemLevel, rarity);
            case 10: return Item.createBuckler(itemLevel, rarity);
            case 11: return Item.createKiteShield(itemLevel, rarity);
            case 12: return Item.createGauntlets(itemLevel, rarity);
            case 13: return Item.createClothGloves(itemLevel, rarity);
            case 14: return Item.createLeatherGloves(itemLevel, rarity);
            case 15: return Item.createAmuletOfFortitude(itemLevel, rarity);
            case 16: return Item.createAmuletOfArcana(itemLevel, rarity);
            case 17: return Item.createAmuletOfTheWarrior(itemLevel, rarity);
            case 18: return Item.createRingOfPower(itemLevel, rarity);
            case 19: return Item.createRingOfProtection(itemLevel, rarity);
            case 20: return Item.createRingOfVitality(itemLevel, rarity);
            case 21: return Item.createMageRobes(itemLevel, rarity);
            case 22: return Item.createDagger(itemLevel, rarity);
            case 23: return Item.createWand(itemLevel, rarity);
            // New Necromancy items
            case 24: return Item.createBoneWand(itemLevel, rarity);
            case 25: return Item.createSkullOffhand(itemLevel, rarity);
            case 26: return Item.createNecroticAmulet(itemLevel, rarity);
            case 27: return Item.createGravewalkerBoots(itemLevel, rarity);
            // New Archer/Ranged items
            case 28: return Item.createLongbow(itemLevel, rarity);
            case 29: return Item.createQuiver(itemLevel, rarity);
            case 30: return Item.createHawkAmulet(itemLevel, rarity);
            case 31: return Item.createArcherGloves(itemLevel, rarity);
            // Additional variety
            case 32: return Item.createSpellbook(itemLevel, rarity);
            case 33: return Item.createCrystalOrb(itemLevel, rarity);
            case 34: return Item.createEnchantedRobes(itemLevel, rarity);
            case 35: return Item.createWizardHat(itemLevel, rarity);
            default: return Item.createSword(itemLevel, rarity);
        }
    }

    /** Creates a random set item piece. Set items start at RARE minimum. */
    public static Item createRandomSetItem(int itemLevel, Rarity rarity) {
        // Enforce minimum RARE rarity for set items
        if (rarity.ordinal() < Rarity.RARE.ordinal()) rarity = Rarity.RARE;

        EquipmentSet[] sets = { EquipmentSet.HOLY, EquipmentSet.DEATH, EquipmentSet.FIRE, EquipmentSet.KNIGHT, EquipmentSet.ARCHER };
        EquipmentSet set = sets[rand.nextInt(sets.length)];
        int piece = rand.nextInt(4); // weapon, offhand, helmet, chest

        switch (set) {
            case HOLY:
                switch (piece) {
                    case 0: return Item.createHolySword(itemLevel, rarity);
                    case 1: return Item.createBlessedShield(itemLevel, rarity);
                    case 2: return Item.createHolyHelm(itemLevel, rarity);
                    case 3: return Item.createHolyPlate(itemLevel, rarity);
                }
                break;
            case DEATH:
                switch (piece) {
                    case 0: return Item.createDeathStaff(itemLevel, rarity);
                    case 1: return Item.createGrimoire(itemLevel, rarity);
                    case 2: return Item.createDeathHood(itemLevel, rarity);
                    case 3: return Item.createDeathRobes(itemLevel, rarity);
                }
                break;
            case FIRE:
                switch (piece) {
                    case 0: return Item.createFireStaff(itemLevel, rarity);
                    case 1: return Item.createFlameOrb(itemLevel, rarity);
                    case 2: return Item.createFlameCrown(itemLevel, rarity);
                    case 3: return Item.createFlameRobes(itemLevel, rarity);
                }
                break;
            case KNIGHT:
                switch (piece) {
                    case 0: return Item.createSteelGreatsword(itemLevel, rarity);
                    case 1: return Item.createTowerShield(itemLevel, rarity);
                    case 2: return Item.createSteelHelm(itemLevel, rarity);
                    case 3: return Item.createSteelPlate(itemLevel, rarity);
                }
                break;
            case ARCHER:
                switch (piece) {
                    case 0: return Item.createRangerBow(itemLevel, rarity);
                    case 1: return Item.createRangerQuiver(itemLevel, rarity);
                    case 2: return Item.createRangerHood(itemLevel, rarity);
                    case 3: return Item.createRangerCloak(itemLevel, rarity);
                }
                break;
            default: break;
        }
        return Item.createSword(itemLevel, rarity); // fallback
    }

    /**
     * Creates an item drop from a defeated enemy, with tier-based drop chance.
     * Normal enemies: 8% drop chance. Mini-bosses (tier 5-8): 30%. Bosses (tier 9-10): 100%.
     * Boss drops are at least EPIC quality. Tier 10 drops LEGENDARY.
     * Returns null if no drop.
     */
    public static Item createItemDrop(javatower.entities.Enemy enemy) {
        int tier = enemy.getType().tier;
        int level = enemy.getWaveLevel();

        // Drop chance based on tier (increased for better loot flow)
        double dropChance;
        if (tier >= 9) dropChance = 1.0;       // bosses always drop
        else if (tier >= 5) dropChance = 0.45;  // mini-bosses
        else dropChance = 0.15;                  // normal enemies

        if (rand.nextDouble() > dropChance) return null;

        // Boss quality floor
        Rarity rarity;
        if (tier >= 10) {
            rarity = Rarity.LEGENDARY;
        } else if (tier >= 9) {
            rarity = Rarity.EPIC;
        } else {
            rarity = rollRarity();
        }

        // Unique boss drops (#44): bosses drop signature named items
        if (tier >= 9) {
            Item unique = createBossUnique(enemy.getType(), level);
            if (unique != null) return unique;
        }

        // 25% chance for a class/set item (increased from 12%)
        if (rand.nextInt(100) < 25) {
            return createRandomSetItem(level, rarity);
        }
        return createRandomItem(level, rarity);
    }

    /**
     * Generates shop stock. Always includes 1 guaranteed set item and
     * a mix of normal items and potions.
     */
    public static List<Item> generateShopStock(int waveLevel, int count) {
        List<Item> stock = new ArrayList<>();

        // 1 guaranteed set item (rare+)
        stock.add(createRandomSetItem(waveLevel, rollRarity()));

        // Fill the rest with normal items + potions
        for (int i = 1; i < count; i++) {
            if (rand.nextInt(5) == 0) {
                // 20% chance for a potion
                stock.add(rand.nextBoolean() ? Item.createHealthPotion() : Item.createManaPotion());
            } else if (rand.nextInt(100) < 20) {
                // 20% chance for another set item (increased from 10%)
                stock.add(createRandomSetItem(waveLevel, rollRarity()));
            } else {
                stock.add(createRandomItem(waveLevel, rollRarity()));
            }
        }
        return stock;
    }

    /** Rolls a random rarity with weighted distribution. */
    private static Rarity rollRarity() {
        int roll = rand.nextInt(100);
        if (roll < 40) return Rarity.COMMON;       // 40%
        if (roll < 70) return Rarity.UNCOMMON;      // 30%
        if (roll < 88) return Rarity.RARE;          // 18%
        if (roll < 97) return Rarity.EPIC;          // 9%
        return Rarity.LEGENDARY;                     // 3%
    }

    /**
     * Creates a unique named item for boss-tier enemies (#44).
     * Each boss type drops a signature weapon/armor piece.
     */
    private static Item createBossUnique(javatower.entities.Enemy.EnemyType bossType, int level) {
        switch (bossType) {
            case BONE_COLOSSUS: {
                // Drops a massive bone shield
                Item item = new Item("Colossus Boneshield", "A shield carved from the Colossus's own spine. Radiates an aura of dread.",
                    Item.Slot.OFFHAND, Rarity.EPIC, Item.WeaponClass.DEFENCE, 2, 2, level);
                item.getStatBonuses().put("defence", (int)(20 * Rarity.EPIC.mult * level));
                item.getStatBonuses().put("maxHealth", (int)(15 * Rarity.EPIC.mult * level));
                item.getStatBonuses().put("attack", (int)(5 * Rarity.EPIC.mult * level));
                item.setBuyPrice(0);
                item.setSellPrice((int)(80 * level));
                return item;
            }
            case NECROMANCER_KING: {
                // Drops one of 3 legendary items randomly
                int pick = rand.nextInt(3);
                if (pick == 0) {
                    Item item = new Item("Crown of the Necromancer King", "The cursed crown pulses with necrotic energy. Empowers all dark magic.",
                        Item.Slot.HELMET, Rarity.LEGENDARY, Item.WeaponClass.NECROMANCY, Item.EquipmentSet.DEATH, 2, 1, level);
                    item.getStatBonuses().put("attack", (int)(15 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("mana", (int)(30 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("defence", (int)(10 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("maxHealth", (int)(20 * Rarity.LEGENDARY.mult * level));
                    item.setBuyPrice(0);
                    item.setSellPrice((int)(200 * level));
                    return item;
                } else if (pick == 1) {
                    Item item = new Item("Soulreaper Scythe", "The Necromancer King's personal weapon. Drains life from the living.",
                        Item.Slot.WEAPON, Rarity.LEGENDARY, Item.WeaponClass.NECROMANCY, Item.EquipmentSet.DEATH, 1, 4, level);
                    item.getStatBonuses().put("attack", (int)(25 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("mana", (int)(20 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("speed", (int)(3 * Rarity.LEGENDARY.mult));
                    item.getStatBonuses().put("heal", (int)(8 * Rarity.LEGENDARY.mult * level));
                    item.setTwoHanded(true);
                    item.setBuyPrice(0);
                    item.setSellPrice((int)(250 * level));
                    return item;
                } else {
                    Item item = new Item("Phylactery of Undying", "The King's phylactery. Grants immense resilience to its bearer.",
                        Item.Slot.AMULET, Rarity.LEGENDARY, Item.WeaponClass.NECROMANCY, 1, 1, level);
                    item.getStatBonuses().put("maxHealth", (int)(40 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("defence", (int)(15 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("mana", (int)(25 * Rarity.LEGENDARY.mult * level));
                    item.getStatBonuses().put("heal", (int)(5 * Rarity.LEGENDARY.mult * level));
                    item.setBuyPrice(0);
                    item.setSellPrice((int)(300 * level));
                    return item;
                }
            }
            default:
                return null; // Non-boss enemies or unrecognized, fall through to normal drops
        }
    }
}
