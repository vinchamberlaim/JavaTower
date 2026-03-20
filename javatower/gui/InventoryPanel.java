package javatower.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.systems.Inventory;
import java.util.List;

/**
 * Tetris-style inventory display panel.
 */
public class InventoryPanel extends VBox {
    private Hero hero;
    private GameGUI gui;
    private GridPane grid;
    private Label infoLabel;

    public InventoryPanel(Hero hero, GameGUI gui) {
        this.hero = hero;
        this.gui = gui;
        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #1a1a2e;");

        Label header = new Label("INVENTORY");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #e94560;");

        infoLabel = new Label();
        infoLabel.setFont(Font.font("Monospaced", 12));
        infoLabel.setStyle("-fx-text-fill: #eee;");

        grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        Button backBtn = new Button("Back to Game");
        backBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        backBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> gui.returnToGame());

        getChildren().addAll(header, infoLabel, grid, backBtn);
        refresh();
    }

    public void refresh() {
        grid.getChildren().clear();
        Inventory inv = hero.getInventory();

        infoLabel.setText(String.format("Space: %d / %d", inv.getUsedSpace(), inv.getTotalSpace()));

        // Render grid cells
        List<Item> allItems = inv.getAllItems();
        // Simply show items as a list with equip/drop options
        for (int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);

            String setTag = item.getEquipmentSet() != Item.EquipmentSet.NONE
                    ? " {" + item.getEquipmentSet().className + "}" : "";
            Label nameLabel = new Label(item.getName() + " [" + item.getRarity().name() + "]" + setTag);
            nameLabel.setFont(Font.font("Monospaced", 12));
            nameLabel.setStyle("-fx-text-fill: " + item.getRarity().color + ";");
            nameLabel.setPrefWidth(260);

            Label statsLabel = new Label(item.getStatBonuses().toString());
            statsLabel.setFont(Font.font("Monospaced", 10));
            statsLabel.setStyle("-fx-text-fill: #aaa;");
            statsLabel.setPrefWidth(180);

            Button equipBtn = new Button("Equip");
            equipBtn.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: white; -fx-font-size: 10;");
            final Item itemRef = item;
            equipBtn.setOnAction(e -> {
                if (item.getSlot() != Item.Slot.CONSUMABLE) {
                    Item prev = hero.equipItem(itemRef);
                    inv.removeSpecificItem(itemRef);
                    if (prev != null) inv.addItem(prev);
                }
                refresh();
            });

            Button dropBtn = new Button("Drop");
            dropBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 10;");
            dropBtn.setOnAction(e -> {
                inv.removeSpecificItem(itemRef);
                refresh();
            });

            grid.add(nameLabel, 0, i);
            grid.add(statsLabel, 1, i);
            grid.add(equipBtn, 2, i);
            grid.add(dropBtn, 3, i);
        }

        if (allItems.isEmpty()) {
            Label empty = new Label("Inventory is empty");
            empty.setStyle("-fx-text-fill: #666;");
            grid.add(empty, 0, 0);
        }
    }
}
