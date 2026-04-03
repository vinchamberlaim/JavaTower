package javatower.systems;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import javatower.entities.Item;
import javatower.entities.Hero;
import javatower.factories.ItemFactory;

/**
 * Shop for buying and selling items between waves.
 */
public class Shop {
    private List<Item> availableItems = new ArrayList<>();
    private Set<String> unlockedItemTypes = new HashSet<>();
    private double discountMultiplier = 1.0;

    /**
     * Refreshes the shop stock for the given wave level.
     */
    public void refreshStock(int waveLevel) {
        availableItems = ItemFactory.generateShopStock(waveLevel, 5);
    }

    /**
     * Buys an item for the hero if affordable.
     */
    public boolean buyItem(Hero hero, Item item) {
        int price = (int)(item.getBuyPrice() * discountMultiplier);
        if (hero.spendGold(price)) {
            hero.getInventory().addItem(item);
            availableItems.remove(item);
            return true;
        }
        return false;
    }

    /**
     * Sells an item from the hero's inventory (one at a time from stacks).
     */
    public int sellItem(Hero hero, Item item) {
        int price = item.getSellPrice();
        if (hero.getInventory().removeOne(item)) {
            hero.gainGold(price);
            return price;
        }
        return 0;
    }

    /**
     * Returns items the hero can afford.
     */
    public List<Item> getAffordableItems(int gold) {
        List<Item> affordable = new ArrayList<>();
        for (Item item : availableItems) {
            if (item.getBuyPrice() <= gold) affordable.add(item);
        }
        return affordable;
    }

    // Getters and setters
    public List<Item> getAvailableItems() { return availableItems; }
    public Set<String> getUnlockedItemTypes() { return unlockedItemTypes; }
    public double getDiscountMultiplier() { return discountMultiplier; }
    public void setDiscountMultiplier(double discountMultiplier) { this.discountMultiplier = discountMultiplier; }
}
