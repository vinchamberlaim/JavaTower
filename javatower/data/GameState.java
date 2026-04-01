package javatower.data;

import javatower.entities.Hero;
import javatower.entities.Tower;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object containing all game state information for save/load.
 * This is a serializable representation of the game state without live game objects.
 */
public class GameState {
    
    private Hero hero;  // Live hero object (will be reconstructed on load)
    private List<TowerData> towers;  // Tower placement data
    private int currentWave;
    private int playtimeSeconds;
    private String inventoryJson;
    private String skillTreeJson;
    private String skillProgressionJson;
    
    /**
     * Inner class representing tower data without live object references.
     */
    public static class TowerData {
        public javatower.entities.Tower.TowerType type;
        public int gridX;
        public int gridY;
        public int upgradeLevel;
        
        public TowerData(javatower.entities.Tower.TowerType type, int gridX, int gridY, int upgradeLevel) {
            this.type = type;
            this.gridX = gridX;
            this.gridY = gridY;
            this.upgradeLevel = upgradeLevel;
        }
        
        @Override
        public String toString() {
            return String.format("TowerData[%s at (%d,%d) Lv.%d]", 
                type, gridX, gridY, upgradeLevel);
        }
    }
    
    public GameState() {
        this.towers = new ArrayList<>();
        this.currentWave = 1;
        this.playtimeSeconds = 0;
    }
    
    // Factory method from live game objects
    public static GameState fromLiveGame(Hero hero, List<Tower> liveTowers, 
                                         int currentWave, int playtimeSeconds) {
        GameState state = new GameState();
        state.hero = hero;
        state.currentWave = currentWave;
        state.playtimeSeconds = playtimeSeconds;
        
        // Convert live towers to data
        for (Tower tower : liveTowers) {
            // Calculate grid position from pixel position
            int[] grid = pixelToGrid(tower.getX(), tower.getY());
            TowerData data = new TowerData(
                tower.getType(),
                grid[0],
                grid[1],
                tower.getUpgradeLevel()
            );
            state.towers.add(data);
        }
        
        return state;
    }
    
    /**
     * Converts pixel coordinates to grid coordinates.
     */
    private static int[] pixelToGrid(double pixelX, double pixelY) {
        int gridX = (int)(pixelX / javatower.util.Constants.TILE_SIZE);
        int gridY = (int)(pixelY / javatower.util.Constants.TILE_SIZE);
        return new int[]{gridX, gridY};
    }
    
    // Getters and setters
    public Hero getHero() { return hero; }
    public void setHero(Hero hero) { this.hero = hero; }
    
    public List<TowerData> getTowers() { return towers; }
    public void setTowers(List<TowerData> towers) { this.towers = towers; }
    
    public int getCurrentWave() { return currentWave; }
    public void setCurrentWave(int currentWave) { this.currentWave = currentWave; }
    
    public int getPlaytimeSeconds() { return playtimeSeconds; }
    public void setPlaytimeSeconds(int playtimeSeconds) { this.playtimeSeconds = playtimeSeconds; }
    
    public String getInventoryJson() { return inventoryJson; }
    public void setInventoryJson(String inventoryJson) { this.inventoryJson = inventoryJson; }
    
    public String getSkillTreeJson() { return skillTreeJson; }
    public void setSkillTreeJson(String skillTreeJson) { this.skillTreeJson = skillTreeJson; }
    
    public String getSkillProgressionJson() { return skillProgressionJson; }
    public void setSkillProgressionJson(String skillProgressionJson) { this.skillProgressionJson = skillProgressionJson; }
    
    /**
     * Returns number of towers in this save.
     */
    public int getTowerCount() {
        return towers != null ? towers.size() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("GameState[wave=%d, towers=%d, playtime=%ds]", 
            currentWave, getTowerCount(), playtimeSeconds);
    }
}
