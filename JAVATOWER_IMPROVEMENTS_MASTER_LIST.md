# JavaTower - Master Improvements List

**Project:** JavaTower - CIS096-1 Assessment 2  
**Collaboration:** Kimi + Copilot  
**Last Updated:** 2026-03-24  

---

## COMPLETED FEATURES (18 Total)

### Kimi's Completed (8)
| # | Feature | Files |
|---|---------|-------|
| K1 | GameEventBus observer pattern | GameEventBus.java |
| K2 | Logger utility | Logger.java |
| K3 | GameBalanceConfig constants | GameBalanceConfig.java |
| K4 | Screen shake on critical hits | GameBoard.java |
| K5 | Tower placement preview (ghost + range) | GameBoard.java |
| K6 | Hotkeys 1-4/Q/W/ESC/TAB/M | GameGUI.java |
| K7 | Special ability W (ranged AOE) | GameGUI.java |
| K8 | Mini-map (M to toggle) | MiniMap.java |

### Copilot's Completed (10)
| # | Feature | Files |
|---|---------|-------|
| C0 | Equipment system (slots, 2H, rings, sets) | Multiple |
| C1 | Visual effects system (13 types) | VisualEffect.java |
| C2 | Skill progression & set bonuses | SkillProgression.java |
| C3 | Forge system | Forge.java, ForgePanel.java |
| C4 | InventoryPanel visual grid | InventoryPanel.java |
| C5 | Arrow key movement | Hero.java, GameGUI.java |
| C6 | E key = self heal | GameGUI.java |
| C7 | R key = tower boost | GameGUI.java |
| C8 | S key = sell tower | GameGUI.java |
| C9 | Kill stats + game over display | Hero.java, GameGUI.java |
| C10 | Enemy hover tooltips | GameBoard.java |

---

## PRIORITY TIERS

### 🔴 TIER 1: Core Gameplay (Do First)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 1 | **End-of-Wave Auto-Save** | Save hero + towers after each wave | Medium | Kimi (IN PROGRESS) |
| 2 | **Save/Load with Towers** | Multiple slots, persist tower positions | High | Kimi |
| 3 | **Sound Effects** | Attack, hit, death, level-up using AudioClip | Medium | Copilot? |
| 4 | **Wave Countdown Timer** | Show time until next wave | Low | Either |
| 5 | **Tower Range Visualization** | Hover to see range circle | Low | Copilot? |
| 6 | **Damage Numbers Toggle** | Option to show/hide floating numbers | Low | Either |
| 7 | **Pause Menu** | ESC opens pause with save/load/quit | Medium | Either |
| 8 | **FPS Counter Toggle** | Show/hide frame rate | Low | Either |

### 🟠 TIER 2: Gameplay Depth (High Impact)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 9 | **Tower Synergies** | Adjacent towers boost each other | Medium | Either |
| 10 | **Tower Specialization** | Branch upgrades at level 2 (Sniper/Gatling) | High | Either |
| 11 | **Elite Enemies** | Random modifiers (Fast, Armored, Explosive) | Medium | Either |
| 12 | **Enemy Abilities** | Each type has unique skill | Medium | Either |
| 13 | **Wave Modifiers** | Random effects each wave (Swarm, Rush, etc.) | Medium | Kimi? |
| 14 | **Boss Phases** | Multi-phase boss fights with mechanics | High | Either |
| 15 | **Environmental Hazards** | Traps, explosions, choke points | Medium | Either |
| 16 | **Dodge/Roll Mechanic** | SHIFT to roll with i-frames | Medium | Copilot? |
| 17 | **Ultimate Ability** | Fill meter for massive attack | Medium | Either |
| 18 | **Weapon Mastery** | Track skill per weapon type | Medium | Either |
| 19 | **Combo System** | Timed attacks for multipliers | Medium | Either |
| 20 | **Status Effects** | Poison, slow, burn, freeze | High | Either |
| 21 | **Enemy Loot Tables** | Enemies drop items on death | Medium | Kimi? |
| 22 | **Item Rarity Glow** | Visual effect for legendary items | Low | Copilot? |
| 23 | **Set Bonus UI** | Show active set bonuses on HUD | Low | Copilot? |
| 24 | **Skill Tree Visuals** | Connect nodes with lines | Medium | Copilot? |

