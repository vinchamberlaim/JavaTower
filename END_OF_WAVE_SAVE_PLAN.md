# End-of-Wave Save System - Comprehensive Plan

**Feature:** Automatic save at end of each wave + manual save/load with tower positions  
**Priority:** High (requested by user)  
**Estimated Effort:** Medium-High (Database schema changes, serialization, UI)  
**Agents:** Kimi (primary), Copilot (support/review)  

---

## 1. OVERVIEW

### Current State
- Database only saves: Hero stats, inventory, wave number
- Towers are NOT persisted - lost on game exit
- Manual save/load exists but incomplete

### Goal
- **Automatic save** at end of every wave (after victory, before next wave starts)
- **Complete state preservation**: Hero + Inventory + Towers + Wave progress
- **Multiple save slots** (3 slots)
- **Save metadata**: Timestamp, wave number, hero level, playtime
- **Load UI**: Visual save slot browser

---

## 2. DATABASE SCHEMA DESIGN

### New Tables

```sql
-- Extended save state with tower data
CREATE TABLE save_slots (
    slot_id INTEGER PRIMARY KEY,  -- 1, 2, or 3
    hero_name TEXT,
    hero_level INTEGER,
    hero_hp INTEGER,
    hero_max_hp INTEGER,
    hero_attack INTEGER,
    hero_defence INTEGER,
    hero_gold INTEGER,
    hero_mana INTEGER,
    hero_max_mana INTEGER,
    hero_exp INTEGER,
    hero_x REAL,
    hero_y REAL,
    current_wave INTEGER,
    playtime_seconds INTEGER,  -- Total play time
    save_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    inventory_data TEXT,  -- JSON: grid width/height, items with positions
    skill_tree_data TEXT,  -- JSON: unlocked nodes
    skill_progression_data TEXT  -- JSON: weapon skill XP
);

-- Tower placements for each save slot
CREATE TABLE save_towers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    slot_id INTEGER,
    tower_type TEXT,  -- ARROW, MAGIC, SIEGE, SUPPORT
    grid_x INTEGER,
    grid_y INTEGER,
    upgrade_level INTEGER DEFAULT 0,
    FOREIGN KEY (slot_id) REFERENCES save_slots(slot_id) ON DELETE CASCADE
);

-- Metadata for save slot browser (fast loading)
CREATE TABLE save_metadata (
    slot_id INTEGER PRIMARY KEY,
    last_wave INTEGER,
    last_save_time DATETIME,
    hero_level INTEGER,
    playtime_seconds INTEGER,
    thumbnail_data TEXT  -- Optional: base64 encoded mini-map screenshot
);
```

### Migration Strategy
- Keep existing `save_state` table for backward compatibility
- New saves go to `save_slots` + `save_towers`
- Load checks new tables first, falls back to old table

---

## 3. SERIALIZATION STRATEGY

### Inventory Serialization (JSON)
```json
{
  "width": 5,
  "height": 5,
  "items": [
    {
      "name": "Iron Sword",
      "slot": "WEAPON",
      "rarity": "RARE",
      "gridX": 0,
      "gridY": 0,
      "stats": {"attack": 15, "speed": 2}
    }
  ]
}
```

### Tower Serialization (SQL rows)
- One row per tower
- Simple: type, grid position, upgrade level

### Skill Tree Serialization (JSON)
```json
{
  "combat": ["c1", "c2", "c3"],
  "magic": ["m1", "m2"],
  "utility": [],
  "unspentPoints": 3
}
```

### Skill Progression (JSON)
```json
{
  "MELEE": {"level": 3, "xp": 450},
  "RANGED": {"level": 2, "xp": 180},
  "NECROMANCY": {"level": 1, "xp": 50},
  "HOLY": {"level": 0, "xp": 0},
  "DEFENCE": {"level": 2, "xp": 300}
}
```

---

## 4. CLASS DESIGN

### SaveGameManager (NEW CLASS)
```java
package javatower.systems;

public class SaveGameManager {
    private static final int MAX_SLOTS = 3;
    
    // Save current game state to slot (1-3)
    public boolean saveToSlot(int slotId, Hero hero, List<Tower> towers, 
                              int currentWave, int playtimeSeconds);
    
    // Load game state from slot
    public GameState loadFromSlot(int slotId);
    
    // Get metadata for all slots (for UI)
    public List<SaveSlotInfo> getAllSlotInfo();
    
    // Delete save in slot
    public boolean deleteSlot(int slotId);
    
    // Check if slot has save
    public boolean slotExists(int slotId);
    
    // Auto-save (uses slot 0 as "auto" slot)
    public boolean autoSave(Hero hero, List<Tower> towers, int currentWave);
    public GameState loadAutoSave();
}

// Data class for UI
public class SaveSlotInfo {
    int slotId;
    boolean hasSave;
    String heroName;
    int heroLevel;
    int currentWave;
    int playtimeSeconds;
    LocalDateTime saveTime;
    // Get formatted playtime: "2h 15m"
    String getFormattedPlaytime();
}
```

### GameState (Data Transfer Object)
```java
public class GameState {
    Hero hero;
    List<TowerData> towers;
    int currentWave;
    int playtimeSeconds;
    // Tower placement data without live objects
    public static class TowerData {
        Tower.TowerType type;
        int gridX, gridY;
        int upgradeLevel;
    }
}
```

