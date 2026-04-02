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
 * Side-panel widget that displays the hero’s stats, bars, weapon-class
 * skill levels, and active equipment-set bonuses.
 * <p>
 * Refreshed every frame by {@link GameGUI#refreshPanels()} to keep
 * HP / mana / XP bars and stat numbers up-to-date.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see Hero
 * @see SkillProgression
 * @see SetBonusManager
 */
public class HeroPanel extends VBox {
    private Hero hero;
    private Label nameLabel, levelLabel, goldLabel;
    private Label atkLabel, defLabel, critLabel, spdLabel, manaLabel;
    private ProgressBar hpBar, xpBar, manaBar;
    private VBox skillSection, setSection;

    public HeroPanel(Hero hero) {
        this.hero = hero;
        setSpacing(4);
        setPadding(new Insets(8));
        setStyle("-fx-background-color: #1b263b; -fx-border-color: #415a77; -fx-border-width: 1; -fx-border-radius: 4;");

        Label header = new Label("⚔️ HERO");
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
        hpBar.setPrefHeight(12);
        hpBar.setStyle("-fx-accent: #48bb78;");

        manaBar = new ProgressBar(1.0);
        manaBar.setPrefWidth(200);
        manaBar.setPrefHeight(8);
        manaBar.setStyle("-fx-accent: #4299e1;");

        xpBar = new ProgressBar(0);
        xpBar.setPrefWidth(200);
        xpBar.setPrefHeight(8);
        xpBar.setStyle("-fx-accent: #9f7aea;");

        Label hpLabel = createStatLabel("HP:");
        Label xpLabel = createStatLabel("XP:");
        Label mpLabel = createStatLabel("MP:");

        // Skill progression section
        Label skillHeader = new Label("📚 SKILLS");
        skillHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        skillHeader.setStyle("-fx-text-fill: #a855f7;");
        skillSection = new VBox(1);

        // Set bonus section
        Label setHeader = new Label("🎁 SET BONUSES");
        setHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        setHeader.setStyle("-fx-text-fill: #f97316;");
        setSection = new VBox(1);

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

        int baseAtk = hero.getAttack();
        int effAtk = hero.getEffectiveAttack();
        atkLabel.setText("ATK: " + effAtk + (effAtk != baseAtk ? " (+" + (effAtk - baseAtk) + ")" : ""));

        int baseDef = hero.getDefence();
        int effDef = hero.getEffectiveDefence();
        defLabel.setText("DEF: " + effDef + (effDef != baseDef ? " (+" + (effDef - baseDef) + ")" : ""));

        int baseCrit = hero.getCritChance();
        int effCrit = hero.getEffectiveCritChance();
        critLabel.setText("CRIT: " + effCrit + "%" + (effCrit != baseCrit ? " (+" + (effCrit - baseCrit) + ")" : ""));

        double cooldown = hero.getEffectiveCooldown();
        spdLabel.setText("SPD: " + String.format("%.2fs", cooldown));

        manaLabel.setText("Skill Pts: " + hero.getSkillPoints());

        int effMaxHP = hero.getEffectiveMaxHealth();
        double hpPct = effMaxHP > 0 ? (double) hero.getCurrentHealth() / effMaxHP : 0;
        hpBar.setProgress(hpPct);
        hpBar.setStyle(hpPct > 0.5 ? "-fx-accent: limegreen;" : hpPct > 0.25 ? "-fx-accent: yellow;" : "-fx-accent: red;");

        int effMaxMana = hero.getEffectiveMaxMana();
        double manaPct = effMaxMana > 0 ? (double) hero.getMana() / effMaxMana : 0;
        manaBar.setProgress(manaPct);

        double xpPct = hero.getExperienceToNextLevel() > 0 ? (double) hero.getExperience() / hero.getExperienceToNextLevel() : 0;
        xpBar.setProgress(xpPct);

        // Refresh skill levels
        skillSection.getChildren().clear();
        SkillProgression sp = hero.getSkillProgression();
        for (WeaponClass wc : new WeaponClass[]{ WeaponClass.MELEE, WeaponClass.RANGED, WeaponClass.NECROMANCY, WeaponClass.HOLY, WeaponClass.DEFENCE }) {
            int lvl = sp.getLevel(wc);
            Label skillLabel = new Label(wc.displayName.substring(0, Math.min(4, wc.displayName.length())) + ": " + lvl);
            skillLabel.setFont(Font.font("Monospaced", 10));
            skillLabel.setStyle("-fx-text-fill: " + (lvl > 0 ? "#4ecca3" : "#778da9") + ";");
            skillSection.getChildren().add(skillLabel);
        }

        // Refresh set bonuses
        setSection.getChildren().clear();
        List<String> bonuses = SetBonusManager.getActiveBonusDescriptions(hero.getEquippedItems());
        if (bonuses.isEmpty()) {
            Label none = new Label("None");
            none.setFont(Font.font("Monospaced", 10));
            none.setStyle("-fx-text-fill: #778da9;");
            setSection.getChildren().add(none);
        } else {
            for (String b : bonuses) {
                Label bl = new Label(b.length() > 25 ? b.substring(0, 25) + "..." : b);
                bl.setFont(Font.font("Monospaced", 9));
                bl.setStyle("-fx-text-fill: #f97316;");
                setSection.getChildren().add(bl);
            }
        }
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 11));
        label.setStyle("-fx-text-fill: #e0e1dd;");
        return label;
    }
}
