package javatower.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Combat log panel showing recent game events.
 * Displays kill notifications, damage, and other combat info.
 */
public class CombatLogPanel extends VBox {
    
    private static final int MAX_ENTRIES = 6;
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private List<Label> logEntries;
    
    public CombatLogPanel() {
        setSpacing(3);
        setPadding(new Insets(8));
        setStyle("-fx-background-color: rgba(10, 10, 30, 0.85); -fx-border-color: #533483; -fx-border-width: 1;");
        setPrefWidth(280);
        setMaxHeight(140);
        
        Label header = new Label("⚔ COMBAT LOG");
        header.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: #e94560;");
        
        logEntries = new ArrayList<>();
        
        getChildren().add(header);
        
        // Initialize empty slots
        for (int i = 0; i < MAX_ENTRIES; i++) {
            Label entry = createLogEntry("");
            logEntries.add(entry);
            getChildren().add(entry);
        }
    }
    
    private Label createLogEntry(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 14));
        label.setStyle("-fx-text-fill: #aaa;");
        label.setWrapText(false);
        return label;
    }
    
    /**
     * Adds a new log entry with timestamp.
     */
    public void addEntry(String message) {
        String timestamp = LocalTime.now().format(timeFormat);
        String fullMessage = String.format("[%s] %s", timestamp, message);
        
        // Shift all entries down
        for (int i = logEntries.size() - 1; i > 0; i--) {
            logEntries.get(i).setText(logEntries.get(i - 1).getText());
            logEntries.get(i).setStyle(logEntries.get(i - 1).getStyle());
        }
        
        // Add new entry at top
        logEntries.get(0).setText(fullMessage);
        logEntries.get(0).setStyle("-fx-text-fill: #fff;");
        
        // Fade older entries
        for (int i = 1; i < logEntries.size(); i++) {
            double opacity = Math.max(0.3, 1.0 - (i * 0.15));
            String color = String.format("#%02x%02x%02x", 
                (int)(170 * opacity), (int)(170 * opacity), (int)(170 * opacity));
            logEntries.get(i).setStyle("-fx-text-fill: " + color + ";");
        }
    }
    
    /**
     * Logs a kill event.
     */
    public void logKill(String enemyName, int xp, int gold) {
        addEntry(String.format("☠ Killed %s (+%d XP, +%dg)", enemyName, xp, gold));
    }
    
    /**
     * Logs damage dealt.
     */
    public void logDamage(String target, int damage, boolean crit) {
        if (crit) {
            addEntry(String.format("💥 CRIT %s for %d damage!", target, damage));
        } else {
            addEntry(String.format("⚔ Hit %s for %d damage", target, damage));
        }
    }
    
    /**
     * Logs a tower action.
     */
    public void logTower(String towerType, String action) {
        addEntry(String.format("🏹 %s %s", towerType, action));
    }
    
    /**
     * Logs player level up.
     */
    public void logLevelUp(int newLevel) {
        addEntry(String.format("🆙 LEVEL UP! Now level %d", newLevel));
    }
    
    /**
     * Logs wave start.
     */
    public void logWaveStart(int wave) {
        addEntry(String.format("🌊 Wave %d started!", wave));
    }
    
    /**
     * Clears all log entries.
     */
    public void clear() {
        for (Label entry : logEntries) {
            entry.setText("");
        }
    }
}
