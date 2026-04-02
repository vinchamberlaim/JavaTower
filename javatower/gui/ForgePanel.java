package javatower.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.systems.Forge;
import java.util.List;
import java.util.ArrayList;

/**
 * Forge panel — item-combining UI.
 * <p>
 * The player selects two inventory items with the <em>same name and rarity</em>,
 * pays a gold cost, and receives a single upgraded item at the next rarity tier.
 * Uses the {@link Forge} system to validate and execute the combination.
 * </p>
 * <p>
 * Layout: header → gold display → two selection slots (Slot 1 + Slot 2) →
 * FORGE button → scrollable inventory list → Back button.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see Forge
 */
public class ForgePanel extends VBox {
    private Hero hero;
    private GameGUI gui;
    private Label goldLabel, statusLabel;
    private VBox itemListSection;
    private Item selectedA, selectedB;
    private Label selectALabel, selectBLabel;
    private Button forgeBtn;

    public ForgePanel(Hero hero, GameGUI gui) {
        this.hero = hero;
        this.gui = gui;
        setSpacing(12);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #1a1a2e;");

        Label header = new Label("FORGE");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        header.setStyle("-fx-text-fill: #f97316;");

        Label desc = new Label("Combine 2 identical items (same name + rarity) to upgrade tier.");
        desc.setFont(Font.font("Monospaced", 16));
        desc.setStyle("-fx-text-fill: #aaa;");

        goldLabel = new Label();
        goldLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        goldLabel.setStyle("-fx-text-fill: #eab308;");

        statusLabel = new Label("Select two items to forge.");
        statusLabel.setFont(Font.font("Monospaced", 16));
        statusLabel.setStyle("-fx-text-fill: #4ecca3;");

        // Selection display
        HBox selectionRow = new HBox(20);
        selectionRow.setAlignment(Pos.CENTER);

        VBox slotA = new VBox(4);
        slotA.setAlignment(Pos.CENTER);
        Label aHeader = new Label("Slot 1");
        aHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        aHeader.setStyle("-fx-text-fill: #eee;");
        selectALabel = new Label("[empty]");
        selectALabel.setFont(Font.font("Monospaced", 15));
        selectALabel.setStyle("-fx-text-fill: #888;");
        Button clearA = new Button("Clear");
        clearA.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14;");
        clearA.setOnAction(e -> { selectedA = null; refresh(); });
        slotA.getChildren().addAll(aHeader, selectALabel, clearA);

        Label plus = new Label("+");
        plus.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        plus.setStyle("-fx-text-fill: #f97316;");

        VBox slotB = new VBox(4);
        slotB.setAlignment(Pos.CENTER);
        Label bHeader = new Label("Slot 2");
        bHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        bHeader.setStyle("-fx-text-fill: #eee;");
        selectBLabel = new Label("[empty]");
        selectBLabel.setFont(Font.font("Monospaced", 15));
        selectBLabel.setStyle("-fx-text-fill: #888;");
        Button clearB = new Button("Clear");
        clearB.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14;");
        clearB.setOnAction(e -> { selectedB = null; refresh(); });
        slotB.getChildren().addAll(bHeader, selectBLabel, clearB);

        selectionRow.getChildren().addAll(slotA, plus, slotB);

        // Forge button
        forgeBtn = new Button("FORGE!");
        forgeBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        forgeBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-cursor: hand;");
        forgeBtn.setDisable(true);
        forgeBtn.setOnAction(e -> doForge());

        // Item list (in scrollable area)
        Label invHeader = new Label("-- Inventory --");
        invHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        invHeader.setStyle("-fx-text-fill: #22d3ee;");

        itemListSection = new VBox(4);
        
        // Wrap item list in ScrollPane so it can scroll if too long
        ScrollPane scrollPane = new ScrollPane(itemListSection);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300); // Fixed height for scrollable area
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button backBtn = new Button("Back to Game");
        backBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        backBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> gui.returnToGame());

        getChildren().addAll(header, desc, goldLabel, statusLabel, selectionRow, forgeBtn, invHeader, scrollPane, backBtn);
        refresh();
    }

    private void doForge() {
        if (selectedA == null || selectedB == null) return;
        if (!Forge.canForge(selectedA, selectedB)) {
            statusLabel.setText("These items can't be forged together!");
            statusLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }
        int cost = Forge.getForgeCost(selectedA);
        if (hero.getGold() < cost) {
            statusLabel.setText("Not enough gold! Need " + cost + "g");
            statusLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }
        Item result = Forge.forge(hero, selectedA, selectedB);
        if (result != null) {
            statusLabel.setText("Forged " + result.getName() + " [" + result.getRarity().name() + "]!");
            statusLabel.setStyle("-fx-text-fill: #4ecca3;");
            selectedA = null;
            selectedB = null;
        } else {
            statusLabel.setText("Forging failed!");
            statusLabel.setStyle("-fx-text-fill: #e94560;");
        }
        refresh();
    }

    public void refresh() {
        goldLabel.setText("Gold: " + hero.getGold());

        // Update selection labels
        if (selectedA != null) {
            selectALabel.setText(selectedA.getName() + " [" + selectedA.getRarity().name() + "]");
            selectALabel.setStyle("-fx-text-fill: " + selectedA.getRarity().color + ";");
        } else {
            selectALabel.setText("[empty]");
            selectALabel.setStyle("-fx-text-fill: #888;");
        }

        if (selectedB != null) {
            selectBLabel.setText(selectedB.getName() + " [" + selectedB.getRarity().name() + "]");
            selectBLabel.setStyle("-fx-text-fill: " + selectedB.getRarity().color + ";");
        } else {
            selectBLabel.setText("[empty]");
            selectBLabel.setStyle("-fx-text-fill: #888;");
        }

        // Enable forge button only if valid
        boolean canForge = selectedA != null && selectedB != null && Forge.canForge(selectedA, selectedB);
        forgeBtn.setDisable(!canForge);
        if (canForge) {
            int cost = Forge.getForgeCost(selectedA);
            forgeBtn.setText("FORGE! (" + cost + "g)");
        } else {
            forgeBtn.setText("FORGE!");
        }

        // Render inventory items with select buttons
        itemListSection.getChildren().clear();
        List<Item> inventory = hero.getInventory().getAllItems();
        for (Item item : inventory) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            String setTag = item.getEquipmentSet() != Item.EquipmentSet.NONE
                    ? " {" + item.getEquipmentSet().className + "}" : "";
            Label name = new Label(item.getName() + " [" + item.getRarity().name() + "]" + setTag);
            name.setFont(Font.font("Monospaced", 15));
            name.setStyle("-fx-text-fill: " + item.getRarity().color + ";");
            name.setPrefWidth(360);

            Label stats = new Label(item.getStatBonuses().toString());
            stats.setFont(Font.font("Monospaced", 14));
            stats.setStyle("-fx-text-fill: #aaa;");
            stats.setPrefWidth(200);

            Button selA = new Button("Slot 1");
            selA.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: white; -fx-font-size: 14;");
            final Item ref = item;
            selA.setOnAction(e -> { selectedA = ref; refresh(); });

            Button selB = new Button("Slot 2");
            selB.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-font-size: 14;");
            selB.setOnAction(e -> { selectedB = ref; refresh(); });

            row.getChildren().addAll(name, stats, selA, selB);
            itemListSection.getChildren().add(row);
        }

        if (inventory.isEmpty()) {
            Label empty = new Label("No items in inventory");
            empty.setStyle("-fx-text-fill: #666;");
            itemListSection.getChildren().add(empty);
        }
    }
}
