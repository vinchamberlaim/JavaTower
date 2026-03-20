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
    /**
     * Weapon class determines which use-based skill is trained when the item is used.
     */
    public enum WeaponClass {
        MELEE("Melee"),
        RANGED("Ranged"),
        NECROMANCY("Necromancy"),
        HOLY("Holy"),
        DEFENCE("Defence"),
        NONE("None");
        public final String displayName;
        WeaponClass(String displayName) { this.displayName = displayName; }
    }

    /**
     * Equipment sets — wearing multiple pieces of the same set grants powerful bonuses.
     * Full sets (4 pieces) are rare and define a class archetype.
     */
    public enum EquipmentSet {
        HOLY("Paladin",    "#f5d442", 2, 4),
        DEATH("Necromancer","#8b5cf6", 2, 4),
        FIRE("Pyromancer", "#ef4444", 2, 4),
        KNIGHT("Warrior",  "#60a5fa", 2, 4),
        NONE("None",       "#aaa",    0, 0);

        public final String className, color;
        public final int twoPieceThreshold, fourPieceThreshold;
        EquipmentSet(String className, String color, int two, int four) {
            this.className = className; this.color = color;
            this.twoPieceThreshold = two; this.fourPieceThreshold = four;
        }
    }

    private String name;
    private String description;
    private Slot slot;
    private Rarity rarity;
    private WeaponClass weaponClass;
    private EquipmentSet equipmentSet;
    private int width, height;
    private Map<String, Integer> statBonuses;
    private int itemLevel;
    private int buyPrice, sellPrice;

    public Item(String name, String description, Slot slot, Rarity rarity, WeaponClass weaponClass, int width, int height, int itemLevel) {
        this(name, description, slot, rarity, weaponClass, EquipmentSet.NONE, width, height, itemLevel);
    }

    public Item(String name, String description, Slot slot, Rarity rarity, WeaponClass weaponClass, EquipmentSet equipmentSet, int width, int height, int itemLevel) {
        this.name = name;
        this.description = description;
        this.slot = slot;
        this.rarity = rarity;
        this.weaponClass = weaponClass;
        this.equipmentSet = equipmentSet;
        this.width = width;
        this.height = height;
        this.itemLevel = itemLevel;
        this.statBonuses = new HashMap<>();
    }

    // Static factory methods
    private void autoPrice() {
        int statTotal = 0;
        for (int v : statBonuses.values()) statTotal += Math.abs(v);
        buyPrice = (int)(10 * rarity.mult * itemLevel) + statTotal;
        sellPrice = buyPrice / 3;
    }

    public static Item createSword(int level, Rarity rarity) {
        Item item = new Item("Sword", "A sharp blade. Trains Melee skill.", Slot.WEAPON, rarity, WeaponClass.MELEE, 1, 3, level);
        item.statBonuses.put("attack", (int)(10 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(2 * rarity.mult));
        item.autoPrice();
        return item;
    }
    public static Item createBow(int level, Rarity rarity) {
        Item item = new Item("Crossbow", "A mechanical crossbow. Trains Ranged skill.", Slot.WEAPON, rarity, WeaponClass.RANGED, 2, 3, level);
        item.statBonuses.put("attack", (int)(7 * rarity.mult * level));
        item.statBonuses.put("range", (int)(30 * rarity.mult));
        item.statBonuses.put("speed", (int)(1 * rarity.mult));
        item.autoPrice();
        return item;
    }
    public static Item createNecroStaff(int level, Rarity rarity) {
        Item item = new Item("Necromancer Staff", "A dark staff. Trains Necromancy skill.", Slot.WEAPON, rarity, WeaponClass.NECROMANCY, 1, 4, level);
        item.statBonuses.put("attack", (int)(6 * rarity.mult * level));
        item.statBonuses.put("mana", (int)(10 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(1 * rarity.mult));
        item.autoPrice();
        return item;
    }
    public static Item createHolyMace(int level, Rarity rarity) {
        Item item = new Item("Holy Mace", "A blessed mace. Trains Holy skill.", Slot.WEAPON, rarity, WeaponClass.HOLY, 1, 3, level);
        item.statBonuses.put("attack", (int)(8 * rarity.mult * level));
        item.statBonuses.put("heal", (int)(3 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(1 * rarity.mult));
        item.autoPrice();
        return item;
    }
    public static Item createShield(int level, Rarity rarity) {
        Item item = new Item("Shield", "A sturdy shield. Trains Defence skill.", Slot.OFFHAND, rarity, WeaponClass.DEFENCE, 2, 2, level);
        item.statBonuses.put("defence", (int)(8 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createHelmet(int level, Rarity rarity) {
        Item item = new Item("Helmet", "Protects your head.", Slot.HELMET, rarity, WeaponClass.DEFENCE, 2, 1, level);
        item.statBonuses.put("defence", (int)(5 * rarity.mult * level));
        item.statBonuses.put("health", (int)(5 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createChestArmor(int level, Rarity rarity) {
        Item item = new Item("Chest Armor", "Protects your torso.", Slot.CHEST, rarity, WeaponClass.DEFENCE, 2, 3, level);
        item.statBonuses.put("defence", (int)(12 * rarity.mult * level));
        item.statBonuses.put("health", (int)(15 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createRing(int level, Rarity rarity) {
        Item item = new Item("Ring", "A magical ring.", Slot.ACCESSORY, rarity, WeaponClass.NONE, 1, 1, level);
        item.statBonuses.put("critChance", (int)(2 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createBoots(int level, Rarity rarity) {
        Item item = new Item("Boots", "Sturdy footwear. Increases movement and attack speed.", Slot.BOOTS, rarity, WeaponClass.NONE, 2, 1, level);
        item.statBonuses.put("speed", (int)(3 * rarity.mult));
        item.statBonuses.put("moveSpeed", (int)(10 * rarity.mult));
        item.statBonuses.put("defence", (int)(2 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createLeggings(int level, Rarity rarity) {
        Item item = new Item("Leggings", "Leg armor. Balances defence and mobility.", Slot.LEGS, rarity, WeaponClass.DEFENCE, 2, 2, level);
        item.statBonuses.put("defence", (int)(7 * rarity.mult * level));
        item.statBonuses.put("health", (int)(10 * rarity.mult * level));
        item.autoPrice();
        return item;
    }
    public static Item createHealthPotion() {
        Item item = new Item("Health Potion", "Restores health. Trains Holy skill.", Slot.CONSUMABLE, Rarity.COMMON, WeaponClass.HOLY, 1, 1, 1);
        item.statBonuses.put("heal", 50);
        item.buyPrice = 25;
        item.sellPrice = 10;
        return item;
    }
    public static Item createManaPotion() {
        Item item = new Item("Mana Potion", "Restores mana.", Slot.CONSUMABLE, Rarity.COMMON, WeaponClass.NECROMANCY, 1, 1, 1);
        item.statBonuses.put("mana", 30);
        item.buyPrice = 20;
        item.sellPrice = 8;
        return item;
    }

    // ========== HOLY SET (Paladin) — healing + undead slaying ==========
    public static Item createHolySword(int level, Rarity rarity) {
        Item item = new Item("Holy Sword", "A blessed blade that smites the undead.", Slot.WEAPON, rarity, WeaponClass.HOLY, EquipmentSet.HOLY, 1, 3, level);
        item.statBonuses.put("attack", (int)(12 * rarity.mult * level));
        item.statBonuses.put("heal", (int)(2 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(2 * rarity.mult));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createBlessedShield(int level, Rarity rarity) {
        Item item = new Item("Blessed Shield", "Shines with divine light.", Slot.OFFHAND, rarity, WeaponClass.HOLY, EquipmentSet.HOLY, 2, 2, level);
        item.statBonuses.put("defence", (int)(10 * rarity.mult * level));
        item.statBonuses.put("heal", (int)(2 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createHolyHelm(int level, Rarity rarity) {
        Item item = new Item("Holy Helm", "Anointed headgear.", Slot.HELMET, rarity, WeaponClass.HOLY, EquipmentSet.HOLY, 2, 1, level);
        item.statBonuses.put("defence", (int)(6 * rarity.mult * level));
        item.statBonuses.put("heal", (int)(1 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createHolyPlate(int level, Rarity rarity) {
        Item item = new Item("Holy Plate", "Radiant armor forged in a cathedral.", Slot.CHEST, rarity, WeaponClass.HOLY, EquipmentSet.HOLY, 2, 3, level);
        item.statBonuses.put("defence", (int)(14 * rarity.mult * level));
        item.statBonuses.put("heal", (int)(3 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }

    // ========== DEATH SET (Necromancer) — dark magic + life drain ==========
    public static Item createDeathStaff(int level, Rarity rarity) {
        Item item = new Item("Death Staff", "Pulses with necrotic energy.", Slot.WEAPON, rarity, WeaponClass.NECROMANCY, EquipmentSet.DEATH, 1, 4, level);
        item.statBonuses.put("attack", (int)(8 * rarity.mult * level));
        item.statBonuses.put("mana", (int)(12 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(2 * rarity.mult));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createGrimoire(int level, Rarity rarity) {
        Item item = new Item("Grimoire of Shadows", "A tome of forbidden knowledge.", Slot.OFFHAND, rarity, WeaponClass.NECROMANCY, EquipmentSet.DEATH, 2, 2, level);
        item.statBonuses.put("mana", (int)(15 * rarity.mult * level));
        item.statBonuses.put("attack", (int)(4 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createDeathHood(int level, Rarity rarity) {
        Item item = new Item("Death Hood", "Shadows cling to its fabric.", Slot.HELMET, rarity, WeaponClass.NECROMANCY, EquipmentSet.DEATH, 2, 1, level);
        item.statBonuses.put("defence", (int)(4 * rarity.mult * level));
        item.statBonuses.put("mana", (int)(8 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createDeathRobes(int level, Rarity rarity) {
        Item item = new Item("Death Robes", "Woven from graveyard silk.", Slot.CHEST, rarity, WeaponClass.NECROMANCY, EquipmentSet.DEATH, 2, 3, level);
        item.statBonuses.put("defence", (int)(8 * rarity.mult * level));
        item.statBonuses.put("mana", (int)(10 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }

    // ========== FIRE SET (Pyromancer) — raw damage + AoE splash ==========
    public static Item createFireStaff(int level, Rarity rarity) {
        Item item = new Item("Fire Staff", "Burns with eternal flame.", Slot.WEAPON, rarity, WeaponClass.RANGED, EquipmentSet.FIRE, 1, 4, level);
        item.statBonuses.put("attack", (int)(10 * rarity.mult * level));
        item.statBonuses.put("range", (int)(20 * rarity.mult));
        item.statBonuses.put("speed", (int)(2 * rarity.mult));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createFlameOrb(int level, Rarity rarity) {
        Item item = new Item("Flame Orb", "A sphere of condensed fire.", Slot.OFFHAND, rarity, WeaponClass.RANGED, EquipmentSet.FIRE, 1, 1, level);
        item.statBonuses.put("attack", (int)(6 * rarity.mult * level));
        item.statBonuses.put("critChance", (int)(3 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createFlameCrown(int level, Rarity rarity) {
        Item item = new Item("Flame Crown", "Circlet wreathed in fire.", Slot.HELMET, rarity, WeaponClass.RANGED, EquipmentSet.FIRE, 2, 1, level);
        item.statBonuses.put("defence", (int)(4 * rarity.mult * level));
        item.statBonuses.put("attack", (int)(4 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createFlameRobes(int level, Rarity rarity) {
        Item item = new Item("Flame Robes", "Embered cloth that never burns out.", Slot.CHEST, rarity, WeaponClass.RANGED, EquipmentSet.FIRE, 2, 3, level);
        item.statBonuses.put("defence", (int)(6 * rarity.mult * level));
        item.statBonuses.put("attack", (int)(5 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }

    // ========== KNIGHT SET (Warrior) — tanky + thorns + melee speed ==========
    public static Item createSteelGreatsword(int level, Rarity rarity) {
        Item item = new Item("Steel Greatsword", "A heavy two-handed blade.", Slot.WEAPON, rarity, WeaponClass.MELEE, EquipmentSet.KNIGHT, 1, 3, level);
        item.statBonuses.put("attack", (int)(14 * rarity.mult * level));
        item.statBonuses.put("speed", (int)(3 * rarity.mult));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createTowerShield(int level, Rarity rarity) {
        Item item = new Item("Tower Shield", "An imposing knightly shield.", Slot.OFFHAND, rarity, WeaponClass.DEFENCE, EquipmentSet.KNIGHT, 2, 2, level);
        item.statBonuses.put("defence", (int)(14 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createSteelHelm(int level, Rarity rarity) {
        Item item = new Item("Steel Helm", "Full-face knight's helmet.", Slot.HELMET, rarity, WeaponClass.DEFENCE, EquipmentSet.KNIGHT, 2, 1, level);
        item.statBonuses.put("defence", (int)(8 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }
    public static Item createSteelPlate(int level, Rarity rarity) {
        Item item = new Item("Steel Plate", "Heavy plate mail for a true knight.", Slot.CHEST, rarity, WeaponClass.DEFENCE, EquipmentSet.KNIGHT, 2, 3, level);
        item.statBonuses.put("defence", (int)(18 * rarity.mult * level));
        item.autoPrice(); item.buyPrice *= 2; item.sellPrice *= 2;
        return item;
    }

    /**
     * Creates an upgraded copy of this item at the next rarity tier.
     * Used by the Forge system.
     */
    public Item forgeUpgrade() {
        Rarity[] rarities = Rarity.values();
        int nextIdx = rarity.ordinal() + 1;
        if (nextIdx >= rarities.length) return null; // already max
        Rarity nextRarity = rarities[nextIdx];

        // Re-create with same properties but higher rarity
        Item upgraded = new Item(name, description, slot, nextRarity, weaponClass, equipmentSet, width, height, itemLevel);
        // Scale stats: multiply each bonus by (newMult / oldMult)
        double scale = nextRarity.mult / rarity.mult;
        for (Map.Entry<String, Integer> e : statBonuses.entrySet()) {
            upgraded.statBonuses.put(e.getKey(), (int)(e.getValue() * scale));
        }
        upgraded.autoPrice();
        if (equipmentSet != EquipmentSet.NONE) {
            upgraded.buyPrice *= 2;
            upgraded.sellPrice *= 2;
        }
        return upgraded;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Slot getSlot() { return slot; }
    public Rarity getRarity() { return rarity; }
    public WeaponClass getWeaponClass() { return weaponClass; }
    public EquipmentSet getEquipmentSet() { return equipmentSet; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Map<String, Integer> getStatBonuses() { return statBonuses; }
    public int getItemLevel() { return itemLevel; }
    public int getBuyPrice() { return buyPrice; }
    public int getSellPrice() { return sellPrice; }
    public void setBuyPrice(int price) { this.buyPrice = price; }
    public void setSellPrice(int price) { this.sellPrice = price; }
}
