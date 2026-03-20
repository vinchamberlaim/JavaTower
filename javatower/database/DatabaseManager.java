package javatower.database;

import java.sql.*;
import javatower.entities.Hero;

/**
 * Singleton for managing SQLite persistence.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initialize();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initializes the database and tables if not exist.
     */
    public void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:javatower.db");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS meta_progression ("
                    + "id INTEGER PRIMARY KEY, unlocked_items TEXT, skill_unlocks TEXT, "
                    + "max_wave INT, total_gold INT, inventory_width INT, inventory_height INT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS save_state ("
                    + "id INTEGER PRIMARY KEY, hero_name TEXT, hero_level INT, hero_hp INT, hero_max_hp INT, "
                    + "hero_attack INT, hero_defence INT, hero_gold INT, hero_mana INT, hero_max_mana INT, "
                    + "hero_exp INT, hero_x INT, hero_y INT, wave INT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current game state.
     */
    public void saveGame(Hero hero, int wave) {
        if (connection == null) return;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO save_state (id, hero_name, hero_level, hero_hp, hero_max_hp, "
                    + "hero_attack, hero_defence, hero_gold, hero_mana, hero_max_mana, hero_exp, hero_x, hero_y, wave) "
                    + "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, hero.getName());
            ps.setInt(2, hero.getLevel());
            ps.setInt(3, hero.getCurrentHealth());
            ps.setInt(4, hero.getMaxHealth());
            ps.setInt(5, hero.getAttack());
            ps.setInt(6, hero.getDefence());
            ps.setInt(7, hero.getGold());
            ps.setInt(8, hero.getMana());
            ps.setInt(9, hero.getMaxMana());
            ps.setInt(10, hero.getExperience());
            ps.setInt(11, (int) hero.getX());
            ps.setInt(12, (int) hero.getY());
            ps.setInt(13, wave);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a saved hero from the database. Returns null if no save exists.
     */
    public Hero loadGame() {
        if (connection == null) return null;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM save_state WHERE id = 1");
            if (rs.next()) {
                Hero hero = new Hero(rs.getString("hero_name"));
                hero.setMaxHealth(rs.getInt("hero_max_hp"));
                hero.setCurrentHealth(rs.getInt("hero_hp"));
                hero.setAttack(rs.getInt("hero_attack"));
                hero.setDefence(rs.getInt("hero_defence"));
                hero.setPosition(rs.getInt("hero_x"), rs.getInt("hero_y"));
                rs.close();
                stmt.close();
                return hero;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the saved wave number, or 1 if none.
     */
    public int loadWave() {
        if (connection == null) return 1;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT wave FROM save_state WHERE id = 1");
            if (rs.next()) {
                int wave = rs.getInt("wave");
                rs.close();
                stmt.close();
                return wave;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Saves meta-progression data.
     */
    public void saveMetaProgression(int maxWave, int totalGold, int invWidth, int invHeight) {
        if (connection == null) return;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO meta_progression (id, max_wave, total_gold, inventory_width, inventory_height) "
                    + "VALUES (1, ?, ?, ?, ?)");
            ps.setInt(1, maxWave);
            ps.setInt(2, totalGold);
            ps.setInt(3, invWidth);
            ps.setInt(4, invHeight);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the maximum wave reached.
     */
    public void updateMaxWave(int wave) {
        if (connection == null) return;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE meta_progression SET max_wave = MAX(max_wave, ?) WHERE id = 1");
            ps.setInt(1, wave);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns whether a save file exists.
     */
    public boolean hasSaveFile() {
        if (connection == null) return false;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM save_state WHERE id = 1");
            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes the save file.
     */
    public void deleteSave() {
        if (connection == null) return;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM save_state WHERE id = 1");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() { return connection; }
}
