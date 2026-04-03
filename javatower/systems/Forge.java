package javatower.systems;

import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.entities.Item.Rarity;

/**
 * The Forge lets the hero combine two identical items (same name + same rarity)
 * into an upgraded version at the next rarity tier, for a gold cost.
 *
 * Upgrade path: COMMON → UNCOMMON → RARE → EPIC → LEGENDARY → MYTHIC → DIVINE
 * Cost scales with item level and rarity.
 * Stacked items can be forged — two from the same stack count as a valid pair.
 */
public class Forge {

    /**
     * Checks whether two items can be forged together.
     * Requirements: same name, same rarity, and a higher rarity tier exists.
     * If both references point to the same stack, the stack must contain at least 2.
     */
    public static boolean canForge(Item a, Item b) {
        if (a == null || b == null) return false;
        // Must have a next rarity tier
        if (a.getRarity().ordinal() >= Rarity.values().length - 1) return false;
        if (a == b) {
            // Same stack — need at least 2
            return a.getStackCount() >= 2;
        }
        return a.getName().equals(b.getName()) && a.getRarity() == b.getRarity();
    }

    /**
     * Returns the gold cost to forge these two items.
     */
    public static int getForgeCost(Item item) {
        if (item == null) return 0;
        return (int)(30 * item.getRarity().mult * item.getItemLevel());
    }

    /**
     * Attempts to forge two items. Handles both separate items and stacked items.
     *
     * @return The forged item, or null if forging failed.
     */
    public static Item forge(Hero hero, Item a, Item b) {
        if (!canForge(a, b)) return null;
        int cost = getForgeCost(a);
        if (hero.getGold() < cost) return null;

        // Create the upgraded item
        Item upgraded = a.forgeUpgrade();
        if (upgraded == null) return null;

        if (a == b) {
            // Same stack — decrement by 2
            a.addStack(-2);
            if (a.getStackCount() <= 0) {
                hero.getInventory().removeSpecificItem(a);
            }
        } else {
            // Separate items — remove one from each (stack-aware)
            hero.getInventory().removeOne(a);
            hero.getInventory().removeOne(b);
        }
        hero.spendGold(cost);

        // Add upgraded item to inventory (will auto-stack if possible)
        hero.getInventory().addItem(upgraded);
        return upgraded;
    }

    /**
     * Auto-forges all matching pairs in the hero's inventory, cascading upgrades.
     * @return The total number of forges performed.
     */
    public static int autoForge(Hero hero) {
        int totalForged = 0;
        boolean forgedAny;
        do {
            forgedAny = false;
            java.util.List<Item> inv = hero.getInventory().getAllItems();
            for (Item item : inv) {
                // Stack of 2+ → forge with itself
                if (item.getStackCount() >= 2
                        && item.getRarity().ordinal() < Rarity.values().length - 1) {
                    int cost = getForgeCost(item);
                    if (hero.getGold() >= cost) {
                        Item result = forge(hero, item, item);
                        if (result != null) {
                            totalForged++;
                            forgedAny = true;
                            break; // restart — inventory changed
                        }
                    }
                }
            }
        } while (forgedAny);
        return totalForged;
    }
}