### 🟡 TIER 3: Content Expansion (More Variety)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 25 | **New Tower: Slow Tower** | Reduces enemy speed | Low | Either |
| 26 | **New Tower: Poison Tower** | Damage over time | Low | Either |
| 27 | **New Tower: Chain Lightning** | Hits multiple enemies | Medium | Either |
| 28 | **New Tower: Fear Tower** | Makes enemies flee | Medium | Either |
| 29 | **New Tower: Sniper Tower** | Extreme range, slow fire | Low | Either |
| 30 | **New Tower: Buff Tower** | Increases nearby tower damage | Medium | Either |
| 31 | **New Enemy: Healer** | Heals other enemies | Medium | Either |
| 32 | **New Enemy: Bomber** | Explodes on death | Low | Either |
| 33 | **New Enemy: Swarmer** | Tiny, fast, low HP | Low | Either |
| 34 | **New Enemy: Shield Bearer** | Protects nearby enemies | Medium | Either |
| 35 | **New Enemy: Summoner** | Spawns minions | Medium | Either |
| 36 | **New Enemy: Teleporter** | Blink around map | Medium | Either |
| 37 | **Mini-Boss Variants** | Unique mechanics for waves 5/10/15/20/25 | High | Either |
| 38 | **Final Boss Rework** | More phases, mechanics, summons | High | Either |
| 39 | **Procedural Enemy Sprites** | Generate pixel art instead of circles | High | Kimi? |
| 40 | **Tower Skin Variants** | Visual styles per element | Low | Copilot? |

### 🟢 TIER 4: UI/UX Polish (Better Experience)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 41 | **Loading Screen** | Progress bar + tips during load | Low | Copilot |
| 42 | **Save Slot Cards** | Visual browser with thumbnails | Medium | Copilot |
| 43 | **Auto-Save Indicator** | Flash "Saved!" notification | Low | Copilot |
| 44 | **Combat Log Panel** | Recent damage/events | Low | Either |
| 45 | **Damage Meter** | Track DPS by source | Medium | Either |
| 46 | **Enemy Inspector** | Detailed stats on hover | Low | Copilot |
| 47 | **Tower Info Panel** | Click tower to see stats | Low | Either |
| 48 | **Settings Menu** | Graphics, audio, controls | Medium | Either |
| 49 | **Key Rebinding** | Customize hotkeys | Medium | Either |
| 50 | **Tooltip System** | Hover for item/enemy info | Medium | Either |
| 51 | **Notification Queue** | Stack notifications | Low | Copilot |
| 52 | **Screen Flash Effects** | Low HP warning, level up | Low | Copilot |
| 53 | **Particle System** | Better than current effects | High | Kimi? |
| 54 | **Projectile Trails** | Fade behind arrows/magic | Low | Either |
| 55 | **Impact Particles** | Burst on hit | Low | Copilot |
| 56 | **Weather Effects** | Rain, fog, snow cosmetic | Medium | Copilot? |
| 57 | **Screen Transitions** | Fade between screens | Low | Copilot |
| 58 | **Main Menu Background** | Animated game scene | Medium | Copilot? |

### 🔵 TIER 5: Progression & Meta (Long-term)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 59 | **Achievement System** | Unlockables for feats | Medium | Either |
| 60 | **Daily Challenges** | Seed-based daily runs | Medium | Either |
| 61 | **Leaderboards** | Online high scores | High | Either |
| 62 | **Endless Mode** | Survive infinite waves | Medium | Either |
| 63 | **Boss Rush Mode** | Fight all bosses back-to-back | Low | Either |
| 64 | **Hero Only Mode** | No towers challenge | Low | Either |
| 65 | **Difficulty Levels** | Easy/Normal/Hard/Nightmare | Medium | Either |
| 66 | **Permadeath Option** | One life mode | Low | Either |
| 67 | **Blessings & Curses** | Modifiers for runs | Medium | Either |
| 68 | **Meta-Unlocks** | Between-run progression | Medium | Either |
| 69 | **New Hero Classes** | Knight, Ranger, Mage starters | High | Either |
| 70 | **Skill Trees Expand** | More nodes, branches | Medium | Either |
| 71 | **Item Collections** | Achievement for finding all | Low | Either |
| 72 | **Bestiary** | Enemy encyclopedia | Medium | Either |
| 73 | **Statistics Page** | Lifetime stats | Low | Either |
| 74 | **Tutorial System** | First-time player guide | Medium | Either |
| 75 | **Tooltips Tutorial** | Contextual help | Low | Either |