---

## 5. UI/UX DESIGN

### Auto-Save Flow
1. Wave completes (all enemies dead)
2. Screen shows "Wave X Complete!" + "Auto-saving..."
3. Save to auto-save slot (slot 0)
4. 3-second countdown to next wave
5. During countdown, player can open shop/skills/inventory

### Manual Save (ESC Menu or new button)
- Pause game
- Show 3 save slots as cards
- Each card shows:
  - Hero name & level
  - Current wave
  - Playtime
  - Timestamp
  - Mini-map thumbnail (if possible)
- Options: "Save Here", "Load", "Delete"
- Confirmation dialog for overwrite/delete

### Load Game (Main Menu)
- "Continue" button loads auto-save if exists
- "Load Game" shows save slot browser
- Click slot → Load → Show loading screen → Start game

### In-Game HUD Indicator
- Small save icon in corner
- Flashes when auto-saving
- Tooltip: "Last saved: Wave 12"

---

## 6. INTEGRATION POINTS

### GameGUI.java Changes
```java
// In wave completion handler:
private void onWaveComplete() {
    // ... existing code ...
    
    // Auto-save
    SaveGameManager saveManager = new SaveGameManager();
    boolean saved = saveManager.autoSave(hero, towers, waveManager.getCurrentWave());
    if (saved) {
        showAutoSaveIndicator();
        Logger.info("Auto-saved at wave %d", waveManager.getCurrentWave());
    }
    
    // ... rest of wave transition ...
}

// New method for manual save
private void showSaveMenu() {
    // Pause game
    // Show SaveGamePanel
}
```

### DatabaseManager.java Changes
- Add new table creation to `initialize()`
- Add methods for CRUD operations on save_slots/towers

### Main Menu Changes
- "Continue" button (loads auto-save)
- "Load Game" button (shows slot browser)

---

## 7. ERROR HANDLING

### Save Failures
- Try-catch around all DB operations
- Log errors with Logger.error()
- Show user-friendly message: "Save failed - disk full?"
- Don't crash game on save failure

### Load Failures
- Validate data before loading
- If corruption detected, show error and delete slot
- Graceful fallback to new game

### Migration Failures
- If old save exists but new tables don't, migrate automatically
- If migration fails, keep old save, start new save system

---

## 8. TESTING PLAN

### Unit Tests (if testing framework added)
```java
@Test
public void testSaveAndLoad() {
    // Create hero, towers
    // Save to slot 1
    // Load from slot 1
    // Assert all data matches
}

@Test
public void testAutoSave() {
    // Complete wave
    // Check auto-save slot has data
}

@Test
public void testOverwrite() {
    // Save to slot 1
    // Save again to slot 1
    // Assert old data replaced
}
```

### Manual Testing
1. Play 3 waves, verify auto-save after each
2. Exit game, relaunch, verify "Continue" works
3. Save to slot 2, load slot 2, verify towers present
4. Delete save, verify slot empty
5. Test with full inventory (edge case)
6. Test with 8 towers (max) placed

---

## 9. COORDINATION WITH COPILOT

### Kimi Will Handle:
- SaveGameManager.java (new class)
- Database schema updates
- SaveSlotInfo data class
- GameState DTO
- Integration in GameGUI.java (auto-save logic)
- SaveGamePanel UI (new panel for save/load UI)

### Copilot Could Handle:
- Visual polish on save slot cards
- Loading screen animation
- Auto-save indicator animation
- Sound effect on save
- Migration from old save format

### Shared Carefully:
- DatabaseManager.java - add new methods only
- Main menu - add buttons only

---

## 10. IMPLEMENTATION ORDER

### Phase 1: Core Database (1-2 hours)
1. Create new tables in DatabaseManager
2. Test schema with simple inserts
3. Add migration logic

### Phase 2: Save System (2-3 hours)
1. Implement SaveGameManager
2. Implement serialization for Hero/Inventory/Towers
3. Test save/load cycle

### Phase 3: Auto-Save (1 hour)
1. Hook into wave completion
2. Add auto-save indicator
3. Test with gameplay

### Phase 4: UI (2-3 hours)
1. Create SaveGamePanel
2. Add to main menu
3. Polish visuals

### Phase 5: Testing (1 hour)
1. Test all scenarios
2. Fix bugs
3. Update documentation

---

## 11. FILES TO CREATE/MODIFY

### New Files:
- `javatower/systems/SaveGameManager.java`
- `javatower/gui/SaveGamePanel.java`
- `javatower/data/SaveSlotInfo.java`
- `javatower/data/GameState.java`

### Modified Files:
- `javatower/database/DatabaseManager.java` - new tables
- `javatower/gui/GameGUI.java` - auto-save hook
- `javatower/gui/GameBoard.java` - save indicator
- `Main.java` or menu class - load buttons
- `sources.txt` - add new files

---

## 12. OPEN QUESTIONS FOR COPILOT

1. Should we keep the old `save_state` table or migrate and drop it?
2. Do we want a "Quick Save" hotkey (F5) in addition to auto-save?
3. Should saves include bone pile positions?
4. Do we want cloud save support (future feature)?
5. Should we compress the JSON data to save disk space?

---

**Next Step:** Kimi implements Phase 1 (Database schema)  
**Status:** Ready to begin implementation  
**Updated:** 2026-03-24
