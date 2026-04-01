package javatower.data;

import javatower.database.DatabaseManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data class representing information about a save slot.
 * Used by the save/load UI to display slot information without loading full game state.
 */
public class SaveSlotInfo {
    
    private int slotId;
    private boolean hasSave;
    private String heroName;
    private int heroLevel;
    private int currentWave;
    private int playtimeSeconds;
    private LocalDateTime saveTime;
    
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public SaveSlotInfo(int slotId) {
        this.slotId = slotId;
        this.hasSave = false;
        this.heroName = "Empty Slot";
        this.heroLevel = 1;
        this.currentWave = 1;
        this.playtimeSeconds = 0;
        this.saveTime = null;
    }
    
    public SaveSlotInfo(int slotId, String heroName, int heroLevel, 
                       int currentWave, int playtimeSeconds, LocalDateTime saveTime) {
        this.slotId = slotId;
        this.hasSave = true;
        this.heroName = heroName;
        this.heroLevel = heroLevel;
        this.currentWave = currentWave;
        this.playtimeSeconds = playtimeSeconds;
        this.saveTime = saveTime;
    }
    
    // Getters
    public int getSlotId() { return slotId; }
    public boolean hasSave() { return hasSave; }
    public String getHeroName() { return heroName; }
    public int getHeroLevel() { return heroLevel; }
    public int getCurrentWave() { return currentWave; }
    public int getPlaytimeSeconds() { return playtimeSeconds; }
    public LocalDateTime getSaveTime() { return saveTime; }
    
    /**
     * Returns formatted playtime string (e.g., "2h 15m" or "45m 30s").
     */
    public String getFormattedPlaytime() {
        return DatabaseManager.formatPlaytime(playtimeSeconds);
    }
    
    /**
     * Returns formatted save time string.
     */
    public String getFormattedSaveTime() {
        if (saveTime == null) return "Never";
        return saveTime.format(formatter);
    }
    
    /**
     * Returns a short description for UI display.
     */
    public String getShortDescription() {
        if (!hasSave) return "Empty";
        return String.format("Wave %d | Lv.%d | %s", 
            currentWave, heroLevel, getFormattedPlaytime());
    }
    
    /**
     * Returns slot label (Auto, Slot 1, Slot 2, Slot 3).
     */
    public String getSlotLabel() {
        if (slotId == 0) return "Auto-Save";
        return "Slot " + slotId;
    }
    
    @Override
    public String toString() {
        return String.format("SaveSlotInfo[slot=%d, hasSave=%s, hero=%s, wave=%d, time=%s]",
            slotId, hasSave, heroName, currentWave, getFormattedPlaytime());
    }
}
