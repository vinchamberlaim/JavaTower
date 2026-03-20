package javatower.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents equipment and consumable items with Tetris-style sizing.
 */
public class Item {
    public enum Slot {
        WEAPON, OFFHAND, HELMET, CHEST, LEGS, BOOTS, ACCESSORY, CONSUMABLE
    }
    public enum Rarity {
        COMMON(1.0, "gray"),
        UNCOMMON(1.2, "green"),
        RARE(1.5, "blue"),
        EPIC(2.0, "purple"),
        LEGENDARY(3.0, "orange");
        public final double mult;
        public final String color;
        Rarity(double mult, String color) { this.mult = mult; this.color = color; }
    }

    private String name;
    private String description;
    private Slot slot;
    private Rarity rarity;
    private int width, height;
    private Map<String, Integer> statBonuses;
    private int itemLevel;
    private int buyPrice, sellPrice;

    public Item(String name, String description, Slot slot, Rarity rarity, int width, int height, int itemLevel) {
        this.name = name;
        this.description = description;
        this.slot = slot;
        this.rarity = rarity;
        this.width = width;
        this.height = height;
        this.itemLevel = itemLevel;
        this.statBonuses = new HashMap<>();
    }

    // Static factory methods
    private void autoPrice() {
        buyPrice = (int)(10 * rarity.mult * itemLevel);
        sellPrice = buyPrice / 2;
    }

    public static Item createSword(int level, Rarity rarity) {
        Item item = new Item("Sword", "A sharp blade.", Slot.WEAPON, rarity, 1, 3, level);
        item.statBonuses.put("attack", (int)(10 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createShield(int level, Rarity rarity) {
        Item item = new Item("Shield", "A sturdy shield.", Slot.OFFHAND, rarity, 2, 2, level);
        item.statBonuses.put("defence", (int)(8 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createHelmet(int level, Rarity rarity) {
        Item item = new Item("Helmet", "Protects your head.", Slot.HELMET, rarity, 2, 1, level);
        item.statBonuses.put("defence", (int)(5 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createChestArmor(int level, Rarity rarity) {
        Item item = new Item("Chest Armor", "Protects your torso.", Slot.CHEST, rarity, 2, 3, level);
        item.statBonuses.put("defence", (int)(12 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createRing(int level, Rarity rarity) {
        Item item = new Item("Ring", "A magical ring.", Slot.ACCESSORY, rarity, 1, 1, level);
        item.statBonuses.put("critChance", (int)(2 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createHealthPotion() {
        Item item = new Item("Health Potion", "Restores health.", Slot.CONSUMABLE, Rarity.COMMON, 1, 1, 1);
        item.statBonuses.put("heal", 50);
        item.buyPrice = 25;
        item.sellPrice = 10;
        return item;
    }
    public static Item createManaPotion() {
        Item item = new Item("Mana Potion", "Restores mana.", Slot.CONSUMABLE, Rarity.COMMON, 1, 1, 1);
        item.statBonuses.put("mana", 30);
        item.buyPrice = 20;
        item.sellPrice = 8;
        return item;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Slot getSlot() { return slot; }
    public Rarity getRarity() { return rarity; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Map<String, Integer> getStatBonuses() { return statBonuses; }
    public int getItemLevel() { return itemLevel; }
    public int getBuyPrice() { return buyPrice; }
    public int getSellPrice() { return sellPrice; }
    public void setBuyPrice(int price) { this.buyPrice = price; }
    public void setSellPrice(int price) { this.sellPrice = price; }
}