### 🟣 TIER 6: Technical & Optimization

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 76 | **Spatial Partitioning** | Grid for collision detection | High | Kimi |
| 77 | **Object Pooling** | Reuse enemy/projectile objects | Medium | Either |
| 78 | **Lazy UI Updates** | Only refresh when changed | Low | Either |
| 79 | **Asset Compression** | Smaller file sizes | Medium | Either |
| 80 | **Memory Profiling** | Fix leaks | High | Either |
| 81 | **Startup Time** | Faster loading | Medium | Either |
| 82 | **Database Optimization** | Indexing, batch operations | Medium | Kimi? |
| 83 | **Config File Support** | JSON/YAML for balance | Medium | Kimi? |
| 84 | **Mod Support Framework** | Load external content | High | Kimi? |
| 85 | **Unit Tests** | JUnit for core systems | Medium | Either |
| 86 | **Error Handling** | Graceful degradation | Medium | Either |
| 87 | **Logging System Expand** | More debug info | Low | Kimi (DONE) |
| 88 | **Crash Reporter** | Auto-report errors | Medium | Either |
| 89 | **Replay System** | Record and playback runs | High | Either |
| 90 | **Cloud Save Support** | Online backup | High | Either |

### ⚫ TIER 7: Stretch Goals (Nice to Have)

| # | Improvement | Description | Complexity | Owner |
|---|-------------|-------------|------------|-------|
| 91 | **Multiplayer Co-op** | 2 players | Very High | Both? |
| 92 | **PvP Mode** | Tower defense vs | Very High | Both? |
| 93 | **Map Editor** | Create custom maps | High | Either |
| 94 | **Steam Integration** | Achievements, workshop | High | Either |
| 95 | **Mobile Port** | Android/iOS | Very High | Both? |
| 96 | **Console Port** | Controller support | High | Either |
| 97 | **Localization** | Multiple languages | Medium | Either |
| 98 | **Accessibility** | Colorblind, screen reader | Medium | Either |
| 99 | **Stream Integration** | Twitch chat votes | Medium | Either |
| 100 | **Seasonal Events** | Halloween, Xmas content | Medium | Copilot? |
| 101 | **Merchandise Tie-in** | Unlockable codes | Low | Either |
| 102 | **Discord Rich Presence** | Status integration | Low | Either |
| 103 | **Speedrun Mode** | Timer + splits | Medium | Either |
| 104 | **Ironman Mode** | One save, permadeath | Low | Either |
| 105 | **New Game Plus** | Carry over after win | Medium | Either |

---

## ASSIGNMENT TO AGENTS

### Kimi's Domain (System Architecture)
- Database systems
- Event systems
- Configuration/balance
- Save/load core
- Performance optimization
- Complex gameplay logic

### Copilot's Domain (Visuals & Polish)
- UI/UX design
- Animations
- Sound effects
- Visual effects polish
- Menu screens
- Tooltip systems

### Shared (Collaboration Required)
- GameGUI.java (hotkeys, hooks)
- GameBoard.java (rendering)
- New tower/enemy types
- Balance tuning
- Testing

---

## CURRENT STATUS

**In Progress:**
- K9: End-of-Wave Save System (Kimi - 60%)

**Next Up:**
- CS1-CS5: Save system polish (Copilot, depends on K9)
- #3: Sound Effects (Copilot)
- #4: Wave Countdown (Either)
- #9: Tower Synergies (Either)

---

## HOW TO USE THIS LIST

1. **Pick a tier** based on current priorities
2. **Check ownership** - who should implement?
3. **Update dev_tasks.json** when starting
4. **Mark complete** in both files when done
5. **Coordinate** on shared files via handoff docs

---

*This is a living document - add new ideas as they come up!*
