package javatower.factories;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javatower.entities.Item;
import javatower.entities.Item.Rarity;

/**
 * Factory for creating and generating items.
 */
public class ItemFactory {
    private static final Random rand = new Random();

    public static Item createRandomItem(int itemLevel, Rarity rarity) {
        int type = rand.nextInt(5);
        switch (type) {
            case 0: return Item.createSword(itemLevel, rarity);
            case 1: return Item.createShield(itemLevel, rarity);
            case 2: return Item.createHelmet(itemLevel, rarity);
            case 3: return Item.createChestArmor(itemLevel, rarity);
            case 4: return Item.createRing(itemLevel, rarity);
            default: return Item.createSword(itemLevel, rarity);
        }
    }

    public static Item createItemDrop(javatower.entities.Enemy enemy) {
        int level = enemy.getWaveLevel();
        Rarity rarity = Rarity.values()[rand.nextInt(Rarity.values().length)];
        return createRandomItem(level, rarity);
    }

    public static List<Item> generateShopStock(int waveLevel, int count) {
        List<Item> stock = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Rarity rarity = Rarity.values()[rand.nextInt(Rarity.values().length)];
            stock.add(createRandomItem(waveLevel, rarity));
        }
        return stock;
    }
}
