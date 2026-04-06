package javatower.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.systems.Forge;
import javatower.systems.Inventory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Equipment + inventory panel with slot limits, unequip support, and filters.
 *
 * <p><b>CIS096 relevance:</b> UI layer over the {@link Inventory} 2D-grid data
 * structure (Tetris-style placement) and {@link Hero} equipment model. This
 * class demonstrates separation of concerns: rendering + user actions here,
 * placement validation and state rules in domain/system classes.</p>
 */
public class InventoryPanel extends VBox {
    private Hero hero;
    private GameGUI gui;
    private VBox equipSection, bagSection;
    private Label infoLabel;
    private ScrollPane scrollPane;
    private double savedScrollV = 0;
    private boolean isRefreshing = false;
    
    // Filter state
    private Item.WeaponClass filterWeaponClass = null;  // null = show all
    private Item.EquipmentSet filterEquipmentSet = null;
    private boolean filterForgeableOnly = false;
    private HBox filterBar;

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

        // Filter bar
        filterBar = createFilterBar();

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

        scrollPane = new ScrollPane(inner);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        
        // Persist scroll position (but not during refresh)
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isRefreshing) {
                savedScrollV = newVal.doubleValue();
            }
        });

        getChildren().addAll(header, infoLabel, filterBar, scrollPane);
        refresh();
    }

    /**
     * Creates the filter bar with class, set, and forgeable filters.
     */
    private HBox createFilterBar() {
        HBox bar = new HBox(6);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(5));
        bar.setStyle("-fx-background-color: #0d1b2a; -fx-border-color: #1b263b; -fx-border-radius: 4;");

        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        filterLabel.setStyle("-fx-text-fill: #aaa;");

        // Forgeable filter (duplicates)
        ToggleButton forgeBtn = new ToggleButton("⚒ Forgeable");
        forgeBtn.setFont(Font.font("Monospaced", 11));
        styleFilterButton(forgeBtn, "#eab308");
        forgeBtn.setOnAction(e -> {
            filterForgeableOnly = forgeBtn.isSelected();
            refresh();
        });

        // WeaponClass filters
        ToggleButton meleeBtn = createClassFilterButton("⚔ Melee", Item.WeaponClass.MELEE, "#60a5fa");
        ToggleButton rangedBtn = createClassFilterButton("🏹 Ranged", Item.WeaponClass.RANGED, "#22c55e");
        ToggleButton necroBtn = createClassFilterButton("💀 Necro", Item.WeaponClass.NECROMANCY, "#8b5cf6");
        ToggleButton holyBtn = createClassFilterButton("✝ Holy", Item.WeaponClass.HOLY, "#f5d442");
        ToggleButton defBtn = createClassFilterButton("🛡 Defence", Item.WeaponClass.DEFENCE, "#94a3b8");

        // EquipmentSet filters
        Label setLabel = new Label(" | Sets:");
        setLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        setLabel.setStyle("-fx-text-fill: #666;");

        ToggleButton paladinBtn = createSetFilterButton("Paladin", Item.EquipmentSet.HOLY, "#f5d442");
        ToggleButton necroSetBtn = createSetFilterButton("Necro", Item.EquipmentSet.DEATH, "#8b5cf6");
        ToggleButton pyroBtn = createSetFilterButton("Pyro", Item.EquipmentSet.FIRE, "#ef4444");
        ToggleButton warriorBtn = createSetFilterButton("Warrior", Item.EquipmentSet.KNIGHT, "#60a5fa");

        // Clear all filters button
        Button clearBtn = new Button("✕");
        clearBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        clearBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-padding: 2 6;");
        clearBtn.setOnAction(e -> {
            filterWeaponClass = null;
            filterEquipmentSet = null;
            filterForgeableOnly = false;
            forgeBtn.setSelected(false);
            meleeBtn.setSelected(false);
            rangedBtn.setSelected(false);
            necroBtn.setSelected(false);
            holyBtn.setSelected(false);
            defBtn.setSelected(false);
            paladinBtn.setSelected(false);
            necroSetBtn.setSelected(false);
            pyroBtn.setSelected(false);
            warriorBtn.setSelected(false);
            refresh();
        });

        bar.getChildren().addAll(filterLabel, forgeBtn, 
                meleeBtn, rangedBtn, necroBtn, holyBtn, defBtn,
                setLabel, paladinBtn, necroSetBtn, pyroBtn, warriorBtn, clearBtn);
        return bar;
    }

    private ToggleButton createClassFilterButton(String text, Item.WeaponClass wc, String color) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Font.font("Monospaced", 11));
        styleFilterButton(btn, color);
        btn.setOnAction(e -> {
            filterWeaponClass = btn.isSelected() ? wc : null;
            refresh();
        });
        return btn;
    }

    private ToggleButton createSetFilterButton(String text, Item.EquipmentSet set, String color) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Font.font("Monospaced", 11));
        styleFilterButton(btn, color);
        btn.setOnAction(e -> {
            filterEquipmentSet = btn.isSelected() ? set : null;
            refresh();
        });
        return btn;
    }

    private void styleFilterButton(ToggleButton btn, String color) {
        btn.setStyle("-fx-background-color: #2a2a4e; -fx-text-fill: " + color + "; -fx-padding: 3 8; -fx-background-radius: 4;");
        btn.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #1a1a2e; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            } else {
                btn.setStyle("-fx-background-color: #2a2a4e; -fx-text-fill: " + color + "; -fx-padding: 3 8; -fx-background-radius: 4;");
            }
        });
    }

    /**
     * Checks if an item passes the current filters.
     */
    private boolean passesFilter(Item item) {
        if (item == null) return false;
        
        // WeaponClass filter
        if (filterWeaponClass != null && item.getWeaponClass() != filterWeaponClass) {
            return false;
        }
        
        // EquipmentSet filter
        if (filterEquipmentSet != null && item.getEquipmentSet() != filterEquipmentSet) {
            return false;
        }
        
        // Forgeable filter — check if this item has a duplicate (can forge)
        if (filterForgeableOnly) {
            return canBeForged(item);
        }
        
        return true;
    }

    /**
     * Checks if an item can be forged (has duplicate in inventory or stack >= 2).
     */
    private boolean canBeForged(Item item) {
        if (item == null) return false;
        // Can't forge max rarity
        if (item.getRarity().ordinal() >= Item.Rarity.values().length - 1) return false;
        
        // Stack of 2+ can self-forge
        if (item.getStackCount() >= 2) return true;
        
        // Check for another item with same name and rarity
        List<Item> allItems = hero.getInventory().getAllItems();
        int count = 0;
        for (Item other : allItems) {
            if (other.getName().equals(item.getName()) && other.getRarity() == item.getRarity()) {
                count++;
                if (count >= 2) return true;
            }
        }
        return false;
    }

    public void refresh() {
        isRefreshing = true;
        equipSection.getChildren().clear();
        bagSection.getChildren().clear();
        Inventory inv = hero.getInventory();

        // Count forgeable items
        int forgeableCount = 0;
        for (Item item : inv.getAllItems()) {
            if (canBeForged(item)) forgeableCount++;
        }

        infoLabel.setText(String.format("Backpack: %d / %d  |  Rings: %d / 10  |  Forgeable: %d",
                inv.getUsedSpace(), inv.getTotalSpace(), hero.getEquippedRingCount(), forgeableCount));

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

        // === Backpack items (with filters applied) ===
        List<Item> allItems = inv.getAllItems();
        int shown = 0;
        int hidden = 0;
        for (int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);
            if (passesFilter(item)) {
                addBagRow(item, inv);
                shown++;
            } else {
                hidden++;
            }
        }

        if (shown == 0 && hidden > 0) {
            Label noMatch = new Label("No items match current filter (" + hidden + " hidden)");
            noMatch.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            bagSection.getChildren().add(noMatch);
        } else if (shown == 0 && hidden == 0) {
            Label empty = new Label("Backpack is empty");
            empty.setStyle("-fx-text-fill: #666;");
            bagSection.getChildren().add(empty);
        } else if (hidden > 0) {
            Label filterInfo = new Label("(" + hidden + " items hidden by filter)");
            filterInfo.setStyle("-fx-text-fill: #555; -fx-font-size: 11;");
            bagSection.getChildren().add(filterInfo);
        }
        
        // Restore scroll position
        if (scrollPane != null) {
            final double targetScroll = savedScrollV;
            javafx.application.Platform.runLater(() -> {
                scrollPane.setVvalue(targetScroll);
                isRefreshing = false;
            });
        } else {
            isRefreshing = false;
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
        String classTag = item.getWeaponClass() != Item.WeaponClass.NONE
                ? " [" + item.getWeaponClass().displayName + "]" : "";
        String twoH = item.isTwoHanded() ? " [2H]" : "";
        String stackTag = item.getStackCount() > 1 ? " x" + item.getStackCount() : "";
        String forgeTag = canBeForged(item) ? " ⚒" : "";
        
        Label nameLabel = new Label(item.getName() + " [" + item.getRarity().name() + "]"
                + setTag + classTag + twoH + stackTag + forgeTag + " (" + item.getSlot() + ")");
        nameLabel.setFont(Font.font("Monospaced", 15));
        String textColor = item.getRarity().color;
        if (canBeForged(item)) {
            nameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        } else {
            nameLabel.setStyle("-fx-text-fill: " + textColor + ";");
        }
        nameLabel.setPrefWidth(420);

        Label statsLabel = new Label(item.getStatBonuses().toString());
        statsLabel.setFont(Font.font("Monospaced", 13));
        statsLabel.setStyle("-fx-text-fill: #888;");
        statsLabel.setPrefWidth(220);

        Button equipBtn = new Button("Equip");
        equipBtn.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: white; -fx-font-size: 9;");
        final Item itemRef = item;
        equipBtn.setOnAction(e -> {
            // Check if item still exists in inventory (prevent race condition)
            if (!inv.getAllItems().contains(itemRef)) {
                refresh();
                return;
            }
            
            if (item.getSlot() == Item.Slot.CONSUMABLE) {
                // Use consumable
                Integer healVal = item.getStatBonuses().get("heal");
                if (healVal != null) hero.heal(healVal);
                Integer manaVal = item.getStatBonuses().get("mana");
                if (manaVal != null) {
                    // Restore mana (simple add, capped)
                }
                inv.removeOne(itemRef);
            } else {
                Item toEquip = (itemRef.getStackCount() > 1) ? itemRef.copy() : itemRef;
                if (itemRef.getStackCount() > 1) {
                    itemRef.addStack(-1);
                } else {
                    inv.removeSpecificItem(itemRef);
                }
                Item prev = hero.equipItem(toEquip);
                if (prev != null) inv.addItem(prev);
            }
            refresh();
        });

        Button dropBtn = new Button("Drop");
        dropBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 9;");
        dropBtn.setOnAction(e -> {
            inv.removeOne(itemRef);
            refresh();
        });

        Label priceLabel = new Label("$" + item.getSellPrice());
        priceLabel.setFont(Font.font("Monospaced", 13));
        priceLabel.setStyle("-fx-text-fill: #eab308;");

        row.getChildren().addAll(nameLabel, statsLabel, equipBtn, dropBtn, priceLabel);
        bagSection.getChildren().add(row);
    }
}
