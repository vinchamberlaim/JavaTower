package javatower.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;

/**
 * Displays hero stats and equipment.
 */
public class HeroPanel extends VBox {
    private Hero hero;
    private Label nameLabel, levelLabel, goldLabel;
    private Label atkLabel, defLabel, critLabel, spdLabel, manaLabel;
    private ProgressBar hpBar, xpBar, manaBar;

    public HeroPanel(Hero hero) {
        this.hero = hero;
        setSpacing(6);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #533483; -fx-border-width: 1;");

        Label header = new Label("HERO");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        header.setStyle("-fx-text-fill: #e94560;");

        nameLabel = createStatLabel("");
        levelLabel = createStatLabel("");
        goldLabel = createStatLabel("");
        atkLabel = createStatLabel("");
        defLabel = createStatLabel("");
        critLabel = createStatLabel("");
        spdLabel = createStatLabel("");
        manaLabel = createStatLabel("");

        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(200);
        hpBar.setStyle("-fx-accent: limegreen;");

        xpBar = new ProgressBar(0);
        xpBar.setPrefWidth(200);
        xpBar.setStyle("-fx-accent: #a855f7;");

        manaBar = new ProgressBar(1.0);
        manaBar.setPrefWidth(200);
        manaBar.setStyle("-fx-accent: #22d3ee;");

        Label hpLabel = createStatLabel("HP:");
        Label xpLabel = createStatLabel("XP:");
        Label mpLabel = createStatLabel("MP:");

        getChildren().addAll(header, nameLabel, levelLabel, goldLabel,
                hpLabel, hpBar, mpLabel, manaBar, xpLabel, xpBar,
                atkLabel, defLabel, critLabel, spdLabel, manaLabel);

        refresh();
    }

    public void refresh() {
        if (hero == null) return;
        nameLabel.setText(hero.getName());
        levelLabel.setText("Level: " + hero.getLevel());
        goldLabel.setText("Gold: " + hero.getGold());
        atkLabel.setText("ATK: " + hero.getAttack());
        defLabel.setText("DEF: " + hero.getDefence());
        critLabel.setText("CRIT: " + hero.getCritChance() + "%");
        spdLabel.setText("SPD: " + (int) hero.getMoveSpeed());
        manaLabel.setText("Skill Pts: " + hero.getSkillPoints());

        double hpPct = hero.getMaxHealth() > 0 ? (double) hero.getCurrentHealth() / hero.getMaxHealth() : 0;
        hpBar.setProgress(hpPct);
        hpBar.setStyle(hpPct > 0.5 ? "-fx-accent: limegreen;" : hpPct > 0.25 ? "-fx-accent: yellow;" : "-fx-accent: red;");

        double manaPct = hero.getMaxMana() > 0 ? (double) hero.getMana() / hero.getMaxMana() : 0;
        manaBar.setProgress(manaPct);

        double xpPct = hero.getExperienceToNextLevel() > 0 ? (double) hero.getExperience() / hero.getExperienceToNextLevel() : 0;
        xpBar.setProgress(xpPct);
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 12));
        label.setStyle("-fx-text-fill: #eee;");
        return label;
    }
}
