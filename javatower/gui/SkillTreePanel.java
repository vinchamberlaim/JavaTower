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
import javatower.systems.SkillTree;
import javatower.systems.SkillNode;
import java.util.List;

/**
 * Full-screen skill-tree UI with node-unlock buttons and a respec option.
 * <p>
 * Displays all three hero skill trees (Combat, Magic, Utility) in a
 * scrollable list. Each {@link SkillNode} shows its name, description,
 * cost, and unlock state. The player can unlock nodes if prerequisites
 * are met and enough skill points are available.
 * </p>
 * <p>
 * A <b>Respec</b> button refunds all spent points, resets base stats to
 * their level-appropriate values, and re-applies any still-unlocked nodes.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see SkillTree
 * @see SkillNode
 */
public class SkillTreePanel extends VBox {
    private Hero hero;
    private GameGUI gui;
    private VBox treeContent;
    private Label pointsLabel, statusLabel;

    public SkillTreePanel(Hero hero, GameGUI gui) {
        this.hero = hero;
        this.gui = gui;
        setSpacing(12);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #1a1a2e;");

        Label header = new Label("SKILL TREE");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        header.setStyle("-fx-text-fill: #e94560;");

        pointsLabel = new Label();
        pointsLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        pointsLabel.setStyle("-fx-text-fill: #a855f7;");

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Monospaced", 16));
        statusLabel.setStyle("-fx-text-fill: #4ecca3;");

        treeContent = new VBox(8);

        // Respec button (refund all skill points)
        Button respecBtn = new Button("🔄 Respec All (Refund Points)");
        respecBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        respecBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-cursor: hand;");
        respecBtn.setOnAction(e -> {
            int refunded = 0;
            if (hero.getCombatTree() != null) refunded += hero.getCombatTree().respec();
            if (hero.getMagicTree() != null) refunded += hero.getMagicTree().respec();
            if (hero.getUtilityTree() != null) refunded += hero.getUtilityTree().respec();
            if (refunded > 0) {
                hero.setSkillPoints(hero.getSkillPoints() + refunded);
                // Re-apply base stats (reset and re-apply)
                hero.setMaxHealth(100 + (hero.getLevel() - 1) * 10);
                hero.setAttack(10 + (hero.getLevel() - 1) * 2);
                hero.setDefence(5 + (hero.getLevel() - 1));
                hero.setMaxMana(50 + (hero.getLevel() - 1) * 5);
                // Re-apply unlocked skills
                hero.getCombatTree().applyBonuses(hero);
                hero.getMagicTree().applyBonuses(hero);
                hero.getUtilityTree().applyBonuses(hero);
                statusLabel.setText("Respec complete! Refunded " + refunded + " points");
            } else {
                statusLabel.setText("No points to refund");
            }
            refresh();
        });
        
        Button backBtn = new Button("Back to Game");
        backBtn.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        backBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> gui.returnToGame());

        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(respecBtn, backBtn);

        VBox inner = new VBox(8);
        inner.setPadding(new Insets(10));
        inner.getChildren().addAll(pointsLabel, statusLabel, treeContent, buttonRow);

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        getChildren().addAll(header, scroll);
        refresh();
    }

    public void refresh() {
        pointsLabel.setText("Skill Points: " + hero.getSkillPoints());
        treeContent.getChildren().clear();

        renderTree("Combat", hero.getCombatTree());
        renderTree("Magic", hero.getMagicTree());
        renderTree("Utility", hero.getUtilityTree());

        if (hero.getCombatTree() == null && hero.getMagicTree() == null && hero.getUtilityTree() == null) {
            Label noTrees = new Label("No skill trees initialized yet.\nSkill trees unlock as you level up.");
            noTrees.setFont(Font.font("Monospaced", 18));
            noTrees.setStyle("-fx-text-fill: #666;");
            treeContent.getChildren().add(noTrees);
        }
    }

    private void renderTree(String treeName, SkillTree tree) {
        if (tree == null) return;

        Label treeHeader = new Label("-- " + treeName.toUpperCase() + " TREE --");
        treeHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        treeHeader.setStyle("-fx-text-fill: #22d3ee;");
        treeContent.getChildren().add(treeHeader);

        List<SkillNode> nodes = tree.getNodes();
        for (SkillNode node : nodes) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            String status = node.isUnlocked() ? "[UNLOCKED]" : "[LOCKED]";
            Label nameLabel = new Label(node.getName() + " " + status);
            nameLabel.setFont(Font.font("Monospaced", 16));
            nameLabel.setStyle(node.isUnlocked() ? "-fx-text-fill: #4ecca3;" : "-fx-text-fill: #eee;");
            nameLabel.setPrefWidth(320);

            Label descLabel = new Label(node.getDescription());
            descLabel.setFont(Font.font("Monospaced", 14));
            descLabel.setStyle("-fx-text-fill: #aaa;");
            descLabel.setPrefWidth(260);

            Label costLabel = new Label("Cost: " + node.getCost());
            costLabel.setFont(Font.font("Monospaced", 14));
            costLabel.setStyle("-fx-text-fill: #eab308;");

            row.getChildren().addAll(nameLabel, descLabel, costLabel);

            if (!node.isUnlocked()) {
                Button unlockBtn = new Button("Unlock");
                unlockBtn.setStyle("-fx-background-color: #a855f7; -fx-text-fill: white;");
                final SkillNode nodeRef = node;
                unlockBtn.setOnAction(e -> {
                    if (hero.getSkillPoints() >= nodeRef.getCost() && tree.unlockNode(nodeRef.getId(), hero.getSkillPoints())) {
                        hero.setSkillPoints(hero.getSkillPoints() - nodeRef.getCost());
                        tree.applyBonuses(hero);
                        statusLabel.setText("Unlocked: " + nodeRef.getName() + "!");
                    } else {
                        statusLabel.setText("Cannot unlock - check points/prerequisites");
                    }
                    refresh();
                });
                row.getChildren().add(unlockBtn);
            }

            treeContent.getChildren().add(row);
        }
    }
}
