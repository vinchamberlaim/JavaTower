package javatower.systems;

import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.entities.Item.Rarity;

/**
 * The Forge lets the hero combine two identical items (same name + same rarity)
 * into an upgraded version at the next rarity tier, for a gold cost.
 *
 * Upgrade path: COMMON → UNCOMMON → RARE → EPIC → LEGENDARY
 * Cost scales with item level and rarity.
 */
public class Forge {

    /**
     * Checks whether two items can be forged together.
     * Requirements: same name, same rarity, and not already LEGENDARY.
     */
    public static boolean canForge(Item a, Item b) {
        if (a == null || b == null) return false;
        if (a == b) return false; // can't combine item with itself
        if (a.getRarity() == Rarity.LEGENDARY) return false;
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
     * Attempts to forge two items. Removes both from inventory, creates the upgraded
     * item, adds it to inventory, and deducts gold.
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

        // Remove both source items and deduct gold
        hero.getInventory().removeSpecificItem(a);
        hero.getInventory().removeSpecificItem(b);
        hero.spendGold(cost);

        // Add upgraded item to inventory
        hero.getInventory().addItem(upgraded);
        return upgraded;
    }
}
