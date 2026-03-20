package javatower.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javatower.systems.WaveManager;
import javatower.util.Constants;

/**
 * Displays wave info and progression.
 */
public class WaveInfoPanel extends VBox {
    private WaveManager waveManager;
    private Label waveLabel, enemyCountLabel, bossLabel, progressLabel;
    private ProgressBar waveProgress;

    public WaveInfoPanel(WaveManager waveManager) {
        this.waveManager = waveManager;
        setSpacing(6);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #533483; -fx-border-width: 1;");

        Label header = new Label("WAVE");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        header.setStyle("-fx-text-fill: #e94560;");

        waveLabel = createLabel("");
        enemyCountLabel = createLabel("");
        bossLabel = createLabel("");
        progressLabel = createLabel("");

        waveProgress = new ProgressBar(0);
        waveProgress.setPrefWidth(200);
        waveProgress.setStyle("-fx-accent: #e94560;");

        getChildren().addAll(header, waveLabel, enemyCountLabel, bossLabel, progressLabel, waveProgress);
        refresh();
    }

    public void refresh() {
        if (waveManager == null) return;
        int current = waveManager.getCurrentWave();
        waveLabel.setText("Wave: " + current + " / " + Constants.MAX_WAVES);
        enemyCountLabel.setText("Enemies: " + waveManager.getActiveEnemies().size());
        bossLabel.setText(waveManager.isBossWave() ? "*** BOSS WAVE ***" : "");
        bossLabel.setStyle(waveManager.isBossWave() ? "-fx-text-fill: #e94560; -fx-font-weight: bold;" : "-fx-text-fill: transparent;");
        progressLabel.setText("Progress: " + (int)((current * 100.0) / Constants.MAX_WAVES) + "%");
        waveProgress.setProgress((double) current / Constants.MAX_WAVES);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 12));
        label.setStyle("-fx-text-fill: #eee;");
        return label;
    }
}
