import javafx.application.Application;
import javafx.stage.Stage;
import javatower.gui.GameGUI;

/**
 * Entry point for JavaTower game.
 */
public class Main {
    /**
     * Launches the JavaFX application.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        Application.launch(GameGUI.class, args);
    }
}
