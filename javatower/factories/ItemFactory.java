package javatower.factories;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javatower.entities.Item;
import javatower.entities.Item.Rarity;
import javatower.entities.Item.EquipmentSet;

/**
 * Factory for creating and generating items.
 * Normal items drop commonly; equipment set pieces are rare.
 */
public class ItemFactory {
    private static final Random rand = new Random();

    /** Creates a random normal (non-set) item. */
    public static Item createRandomItem(int itemLevel, Rarity rarity) {
        int type = rand.nextInt(10);
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
            default: return Item.createSword(itemLevel, rarity);
        }
    }

    /** Creates a random set item piece. Set items start at RARE minimum. */
    public static Item createRandomSetItem(int itemLevel, Rarity rarity) {
        // Enforce minimum RARE rarity for set items
        if (rarity.ordinal() < Rarity.RARE.ordinal()) rarity = Rarity.RARE;

        EquipmentSet[] sets = { EquipmentSet.HOLY, EquipmentSet.DEATH, EquipmentSet.FIRE, EquipmentSet.KNIGHT };
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
            default: break;
        }
        return Item.createSword(itemLevel, rarity); // fallback
    }

    /**
     * Creates an item drop from a defeated enemy.
     * 12% chance for a set item; otherwise a normal item.
     */
    public static Item createItemDrop(javatower.entities.Enemy enemy) {
        int level = enemy.getWaveLevel();
        Rarity rarity = rollRarity();
        if (rand.nextInt(100) < 12) {
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
            } else if (rand.nextInt(100) < 10) {
                // 10% chance for another set item
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
}
