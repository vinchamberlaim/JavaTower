package javatower.systems;

import javatower.database.DatabaseManager;
import javatower.data.GameState;
import javatower.data.SaveSlotInfo;
import javatower.entities.Hero;
import javatower.entities.Tower;
import javatower.factories.TowerFactory;
import javatower.util.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages saving and loading game state to/from database.
 * Supports multiple save slots (0 = auto-save, 1-3 = manual slots).
 * Persists hero, inventory, skill trees, and tower placements.
 */
public class SaveGameManager {
    
    public static final int AUTO_SAVE_SLOT = 0;
    public static final int MAX_SLOTS = 3;
    public static final int[] ALL_SLOTS = {0, 1, 2, 3};
    
    private final DatabaseManager db;
    
    public SaveGameManager() {
        this.db = DatabaseManager.getInstance();
        // Ensure new tables exist
        db.initializeNewSaveSystem();
    }
    
    /**
     * Saves current game state to specified slot (0 = auto, 1-3 = manual).
     * 
     * @param slotId Slot ID (0 for auto-save, 1-3 for manual)
     * @param hero Hero to save
     * @param towers List of placed towers
     * @param currentWave Current wave number
     * @param playtimeSeconds Total playtime in seconds
     * @return true if save successful
     */
    public boolean saveToSlot(int slotId, Hero hero, List<Tower> towers, 
                              int currentWave, int playtimeSeconds) {
        if (slotId < 0 || slotId > MAX_SLOTS) {
            Logger.error("Invalid save slot: %d", slotId);
            return false;
        }
        
        try {
            // Serialize complex data to JSON (simplified for now)
            String inventoryJson = serializeInventory(hero);
            String skillTreeJson = serializeSkillTree(hero);
            String skillProgressionJson = serializeSkillProgression(hero);
            
            // Save hero data
            boolean heroSaved = db.saveHeroToSlot(slotId, hero, currentWave, playtimeSeconds,
                inventoryJson, skillTreeJson, skillProgressionJson);
            
            if (!heroSaved) {
                Logger.error("Failed to save hero data to slot %d", slotId);
                return false;
            }
            
            // Clear old towers and save new ones
            db.clearTowersForSlot(slotId);
            
            for (Tower tower : towers) {
                // Convert pixel position to grid position
                int[] grid = pixelToGrid(tower.getX(), tower.getY());
                db.saveTowerToSlot(slotId, tower.getType().name(), 
                    grid[0], grid[1], tower.getUpgradeLevel());
            }
            
            Logger.info("Saved game to slot %d (Wave %d, %d towers)", 
                slotId, currentWave, towers.size());
            return true;
            
        } catch (Exception e) {
            Logger.error("Exception during save to slot %d: %s", slotId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Auto-save convenience method (uses slot 0).
     */
    public boolean autoSave(Hero hero, List<Tower> towers, int currentWave, int playtimeSeconds) {
        return saveToSlot(AUTO_SAVE_SLOT, hero, towers, currentWave, playtimeSeconds);
    }
    
    /**
     * Loads game state from specified slot.
     * 
     * @param slotId Slot to load from
     * @return GameState object, or null if load fails
     */
    public GameState loadFromSlot(int slotId) {
        if (slotId < 0 || slotId > MAX_SLOTS) {
            Logger.error("Invalid load slot: %d", slotId);
            return null;
        }
        
        if (!db.slotExists(slotId)) {
            Logger.warn("No save exists in slot %d", slotId);
            return null;
        }
        
        try {
            GameState state = new GameState();
            
            // Load hero data
            ResultSet heroRs = db.loadHeroFromSlot(slotId);
            if (heroRs == null || !heroRs.next()) {
                Logger.error("Failed to load hero data from slot %d", slotId);
                return null;
            }
            
            Hero hero = new Hero(heroRs.getString("hero_name"));
            hero.setLevel(heroRs.getInt("hero_level"));
            hero.setMaxHealth(heroRs.getInt("hero_max_hp"));
            hero.setCurrentHealth(heroRs.getInt("hero_hp"));
            hero.setAttack(heroRs.getInt("hero_attack"));
            hero.setDefence(heroRs.getInt("hero_defence"));
            // TODO: Load gold, mana, exp
            hero.setPosition(heroRs.getDouble("hero_x"), heroRs.getDouble("hero_y"));
            
            state.setHero(hero);
            state.setCurrentWave(heroRs.getInt("current_wave"));
            state.setPlaytimeSeconds(heroRs.getInt("playtime_seconds"));
            
            heroRs.close();
            
            // Load tower data
            List<GameState.TowerData> towerDataList = new ArrayList<>();
            ResultSet towerRs = db.loadTowersFromSlot(slotId);
            while (towerRs != null && towerRs.next()) {
                String typeStr = towerRs.getString("tower_type");
                Tower.TowerType type = Tower.TowerType.valueOf(typeStr);
                int gridX = towerRs.getInt("grid_x");
                int gridY = towerRs.getInt("grid_y");
                int upgradeLevel = towerRs.getInt("upgrade_level");
                
                towerDataList.add(new GameState.TowerData(type, gridX, gridY, upgradeLevel));
            }
            if (towerRs != null) towerRs.close();
            
            state.setTowers(towerDataList);
            
            Logger.info("Loaded game from slot %d (Wave %d, %d towers)",
                slotId, state.getCurrentWave(), towerDataList.size());
            return state;
            
        } catch (SQLException e) {
            Logger.error("SQLException during load from slot %d: %s", slotId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads auto-save (convenience method).
     */
    public GameState loadAutoSave() {
        return loadFromSlot(AUTO_SAVE_SLOT);
    }
    
    /**
     * Gets metadata for all save slots (for UI browser).
     * 
     * @return List of SaveSlotInfo for slots 0-3
     */
    public List<SaveSlotInfo> getAllSlotInfo() {
        List<SaveSlotInfo> infos = new ArrayList<>();
        
        // Initialize all slots as empty
        for (int i = 0; i <= MAX_SLOTS; i++) {
            infos.add(new SaveSlotInfo(i));
        }
        
        // Fill in metadata where saves exist
        try {
            ResultSet rs = db.getAllSaveMetadata();
            if (rs != null) {
                while (rs.next()) {
                    int slotId = rs.getInt("slot_id");
                    if (slotId >= 0 && slotId <= MAX_SLOTS) {
                        SaveSlotInfo info = new SaveSlotInfo(
                            slotId,
                            "Hero", // Hero name not in metadata, would need separate query
                            rs.getInt("hero_level"),
                            rs.getInt("last_wave"),
                            rs.getInt("playtime_seconds"),
                            rs.getTimestamp("last_save_time").toLocalDateTime()
                        );
                        infos.set(slotId, info);
                    }
                }
                rs.close();
            }
        } catch (SQLException e) {
            Logger.error("Failed to load save metadata: %s", e.getMessage());
        }
        
        return infos;
    }
    
    /**
     * Deletes a save slot.
     * 
     * @param slotId Slot to delete
     * @return true if deleted successfully
     */
    public boolean deleteSlot(int slotId) {
        boolean result = db.deleteSlot(slotId);
        if (result) {
            Logger.info("Deleted save slot %d", slotId);
        } else {
            Logger.error("Failed to delete save slot %d", slotId);
        }
        return result;
    }
    
    /**
     * Checks if a save exists in the specified slot.
     */
    public boolean slotExists(int slotId) {
        return db.slotExists(slotId);
    }
    
    /**
     * Checks if auto-save exists.
     */
    public boolean hasAutoSave() {
        return slotExists(AUTO_SAVE_SLOT);
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Converts pixel coordinates to grid coordinates.
     */
    private int[] pixelToGrid(double pixelX, double pixelY) {
        int gridX = (int)(pixelX / javatower.util.Constants.TILE_SIZE);
        int gridY = (int)(pixelY / javatower.util.Constants.TILE_SIZE);
        return new int[]{gridX, gridY};
    }
    
    /**
     * Serializes inventory to JSON string.
     * TODO: Implement full serialization
     */
    private String serializeInventory(Hero hero) {
        // Placeholder - would serialize actual inventory items
        return "{}";
    }
    
    /**
     * Serializes skill tree to JSON string.
     * TODO: Implement full serialization
     */
    private String serializeSkillTree(Hero hero) {
        // Placeholder - would serialize unlocked nodes
        return "{}";
    }
    
    /**
     * Serializes skill progression to JSON string.
     * TODO: Implement full serialization
     */
    private String serializeSkillProgression(Hero hero) {
        // Placeholder - would serialize weapon skill XP
        return "{}";
    }
    
    /**
     * Reconstructs live Tower objects from GameState.
     * Call this after loading to populate the game world.
     */
    public List<Tower> rebuildTowers(GameState state) {
        List<Tower> towers = new ArrayList<>();
        
        for (GameState.TowerData data : state.getTowers()) {
            Tower tower = TowerFactory.createTower(data.type, data.gridX, data.gridY);
            if (tower != null) {
                // Apply upgrades
                for (int i = 0; i < data.upgradeLevel; i++) {
                    tower.upgrade();
                }
                towers.add(tower);
            }
        }
        
        return towers;
    }
}
