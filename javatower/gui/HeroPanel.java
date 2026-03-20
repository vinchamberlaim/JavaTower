package javatower.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.entities.Hero;
import javatower.entities.Item.WeaponClass;
import javatower.entities.Item.EquipmentSet;
import javatower.systems.SkillProgression;
import javatower.systems.SetBonusManager;
import java.util.List;
import java.util.Map;

/**
 * Displays hero stats, skill levels, and active set bonuses.
 */
public class HeroPanel extends VBox {
    private Hero hero;
    private Label nameLabel, levelLabel, goldLabel;
    private Label atkLabel, defLabel, critLabel, spdLabel, manaLabel;
    private ProgressBar hpBar, xpBar, manaBar;
    private VBox skillSection, setSection;

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

        // Skill progression section
        Label skillHeader = new Label("SKILLS");
        skillHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        skillHeader.setStyle("-fx-text-fill: #a855f7;");
        skillSection = new VBox(2);

        // Set bonus section
        Label setHeader = new Label("SET BONUSES");
        setHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        setHeader.setStyle("-fx-text-fill: #f97316;");
        setSection = new VBox(2);

        getChildren().addAll(header, nameLabel, levelLabel, goldLabel,
                hpLabel, hpBar, mpLabel, manaBar, xpLabel, xpBar,
                atkLabel, defLabel, critLabel, spdLabel, manaLabel,
                skillHeader, skillSection, setHeader, setSection);

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

        // Refresh skill levels
        skillSection.getChildren().clear();
        SkillProgression sp = hero.getSkillProgression();
        for (WeaponClass wc : new WeaponClass[]{ WeaponClass.MELEE, WeaponClass.RANGED, WeaponClass.NECROMANCY, WeaponClass.HOLY, WeaponClass.DEFENCE }) {
            int lvl = sp.getLevel(wc);
            Label skillLabel = new Label(wc.displayName + ": " + lvl);
            skillLabel.setFont(Font.font("Monospaced", 10));
            skillLabel.setStyle("-fx-text-fill: " + (lvl > 0 ? "#4ecca3" : "#666") + ";");
            skillSection.getChildren().add(skillLabel);
        }

        // Refresh set bonuses
        setSection.getChildren().clear();
        List<String> bonuses = SetBonusManager.getActiveBonusDescriptions(hero.getEquippedItems());
        if (bonuses.isEmpty()) {
            Label none = new Label("None");
            none.setFont(Font.font("Monospaced", 10));
            none.setStyle("-fx-text-fill: #666;");
            setSection.getChildren().add(none);
        } else {
            for (String b : bonuses) {
                Label bl = new Label(b);
                bl.setFont(Font.font("Monospaced", 10));
                bl.setStyle("-fx-text-fill: #f97316;");
                setSection.getChildren().add(bl);
            }
        }
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 12));
        label.setStyle("-fx-text-fill: #eee;");
        return label;
    }
}
