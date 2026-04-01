# Copilot Handoff: End-of-Wave Save System

**From:** Kimi  
**To:** Copilot  
**Date:** 2026-03-24  
**Feature:** K9 - End-of-Wave Save System with Tower Persistence

---

## 📋 Overview

I'm implementing a comprehensive save system that automatically saves at the end of each wave AND allows manual save/load with **tower positions preserved**. This is a major feature request.

**Key Requirements:**
1. Auto-save after every wave victory (before next wave starts)
2. Save tower placements (not just hero/inventory)
3. Multiple save slots (3 slots)
4. Save metadata (timestamp, wave, playtime)
5. Visual save slot browser UI

---

## 📁 Planning Document

**Read this first:** `END_OF_WAVE_SAVE_PLAN.md`

Contains:
- Database schema design
- Serialization strategy
- Class diagrams
- UI/UX mockups
- Testing plan
- Implementation phases

---

## 🗄️ Database Schema (Proposed)

```sql
-- Main save data (one row per slot)
CREATE TABLE save_slots (
    slot_id INTEGER PRIMARY KEY,  -- 1, 2, 3 (and 0 for auto-save)
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
    playtime_seconds INTEGER,
    save_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    inventory_data TEXT,  -- JSON
    skill_tree_data TEXT,  -- JSON
    skill_progression_data TEXT  -- JSON
);

-- Tower placements (one row per tower)
CREATE TABLE save_towers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    slot_id INTEGER,
    tower_type TEXT,  -- ARROW, MAGIC, SIEGE, SUPPORT
    grid_x INTEGER,
    grid_y INTEGER,
    upgrade_level INTEGER DEFAULT 0,
    FOREIGN KEY (slot_id) REFERENCES save_slots(slot_id) ON DELETE CASCADE
);

-- Fast metadata for UI (no need to load full save)
CREATE TABLE save_metadata (
    slot_id INTEGER PRIMARY KEY,
    last_wave INTEGER,
    last_save_time DATETIME,
    hero_level INTEGER,
    playtime_seconds INTEGER
);
```

---

## 🏗️ Architecture

### New Classes (Kimi will create)

1. **SaveGameManager.java** - Core save/load logic
   - `saveToSlot(slotId, hero, towers, wave, playtime)`
   - `loadFromSlot(slotId)` → returns GameState
   - `autoSave()` / `loadAutoSave()`
   - `getAllSlotInfo()` → for UI

2. **SaveGamePanel.java** - UI for save/load browser
   - 3 save slot cards
   - Shows: hero name, level, wave, playtime, timestamp
   - Buttons: Save, Load, Delete

3. **SaveSlotInfo.java** - Data class for UI
4. **GameState.java** - DTO with Hero + TowerData list

### Modified Classes

- **DatabaseManager.java** - Add table creation + CRUD methods
- **GameGUI.java** - Add auto-save hook in wave completion
- **sources.txt** - Add new files

---

## 🤝 Collaboration Areas

### I'd like your input on:

1. **Visual Design for Save Slot Cards**
   - What should they look like?
   - Should we show a mini-map thumbnail?
   - Animation ideas for hover/selection?

2. **Auto-Save Indicator**
   - Where on screen?
   - Flash animation?
   - Text: "Auto-saved Wave 12"?

3. **Loading Screen**
   - Show while loading save?
   - Progress bar or spinner?
   - Tips while loading?

4. **Sound Effects**
   - Save success sound?
   - Load complete sound?
   - Error sound if save fails?

5. **Bone Piles Question**
   - Should we save bone pile positions?
   - Or let them regenerate?

6. **Migration Strategy**
   - Old saves don't have towers
   - Should we auto-migrate or start fresh?

---

## 🎯 Implementation Phases

### Phase 1: Database (Kimi - IN PROGRESS)
- [ ] Create new tables in DatabaseManager
- [ ] Add CRUD methods
- [ ] Test with sample data

### Phase 2: SaveGameManager (Kimi - PLANNED)
- [ ] Implement core save/load logic
- [ ] JSON serialization for inventory/skills
- [ ] Tower serialization to SQL

### Phase 3: Auto-Save Hook (Kimi - PLANNED)
- [ ] Add to wave completion in GameGUI
- [ ] Add auto-save indicator

### Phase 4: UI Panel (Kimi - PLANNED, Copilot - OPTIONAL)
- [ ] Create SaveGamePanel
- [ ] Add to main menu
- [ ] Visual polish (Copilot could take this!)

### Phase 5: Polish (Copilot - YOUR TURN!)
- [ ] Animations for save/load
- [ ] Sound effects
- [ ] Loading screen
- [ ] Visual effects

---

## ⚠️ Boundaries

**Kimi will handle:**
- All database schema changes
- SaveGameManager core logic
- Serialization code
- Basic UI structure

**Copilot can handle (after Kimi finishes core):**
- Visual polish on UI
- Animations
- Sound effects
- Loading screen
- Error message styling

**Shared (append-only):**
- DatabaseManager.java - only add new methods
- GameGUI.java - only add new methods/hooks

---

## ❓ Questions for You

Please add your thoughts to this file or dev_tasks.json:

1. **Visual priority**: Should save slots look like Diablo-style cards or simple list items?

2. **Quick save**: Want a separate F5 quick-save in addition to auto-save?

3. **Max towers**: Should we enforce max 8 towers in save? Or save any amount?

4. **Cloud saves**: Should architecture support cloud saves later? (I think yes - use UUIDs)

5. **Compression**: JSON could be big - compress with GZIP? Or keep human-readable?

---

## 📊 Progress Tracking

**Current Status:** Planning complete, starting Phase 1  
**Files Created:** None yet  
**Files Modified:** None yet  

See `dev_tasks.json` for live status updates.

---

## 🔗 Related Files

- `END_OF_WAVE_SAVE_PLAN.md` - Full technical spec
- `dev_tasks.json` - Task tracking
- `CHAT_LOG_AI_ASSISTANCE.md` - Previous work history

---

**Ready for your feedback!** Once you review the plan, I'll start Phase 1 implementation.

