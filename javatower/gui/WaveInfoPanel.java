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
    private Label waveLabel, enemyCountLabel, bossLabel, progressLabel, countdownLabel;
    private ProgressBar waveProgress;
    private double countdownValue = 0;
    private boolean showCountdown = false;

    public WaveInfoPanel(WaveManager waveManager) {
        this.waveManager = waveManager;
        setSpacing(6);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #533483; -fx-border-width: 1;");

        Label header = new Label("WAVE");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        header.setStyle("-fx-text-fill: #e94560;");

        waveLabel = createLabel("");
        enemyCountLabel = createLabel("");
        bossLabel = createLabel("");
        progressLabel = createLabel("");
        countdownLabel = createLabel("");
        countdownLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        countdownLabel.setStyle("-fx-text-fill: #4ecca3;");
        countdownLabel.setVisible(false);

        waveProgress = new ProgressBar(0);
        waveProgress.setPrefWidth(260);
        waveProgress.setStyle("-fx-accent: #e94560;");

        getChildren().addAll(header, waveLabel, enemyCountLabel, bossLabel, progressLabel, countdownLabel, waveProgress);
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
        label.setFont(Font.font("Monospaced", 16));
        label.setStyle("-fx-text-fill: #eee;");
        return label;
    }
    
    /**
     * Starts the wave countdown timer display.
     * @param seconds Total seconds to count down from
     */
    public void startCountdown(double seconds) {
        this.countdownValue = seconds;
        this.showCountdown = true;
        countdownLabel.setVisible(true);
        updateCountdownDisplay();
    }
    
    /**
     * Updates the countdown timer. Call every frame with delta time.
     * @param dt Delta time in seconds
     * @return true if countdown reached zero
     */
    public boolean updateCountdown(double dt) {
        if (!showCountdown) return false;
        
        countdownValue -= dt;
        if (countdownValue <= 0) {
            countdownValue = 0;
            showCountdown = false;
            countdownLabel.setVisible(false);
            return true; // Countdown complete
        }
        
        updateCountdownDisplay();
        return false;
    }
    
    /**
     * Stops and hides the countdown.
     */
    public void stopCountdown() {
        showCountdown = false;
        countdownValue = 0;
        countdownLabel.setVisible(false);
    }
    
    private void updateCountdownDisplay() {
        int wholeSeconds = (int) Math.ceil(countdownValue);
        countdownLabel.setText(String.format("⏱ Next wave in: %d...", wholeSeconds));
    }
    
    /**
     * Returns true if countdown is currently active.
     */
    public boolean isCountdownActive() {
        return showCountdown;
    }
}
