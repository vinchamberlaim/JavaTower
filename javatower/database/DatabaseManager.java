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
        boolean isFirstRun = !new java.io.File("javatower.db").exists();
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
            
            if (isFirstRun) {
                System.out.println("[JavaTower] Database created successfully!");
            }
        } catch (SQLException e) {
            System.err.println("[JavaTower] Database error: " + e.getMessage());
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

    // ========== NEW SAVE SYSTEM (K9: End-of-Wave Save) ==========

    /**
     * Initializes the new save system tables.
     * Call this after initialize() to ensure new tables exist.
     */
    public void initializeNewSaveSystem() {
        if (connection == null) return;
        try {
            Statement stmt = connection.createStatement();
            
            // Main save slots table (0 = auto-save, 1-3 = manual slots)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS save_slots ("
                + "slot_id INTEGER PRIMARY KEY,"
                + "hero_name TEXT,"
                + "hero_level INTEGER DEFAULT 1,"
                + "hero_hp INTEGER DEFAULT 100,"
                + "hero_max_hp INTEGER DEFAULT 100,"
                + "hero_attack INTEGER DEFAULT 10,"
                + "hero_defence INTEGER DEFAULT 5,"
                + "hero_gold INTEGER DEFAULT 50,"
                + "hero_mana INTEGER DEFAULT 50,"
                + "hero_max_mana INTEGER DEFAULT 50,"
                + "hero_exp INTEGER DEFAULT 0,"
                + "hero_x REAL DEFAULT 100,"
                + "hero_y REAL DEFAULT 320,"
                + "current_wave INTEGER DEFAULT 1,"
                + "playtime_seconds INTEGER DEFAULT 0,"
                + "save_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "inventory_data TEXT,"
                + "skill_tree_data TEXT,"
                + "skill_progression_data TEXT)");
            
            // Tower placements for each save slot
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS save_towers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "slot_id INTEGER,"
                + "tower_type TEXT,"
                + "grid_x INTEGER,"
                + "grid_y INTEGER,"
                + "upgrade_level INTEGER DEFAULT 0,"
                + "FOREIGN KEY (slot_id) REFERENCES save_slots(slot_id) ON DELETE CASCADE)");
            
            // Metadata for fast UI loading
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS save_metadata ("
                + "slot_id INTEGER PRIMARY KEY,"
                + "last_wave INTEGER DEFAULT 1,"
                + "last_save_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "hero_level INTEGER DEFAULT 1,"
                + "playtime_seconds INTEGER DEFAULT 0)");
            
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves hero data to a specific slot (0 = auto-save, 1-3 = manual).
     * Returns true on success.
     */
    public boolean saveHeroToSlot(int slotId, Hero hero, int wave, int playtimeSeconds,
                                   String inventoryJson, String skillTreeJson, String skillProgressionJson) {
        if (connection == null) return false;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO save_slots (slot_id, hero_name, hero_level, hero_hp, hero_max_hp, "
                + "hero_attack, hero_defence, hero_gold, hero_mana, hero_max_mana, hero_exp, hero_x, hero_y, "
                + "current_wave, playtime_seconds, inventory_data, skill_tree_data, skill_progression_data) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            ps.setInt(1, slotId);
            ps.setString(2, hero.getName());
            ps.setInt(3, hero.getLevel());
            ps.setInt(4, hero.getCurrentHealth());
            ps.setInt(5, hero.getMaxHealth());
            ps.setInt(6, hero.getAttack());
            ps.setInt(7, hero.getDefence());
            ps.setInt(8, hero.getGold());
            ps.setInt(9, hero.getMana());
            ps.setInt(10, hero.getMaxMana());
            ps.setInt(11, hero.getExperience());
            ps.setDouble(12, hero.getX());
            ps.setDouble(13, hero.getY());
            ps.setInt(14, wave);
            ps.setInt(15, playtimeSeconds);
            ps.setString(16, inventoryJson);
            ps.setString(17, skillTreeJson);
            ps.setString(18, skillProgressionJson);
            
            ps.executeUpdate();
            ps.close();
            
            // Update metadata
            updateSaveMetadata(slotId, wave, hero.getLevel(), playtimeSeconds);
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a tower placement for a specific slot.
     */
    public boolean saveTowerToSlot(int slotId, String towerType, int gridX, int gridY, int upgradeLevel) {
        if (connection == null) return false;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO save_towers (slot_id, tower_type, grid_x, grid_y, upgrade_level) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, slotId);
            ps.setString(2, towerType);
            ps.setInt(3, gridX);
            ps.setInt(4, gridY);
            ps.setInt(5, upgradeLevel);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clears all towers for a slot before saving new ones.
     */
    public boolean clearTowersForSlot(int slotId) {
        if (connection == null) return false;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM save_towers WHERE slot_id = ?");
            ps.setInt(1, slotId);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates metadata for a save slot.
     */
    private void updateSaveMetadata(int slotId, int wave, int heroLevel, int playtimeSeconds) {
        if (connection == null) return;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO save_metadata (slot_id, last_wave, last_save_time, hero_level, playtime_seconds) "
                + "VALUES (?, ?, datetime('now'), ?, ?)");
            ps.setInt(1, slotId);
            ps.setInt(2, wave);
            ps.setInt(3, heroLevel);
            ps.setInt(4, playtimeSeconds);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets metadata for all save slots (0-3).
     */
    public ResultSet getAllSaveMetadata() {
        if (connection == null) return null;
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(
                "SELECT slot_id, last_wave, last_save_time, hero_level, playtime_seconds "
                + "FROM save_metadata ORDER BY slot_id");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads hero data from a specific slot.
     * Returns null if no save exists in that slot.
     */
    public ResultSet loadHeroFromSlot(int slotId) {
        if (connection == null) return null;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM save_slots WHERE slot_id = ?");
            ps.setInt(1, slotId);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads all towers for a specific slot.
     */
    public ResultSet loadTowersFromSlot(int slotId) {
        if (connection == null) return null;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT tower_type, grid_x, grid_y, upgrade_level FROM save_towers WHERE slot_id = ?");
            ps.setInt(1, slotId);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a save slot exists.
     */
    public boolean slotExists(int slotId) {
        if (connection == null) return false;
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM save_slots WHERE slot_id = ?");
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            ps.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a save slot and its towers.
     */
    public boolean deleteSlot(int slotId) {
        if (connection == null) return false;
        try {
            // Towers will be deleted via CASCADE
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM save_slots WHERE slot_id = ?");
            ps.setInt(1, slotId);
            ps.executeUpdate();
            ps.close();
            
            // Also delete metadata
            ps = connection.prepareStatement("DELETE FROM save_metadata WHERE slot_id = ?");
            ps.setInt(1, slotId);
            ps.executeUpdate();
            ps.close();
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Formats playtime seconds to readable string (e.g., "2h 15m" or "45m 30s").
     */
    public static String formatPlaytime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
