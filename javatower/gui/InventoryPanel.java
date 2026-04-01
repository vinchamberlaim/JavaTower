package javatower.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.systems.Inventory;
import java.util.List;

/**
 * Equipment + Inventory panel with slot limits and unequip support.
 */
public class InventoryPanel extends VBox {
    private Hero hero;
    private GameGUI gui;
    private VBox equipSection, bagSection;
    private Label infoLabel;

    public InventoryPanel(Hero hero, GameGUI gui) {
        this.hero = hero;
        this.gui = gui;
        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #1a1a2e;");

        Label header = new Label("EQUIPMENT & INVENTORY");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 32));
        header.setStyle("-fx-text-fill: #e94560;");

        infoLabel = new Label();
        infoLabel.setFont(Font.font("Monospaced", 16));
        infoLabel.setStyle("-fx-text-fill: #eee;");

        equipSection = new VBox(4);
        bagSection = new VBox(4);

        Label equipHeader = new Label("-- EQUIPPED --");
        equipHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        equipHeader.setStyle("-fx-text-fill: #22d3ee;");

        Label bagHeader = new Label("-- BACKPACK --");
        bagHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        bagHeader.setStyle("-fx-text-fill: #4ecca3;");

        Button backBtn = new Button("Back to Game");
        backBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        backBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> gui.returnToGame());

        VBox inner = new VBox(8);
        inner.setPadding(new Insets(5));
        inner.getChildren().addAll(equipHeader, equipSection, bagHeader, bagSection, backBtn);

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        getChildren().addAll(header, infoLabel, scroll);
        refresh();
    }

    public void refresh() {
        equipSection.getChildren().clear();
        bagSection.getChildren().clear();
        Inventory inv = hero.getInventory();

        infoLabel.setText(String.format("Backpack: %d / %d  |  Rings: %d / 10",
                inv.getUsedSpace(), inv.getTotalSpace(), hero.getEquippedRingCount()));

        // === Equipped items ===
        addEquipRow("Weapon", hero.getWeapon(), "weapon",
                hero.getWeapon() != null && hero.getWeapon().isTwoHanded() ? " [2H]" : " [1H]");
        addEquipRow("Offhand", hero.getOffhand(), "offhand",
                hero.getWeapon() != null && hero.getWeapon().isTwoHanded() ? " [BLOCKED]" : "");
        addEquipRow("Helmet", hero.getHelmet(), "helmet", "");
        addEquipRow("Chest", hero.getChest(), "chest", "");
        addEquipRow("Legs", hero.getLegs(), "legs", "");
        addEquipRow("Boots", hero.getBoots(), "boots", "");
        addEquipRow("Gloves", hero.getGloves(), "gloves", "");
        addEquipRow("Amulet", hero.getAmulet(), "amulet", "");

        // Rings (up to 10)
        Item[] rings = hero.getRings();
        for (int i = 0; i < rings.length; i++) {
            if (rings[i] != null) {
                addEquipRow("Ring " + (i + 1), rings[i], "ring" + i, "");
            }
        }
        // Show empty ring slots count
        int emptyRings = 10 - hero.getEquippedRingCount();
        if (emptyRings > 0) {
            Label emptyLabel = new Label("  +" + emptyRings + " empty ring slot" + (emptyRings > 1 ? "s" : ""));
            emptyLabel.setFont(Font.font("Monospaced", 14));
            emptyLabel.setStyle("-fx-text-fill: #666;");
            equipSection.getChildren().add(emptyLabel);
        }

        // === Backpack items ===
        List<Item> allItems = inv.getAllItems();
        for (int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);
            addBagRow(item, inv);
        }

        if (allItems.isEmpty()) {
            Label empty = new Label("Backpack is empty");
            empty.setStyle("-fx-text-fill: #666;");
            bagSection.getChildren().add(empty);
        }
    }

    private void addEquipRow(String slotName, Item item, String slotKey, String suffix) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label slotLabel = new Label(slotName + ":");
        slotLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        slotLabel.setStyle("-fx-text-fill: #aaa;");
        slotLabel.setPrefWidth(100);

        if (item != null) {
            String setTag = item.getEquipmentSet() != Item.EquipmentSet.NONE
                    ? " {" + item.getEquipmentSet().className + "}" : "";
            Label nameLabel = new Label(item.getName() + " [" + item.getRarity().name() + "]" + setTag + suffix);
            nameLabel.setFont(Font.font("Monospaced", 15));
            nameLabel.setStyle("-fx-text-fill: " + item.getRarity().color + ";");
            nameLabel.setPrefWidth(360);

            Label statsLabel = new Label(item.getStatBonuses().toString());
            statsLabel.setFont(Font.font("Monospaced", 13));
            statsLabel.setStyle("-fx-text-fill: #888;");
            statsLabel.setPrefWidth(240);

            Button unequipBtn = new Button("Unequip");
            unequipBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 9;");
            unequipBtn.setOnAction(e -> {
                Item removed = hero.unequipSlot(slotKey);
                if (removed != null) {
                    hero.getInventory().addItem(removed);
                }
                refresh();
            });

            row.getChildren().addAll(slotLabel, nameLabel, statsLabel, unequipBtn);
        } else {
            Label emptyLabel = new Label("-- empty --" + suffix);
            emptyLabel.setFont(Font.font("Monospaced", 14));
            emptyLabel.setStyle("-fx-text-fill: #555;");
            row.getChildren().addAll(slotLabel, emptyLabel);
        }

        equipSection.getChildren().add(row);
    }

    private void addBagRow(Item item, Inventory inv) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        String setTag = item.getEquipmentSet() != Item.EquipmentSet.NONE
                ? " {" + item.getEquipmentSet().className + "}" : "";
        String twoH = item.isTwoHanded() ? " [2H]" : "";
        Label nameLabel = new Label(item.getName() + " [" + item.getRarity().name() + "]"
                + setTag + twoH + " (" + item.getSlot() + ")");
        nameLabel.setFont(Font.font("Monospaced", 15));
        nameLabel.setStyle("-fx-text-fill: " + item.getRarity().color + ";");
        nameLabel.setPrefWidth(400);

        Label statsLabel = new Label(item.getStatBonuses().toString());
        statsLabel.setFont(Font.font("Monospaced", 13));
        statsLabel.setStyle("-fx-text-fill: #888;");
        statsLabel.setPrefWidth(220);

        Button equipBtn = new Button("Equip");
        equipBtn.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: white; -fx-font-size: 9;");
        final Item itemRef = item;
        equipBtn.setOnAction(e -> {
            if (item.getSlot() == Item.Slot.CONSUMABLE) {
                // Use consumable
                Integer healVal = item.getStatBonuses().get("heal");
                if (healVal != null) hero.heal(healVal);
                Integer manaVal = item.getStatBonuses().get("mana");
                if (manaVal != null) {
                    // Restore mana (simple add, capped)
                }
                inv.removeSpecificItem(itemRef);
            } else {
                Item prev = hero.equipItem(itemRef);
                inv.removeSpecificItem(itemRef);
                if (prev != null) inv.addItem(prev);
            }
            refresh();
        });

        Button dropBtn = new Button("Drop");
        dropBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 9;");
        dropBtn.setOnAction(e -> {
            inv.removeSpecificItem(itemRef);
            refresh();
        });

        Label priceLabel = new Label("$" + item.getSellPrice());
        priceLabel.setFont(Font.font("Monospaced", 13));
        priceLabel.setStyle("-fx-text-fill: #eab308;");

        row.getChildren().addAll(nameLabel, statsLabel, equipBtn, dropBtn, priceLabel);
        bagSection.getChildren().add(row);
    }
}
