package javatower.systems;

import javatower.database.DatabaseManager;
import javatower.data.GameState;
import javatower.data.SaveSlotInfo;
import javatower.entities.Hero;
import javatower.entities.Item;
import javatower.entities.Tower;
import javatower.factories.TowerFactory;
import javatower.util.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            hero.setGold(heroRs.getInt("hero_gold"));
            hero.setMana(heroRs.getInt("hero_mana"));
            hero.setMaxMana(heroRs.getInt("hero_max_mana"));
            hero.setExperience(heroRs.getInt("hero_exp"));
            hero.setPosition(heroRs.getDouble("hero_x"), heroRs.getDouble("hero_y"));

            String inventoryData = heroRs.getString("inventory_data");
            String skillTreeData = heroRs.getString("skill_tree_data");
            String skillProgressionData = heroRs.getString("skill_progression_data");

            deserializeInventory(hero, inventoryData);
            deserializeSkillTree(hero, skillTreeData);
            deserializeSkillProgression(hero, skillProgressionData);
            
            state.setHero(hero);
            state.setCurrentWave(heroRs.getInt("current_wave"));
            state.setPlaytimeSeconds(heroRs.getInt("playtime_seconds"));
            state.setInventoryJson(inventoryData);
            state.setSkillTreeJson(skillTreeData);
            state.setSkillProgressionJson(skillProgressionData);
            
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
        StringBuilder sb = new StringBuilder();
        sb.append("size,")
          .append(hero.getInventory().getWidth())
          .append(',')
          .append(hero.getInventory().getHeight())
          .append('\n');

        appendEquippedLine(sb, "weapon", hero.getWeapon());
        appendEquippedLine(sb, "offhand", hero.getOffhand());
        appendEquippedLine(sb, "helmet", hero.getHelmet());
        appendEquippedLine(sb, "chest", hero.getChest());
        appendEquippedLine(sb, "legs", hero.getLegs());
        appendEquippedLine(sb, "boots", hero.getBoots());
        appendEquippedLine(sb, "gloves", hero.getGloves());
        appendEquippedLine(sb, "amulet", hero.getAmulet());

        Item[] rings = hero.getRings();
        for (int i = 0; i < rings.length; i++) {
            appendEquippedLine(sb, "ring" + i, rings[i]);
        }

        for (Item item : hero.getInventory().getAllItems()) {
            sb.append("bag,").append(serializeItem(item)).append('\n');
        }
        return sb.toString();
    }
    
    /**
     * Serializes skill tree to JSON string.
     * TODO: Implement full serialization
     */
    private String serializeSkillTree(Hero hero) {
                StringBuilder sb = new StringBuilder();
                appendTreeLine(sb, "warrior", hero.getWarriorTree());
                appendTreeLine(sb, "paladin", hero.getPaladinTree());
                appendTreeLine(sb, "necromancer", hero.getNecromancerTree());
                appendTreeLine(sb, "pyromancer", hero.getPyromancerTree());
                appendTreeLine(sb, "archer", hero.getArcherTree());

                sb.append("special,")
                    .append(hero.getArcherMultishotCount()).append(',')
                    .append(hero.getArcherSkillRangeBonus()).append(',')
                    .append(hero.getNecroSummonBonus()).append(',')
                    .append(hero.isNecroSummonActive()).append(',')
                    .append(hero.isNecroCorpseExplosionActive()).append(',')
                    .append(hero.getPaladinHealBonus()).append(',')
                    .append(hero.getPyroFireBonus()).append(',')
                    .append(hero.isPyroElementalActive()).append(',')
                    .append(hero.getWarriorArmorBonus())
                    .append('\n');

                sb.append("skillPoints,").append(hero.getSkillPoints()).append('\n');
                return sb.toString();
    }
    
    /**
     * Serializes skill progression to JSON string.
     * TODO: Implement full serialization
     */
    private String serializeSkillProgression(Hero hero) {
        StringBuilder sb = new StringBuilder();
        for (javatower.entities.Item.WeaponClass wc : javatower.entities.Item.WeaponClass.values()) {
            if (wc == javatower.entities.Item.WeaponClass.NONE) continue;
            sb.append(wc.name())
              .append(',')
              .append(hero.getSkillProgression().getLevel(wc))
              .append(',')
              .append(hero.getSkillProgression().getXP(wc))
              .append('\n');
        }
        return sb.toString();
    }

    private void appendEquippedLine(StringBuilder sb, String slot, Item item) {
        if (item == null) return;
        sb.append("eq,").append(slot).append(',').append(serializeItem(item)).append('\n');
    }

    private void appendTreeLine(StringBuilder sb, String treeName, SkillTree tree) {
        if (tree == null) return;
        StringBuilder unlocked = new StringBuilder();
        for (SkillNode node : tree.getNodes()) {
            if (!node.isUnlocked()) continue;
            if (unlocked.length() > 0) unlocked.append(';');
            unlocked.append(node.getId());
        }
        sb.append("tree,").append(treeName).append(',').append(unlocked).append('\n');
    }

    private String serializeItem(Item item) {
        StringBuilder stats = new StringBuilder();
        for (Map.Entry<String, Integer> entry : item.getStatBonuses().entrySet()) {
            if (stats.length() > 0) stats.append(';');
            stats.append(escape(entry.getKey())).append('=').append(entry.getValue());
        }
        return joinFields(
            encode(item.getName()),
            encode(item.getDescription()),
            item.getSlot().name(),
            item.getRarity().name(),
            item.getWeaponClass().name(),
            item.getEquipmentSet().name(),
            Integer.toString(item.getWidth()),
            Integer.toString(item.getHeight()),
            Integer.toString(item.getItemLevel()),
            Integer.toString(item.getBuyPrice()),
            Integer.toString(item.getSellPrice()),
            Boolean.toString(item.isTwoHanded()),
            Integer.toString(item.getStackCount()),
            escape(stats.toString())
        );
    }

    private Item deserializeItem(String payload) {
        if (payload == null || payload.isBlank()) return null;
        String[] fields = splitFields(payload);
        if (fields.length < 14) return null;
        try {
            Item item = new Item(
                decode(fields[0]),
                decode(fields[1]),
                Item.Slot.valueOf(fields[2]),
                Item.Rarity.valueOf(fields[3]),
                Item.WeaponClass.valueOf(fields[4]),
                Item.EquipmentSet.valueOf(fields[5]),
                Integer.parseInt(fields[6]),
                Integer.parseInt(fields[7]),
                Integer.parseInt(fields[8])
            );
            item.setBuyPrice(Integer.parseInt(fields[9]));
            item.setSellPrice(Integer.parseInt(fields[10]));
            item.setTwoHanded(Boolean.parseBoolean(fields[11]));
            item.setStackCount(Integer.parseInt(fields[12]));

            String statsRaw = unescape(fields[13]);
            if (!statsRaw.isBlank()) {
                String[] parts = statsRaw.split(";");
                for (String part : parts) {
                    int eq = part.indexOf('=');
                    if (eq <= 0 || eq >= part.length() - 1) continue;
                    String key = unescape(part.substring(0, eq));
                    int value = Integer.parseInt(part.substring(eq + 1));
                    item.getStatBonuses().put(key, value);
                }
            }
            return item;
        } catch (Exception ex) {
            Logger.warn("Skipping malformed item data: %s", ex.getMessage());
            return null;
        }
    }

    private void deserializeInventory(Hero hero, String data) {
        if (data == null || data.isBlank()) return;
        String[] lines = data.split("\\n");
        for (String raw : lines) {
            if (raw == null || raw.isBlank()) continue;
            String line = raw.trim();
            if (line.startsWith("size,")) {
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {
                    int w = Integer.parseInt(parts[1]);
                    int h = Integer.parseInt(parts[2]);
                    hero.getInventory().expand(Math.max(1, w), Math.max(1, h));
                }
                continue;
            }
            if (line.startsWith("bag,")) {
                Item item = deserializeItem(line.substring(4));
                if (item != null) hero.getInventory().addItem(item);
                continue;
            }
            if (line.startsWith("eq,")) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;
                String slot = parts[1];
                Item item = deserializeItem(parts[2]);
                if (item == null) continue;
                if (slot.startsWith("ring")) {
                    hero.equipItemWithDisplaced(item);
                } else {
                    hero.equipItemWithDisplaced(item);
                }
            }
        }
    }

    private void deserializeSkillTree(Hero hero, String data) {
        if (data == null || data.isBlank()) return;
        Map<String, SkillTree> trees = new HashMap<>();
        trees.put("warrior", hero.getWarriorTree());
        trees.put("paladin", hero.getPaladinTree());
        trees.put("necromancer", hero.getNecromancerTree());
        trees.put("pyromancer", hero.getPyromancerTree());
        trees.put("archer", hero.getArcherTree());

        String[] lines = data.split("\\n");
        for (String raw : lines) {
            if (raw == null || raw.isBlank()) continue;
            String line = raw.trim();
            if (line.startsWith("tree,")) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;
                SkillTree tree = trees.get(parts[1]);
                if (tree == null || parts[2].isBlank()) continue;
                String[] ids = parts[2].split(";");
                for (String id : ids) {
                    for (SkillNode node : tree.getNodes()) {
                        if (node.getId().equals(id)) {
                            node.setUnlocked(true);
                            break;
                        }
                    }
                }
                continue;
            }
            if (line.startsWith("special,")) {
                String[] p = line.split(",");
                if (p.length >= 10) {
                    hero.setArcherMultishotCount(Integer.parseInt(p[1]));
                    hero.setArcherSkillRangeBonus(Double.parseDouble(p[2]));
                    hero.setNecroSummonBonus(Double.parseDouble(p[3]));
                    hero.setNecroSummonActive(Boolean.parseBoolean(p[4]));
                    hero.setNecroCorpseExplosionActive(Boolean.parseBoolean(p[5]));
                    hero.setPaladinHealBonus(Double.parseDouble(p[6]));
                    hero.setPyroFireBonus(Double.parseDouble(p[7]));
                    hero.setPyroElementalActive(Boolean.parseBoolean(p[8]));
                    hero.setWarriorArmorBonus(Integer.parseInt(p[9]));
                }
                continue;
            }
            if (line.startsWith("skillPoints,")) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    hero.setSkillPoints(Integer.parseInt(parts[1]));
                }
            }
        }
    }

    private void deserializeSkillProgression(Hero hero, String data) {
        if (data == null || data.isBlank()) return;
        String[] lines = data.split("\\n");
        for (String raw : lines) {
            if (raw == null || raw.isBlank()) continue;
            String[] parts = raw.split(",", 3);
            if (parts.length < 3) continue;
            try {
                Item.WeaponClass wc = Item.WeaponClass.valueOf(parts[0]);
                int level = Integer.parseInt(parts[1]);
                double xp = Double.parseDouble(parts[2]);
                hero.getSkillProgression().setSkillState(wc, level, xp);
            } catch (Exception ex) {
                Logger.warn("Skipping malformed skill progression row: %s", raw);
            }
        }
    }

    private String joinFields(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(fields[i] == null ? "" : fields[i]);
        }
        return sb.toString();
    }

    private String[] splitFields(String payload) {
        return payload.split("\\|", -1);
    }

    private String encode(String raw) {
        if (raw == null) return "";
        return Base64.getEncoder().encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decode(String encoded) {
        if (encoded == null || encoded.isBlank()) return "";
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return encoded;
        }
    }

    private String escape(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|").replace(";", "\\;").replace("=", "\\=").replace(",", "\\,");
    }

    private String unescape(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaping) {
                out.append(c);
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                out.append(c);
            }
        }
        if (escaping) out.append('\\');
        return out.toString();
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
