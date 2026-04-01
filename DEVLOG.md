# JavaTower — Full Development Log
**Course:** CIS096-1 Assessment 2  
**Team:** Vincent Chamberlain, Nicolas Alfaro, Emmanuel Adewumi  
**Repo:** `vinchamberlaim/JavaTower` on GitHub (`origin/master`)

---

## Project Overview
JavaTower is a real-time tower defence RPG built with JavaFX.
- Canvas-based 2D rendering with `AnimationTimer` game loop
- 960×640 screen, 64px tile grid for tower placement
- SQLite/JDBC persistence via `sqlite-jdbc-3.45.1.0.jar`
- Tetris-style grid inventory where items occupy width×height cells
- 10-tier undead enemy hierarchy with boss mechanics
- 4 tower types, equipment sets, forge system, skill progression

---

## Commit History (Chronological)

### Commit 1 — `057b409` — 20 Mar 2026 08:21
**"Week 9 submission: JavaTower OOP Architecture and Implementation"**

Initial submission. 46 files, ~4,870 lines across 7 packages. This established the full game architecture:

#### Package Structure
```
Main.java                          — Entry point
javatower/
├── database/
│   └── DatabaseManager.java       — SQLite CRUD (heroes, inventory, settings)
├── entities/
│   ├── Entity.java                — Abstract base (x, y, hp, alive, render)
│   ├── Hero.java                  — Player character (combat, movement, equip)
│   ├── Enemy.java                 — Base enemy with pathfinding, loot tables
│   ├── Item.java                  — Item definitions (name, slot, stats, rarity)
│   ├── Tower.java                 — Abstract tower (range, damage, cooldown)
│   ├── BonePile.java              — Collectible loot drop entity
│   └── enemies/
│       ├── Skeleton.java          — Tier 1 (basic)
│       ├── Zombie.java            — Tier 2 (slow, tanky)
│       ├── Ghoul.java             — Tier 3 (fast)
│       ├── Wight.java             — Tier 4 (armoured)
│       ├── Wraith.java            — Tier 5 (phasing)
│       ├── Revenant.java          — Tier 6 (regenerating)
│       ├── DeathKnight.java       — Tier 7 (shields minions)
│       ├── Lich.java              — Tier 8 (mini-boss, spellcaster)
│       ├── BoneColossus.java      — Tier 9 (tanky boss)
│       └── NecromancerKing.java   — Tier 10 (final boss, summons, phases)
├── entities/towers/
│   ├── ArrowTower.java            — Fast single-target
│   ├── MagicTower.java            — AoE magic damage
│   ├── SiegeTower.java            — Slow, heavy damage
│   └── SupportTower.java          — Buff/heal nearby towers
├── factories/
│   ├── EnemyFactory.java          — Creates enemies by tier + wave scaling
│   ├── ItemFactory.java           — Random item generation with rarity rolls
│   └── TowerFactory.java          — Tower instantiation helper
├── gui/
│   ├── GameGUI.java               — Main Application, scene management, game loop
│   ├── GameBoard.java             — Canvas renderer (map, entities, HUD)
│   ├── HeroPanel.java             — Hero stats display
│   ├── InventoryPanel.java        — Grid-based inventory management
│   ├── ShopPanel.java             — Buy/sell item interface
│   ├── SkillTreePanel.java        — Skill tree node selection
│   └── WaveInfoPanel.java         — Wave counter and enemy info
├── systems/
│   ├── CombatSystem.java          — Damage calculation and resolution
│   ├── Inventory.java             — Grid-based item storage (fit/place/remove)
│   ├── Shop.java                  — Shop inventory and pricing
│   ├── SkillNode.java             — Skill tree node data
│   ├── SkillTree.java             — Skill tree graph and progression
│   └── WaveManager.java           — Wave spawning, timing, difficulty
└── util/
    ├── Constants.java             — Game constants (screen size, tile size, etc.)
    └── GameState.java             — Enum: MENU, PLAYING, PAUSED, GAME_OVER
```

#### Core Mechanics Implemented
- **Hero:** Base stats (attack, defence, critChance, health, maxHealth, mana, maxMana), auto-attack nearest enemy, level-up scaling, equipment slots
- **Enemy hierarchy:** 10 tiers of undead with scaling HP/ATK/DEF/speed/gold/XP. NecromancerKing has multi-phase boss fight with summon waves
- **Tower system:** 4 types with different range/damage/cooldown profiles, placed on 64px grid
- **Inventory:** Grid-based with items occupying width×height cells (Tetris-style), collision checking for placement
- **Shop:** Buy items (random stock refreshed per wave), sell at discount
- **Skill tree:** Branching node graph, spending skill points on level-up
- **Wave manager:** Timed waves of increasing difficulty, enemy composition scaling
- **Database:** SQLite persistence for hero saves, inventory state, game settings
- **GUI:** JavaFX Application with scene switching (menu → game → panels)

#### Build System
```
sources.txt          — Lists all 40 .java files for javac @sources.txt
RunJavaTower.bat     — Windows batch launcher
RunJavaTower.ps1     — PowerShell launcher with error handling
```

#### Documentation
- `JavaTower_Architecture_Documentation.md` — 947-line architecture document covering OOP design, UML relationships, package organization, design patterns used

---

### Commit 2 — `70aeac8` — 20 Mar 2026 08:36
**"Add group members: Nicolas Alfaro, Emmanuel Adewumi"**

Updated README.md with team member names.

---

### Commit 3 — `8852ecc` — 20 Mar 2026 08:45
**"Add application screenshots to architecture doc"**

Added 6 gameplay screenshots to `screenshots/` folder and embedded them in the architecture documentation:
- `01_main_menu.png` — Main menu screen
- `02_gameplay.png` — Active gameplay
- `02_gameplay_start.png` — Game start state
- `03_gameplay_combat.png` — Combat in progress
- `04_shop_or_gameplay.png` — Shop interface
- `05_inventory_or_gameplay.png` — Inventory screen
- `06_skilltree_or_gameplay.png` — Skill tree interface

---

### Commit 4 — `190ed70` — 20 Mar 2026 09:28
**"Add equipment sets, forge upgrade system, and use-based skill progression"**

Major feature addition — 4 new files, extensive modifications to 6 existing files.

#### New Files
| File | Lines | Purpose |
|------|-------|---------|
| `javatower/systems/SetBonusManager.java` | 143 | Detects equipped set pieces, applies 2pc/4pc bonuses |
| `javatower/systems/SkillProgression.java` | 131 | XP-based weapon skill levels (Melee, Ranged, Necro, Holy, Defence) |
| `javatower/systems/Forge.java` | 59 | Combine 2 identical items → upgrade rarity tier for gold |
| `javatower/gui/ForgePanel.java` | 206 | GUI for forge item combining |

#### Equipment Sets (4 sets × 4 pieces = 16 set items)
Each set has a 2-piece and 4-piece bonus threshold:

| Set | Theme | Weapon | Offhand | Helmet | Chest | 2pc Bonus | 4pc Bonus |
|-----|-------|--------|---------|--------|-------|-----------|-----------|
| **Holy** | Paladin | Blessed Blade | Blessed Shield | Holy Helm | Sacred Robe | +heal, +undead dmg | Full holy power |
| **Death** | Necromancer | Soul Reaper | Grimoire | Skull Helm | Death Shroud | +life steal, +mana | Full death power |
| **Fire** | Pyromancer | Flame Sword | Flame Orb | Ember Crown | Inferno Plate | +AoE splash | Full fire power |
| **Knight** | Warrior | Knight's Lance | Tower Shield | Knight's Helm | Knight's Plate | +thorns, +speed | Full knight power |

Set items are minimum RARE rarity and are rare drops from enemies.

#### Forge System
- Combine 2 items with **same name AND same rarity** → produces 1 item at next rarity tier
- Costs gold scaling with rarity: COMMON→UNCOMMON = cheap, EPIC→LEGENDARY = expensive
- GUI: select two matching items from inventory, pay gold, receive upgrade

#### Use-Based Skill Progression
- 5 weapon skills: Melee, Ranged, Necromancy, Holy, Defence
- Each skill gains XP when the corresponding weapon class is used in combat
- Level ups provide passive bonuses to that combat style
- Displayed in HeroPanel alongside hero stats

#### Modified Files
- `Item.java` — Added `WeaponClass` enum (MELEE, RANGED, NECROMANCY, HOLY, DEFENCE, NONE), `EquipmentSet` enum (HOLY, DEATH, FIRE, KNIGHT), set piece factory methods, `Rarity` enum with multipliers (COMMON 1.0× → LEGENDARY 3.0×)
- `ItemFactory.java` — Added set item drops, rarity roll system, new weapon types (Crossbow, Necromancer Staff, Holy Mace)
- `Hero.java` — Integrated SkillProgression, SetBonusManager, equipment stat calculation
- `HeroPanel.java` — Shows skill levels and active set bonuses
- `ShopPanel.java` — Shows set item tags on items
- `InventoryPanel.java` — Shows set item tags
- `sources.txt` — Added 4 new files

---

### Commit 5 — `713761c` — 20 Mar 2026 09:54 (HEAD)
**"Equipment stats now affect combat: attack speed, effective stats, boots/legs items, balanced pricing"**

Equipment stats actually influence gameplay instead of being display-only.

#### Changes
- **Hero.java** — `getEquipmentStat(String stat)` sums stat values across all equipped items. Attack cooldown modified by `attackSpeed`, movement speed by `moveSpeed`. Combat uses effective attack/defence/crit/health/mana
- **Item.java** — Added stat fields: `attack`, `defence`, `critChance`, `health`, `mana`, `healPower`, `moveSpeed`, `attackSpeed`. Added `Slot.LEGS` and `Slot.BOOTS`. Speed stat on all weapons, health on Helmet/Chest. Added Boots (2×1, +defence +moveSpeed) and Leggings (2×2, +defence +health). `autoPrice()` now factors stat total × rarity × slot weight; sell = buy / 3
- **HeroPanel.java** — Shows effective stats with bonus breakdown: "ATK: 15 (+5)", "DEF: 10 (+3)", etc. SPD shows attack cooldown in seconds
- **ItemFactory.java** — Updated to include boots and leggings in random generation

---

## Uncommitted Work (Post `713761c`)

Everything below has been developed in the current AI-assisted session but **not yet committed**.

### 6. Visual Effects System
**New file:** `javatower/gui/VisualEffect.java` (~400 lines)  
**Status:** Created but NOT yet added to `sources.txt`

Lightweight visual effects rendered on the game canvas. Each effect has position, velocity, lifetime, and type-specific rendering logic.

#### Effect Types
| Type | Description | Rendering |
|------|-------------|-----------|
| `ARROW` | Ranged hero attack projectile | Line with arrowhead |
| `FIREBALL` | Fire set projectile | Circle with orange trail particles |
| `NECRO_BOLT` | Death/necromancy projectile | Purple bolt with dark trail |
| `HOLY_SMITE` | Holy attack | Golden beam/pillar |
| `MELEE_SLASH` | Melee weapon swing | White arc slash |
| `DAMAGE_NUMBER` | Floating damage text | Red number, drifts upward, fades |
| `HEAL_NUMBER` | Floating heal text | Green number, drifts upward, fades |
| `IMPACT_BURST` | Hit confirmation | Expanding circle, fades |
| `FIRE_SPLASH` | Fire AoE indicator | Orange expanding ring |
| `TOWER_ARROW` | Arrow tower projectile | Small fast line |
| `TOWER_MAGIC` | Magic tower projectile | Blue-purple bolt |
| `TOWER_SIEGE` | Siege tower projectile | Large slow projectile |
| `GOLD_PICKUP` | Enemy death gold | Floating gold text "+X gold" |

Each type has a static factory method: `VisualEffect.arrow(sx, sy, tx, ty)`, etc.

#### How Effects Work
1. Factory method creates effect with start position, target, speed, lifetime
2. `update(double dt)` moves the effect and returns `true` when expired
3. `render(GraphicsContext gc)` draws the effect type-specifically
4. `GameBoard` maintains `List<VisualEffect>` and renders after all entities

### 7. Combat Effect Integration
**Modified files:** `Hero.java`, `Tower.java`, `GameBoard.java`, `GameGUI.java`

#### Hero.java — Attack Tracking
Added per-frame combat tracking fields:
- `attackedThisFrame` — true if hero attacked this update
- `lastAttackCrit` — true if last attack was critical
- `lastAttackWeaponClass` — WeaponClass of equipped weapon used
- `lastAttackTarget` — reference to the Enemy attacked
- `lastDamageDealt` — damage number dealt

Getters: `didAttackThisFrame()`, `wasAttackCrit()`, `getLastAttackWeaponClass()`, `getLastAttackTarget()`, `clearFrameFlags()`

The `attackEnemy()` method now stores weapon class and crit info. `update()` sets `attackedThisFrame` and stores target reference.

#### Tower.java — Fire Tracking  
Added `lastTarget` and `firedThisFrame` fields. `update()` calls `selectTarget()` before `attack()`, stores target and sets flag. Getters: `getLastTarget()`, `didFireThisFrame()`

#### GameBoard.java — Effect Rendering
- Added `List<VisualEffect> effects` field and `Iterator` import
- `render()` draws effects **after** hero (on top of all entities)
- `updateEffects(double dt)` iterates with Iterator, removes expired effects
- `addEffect(VisualEffect e)` and `getEffects()` methods

#### GameGUI.java — Effect Spawning in Game Loop
`gameUpdate()` modified to spawn effects each frame:
- Hero attack → spawn projectile based on weapon class (arrow/fireball/necro bolt/holy smite/melee slash)
- Hero deals damage → spawn DAMAGE_NUMBER at target position
- Enemy damages hero → spawn DAMAGE_NUMBER at hero position (red)
- Tower fires → spawn TOWER_ARROW / TOWER_MAGIC / TOWER_SIEGE based on tower type
- Enemy dies → spawn GOLD_PICKUP and IMPACT_BURST at death position
- Calls `gameBoard.updateEffects(dt)` each frame

**⚠️ KNOWN ISSUE:** `spawnHeroAttackEffect(target)` is called in GameGUI but the method body is NOT YET IMPLEMENTED — will cause compile error.

### 8. Shield Variants
**Modified files:** `Item.java`, `ItemFactory.java`

Added three shield tiers with different inventory grid footprints:

| Shield | Grid Size | Stats | Notes |
|--------|-----------|-------|-------|
| Buckler | 1×2 | 5 DEF + 2 SPD | Lightweight, leaves inventory room |
| Shield | 2×2 | 8 DEF | Standard medium shield |
| Kite Shield | 2×3 | 12 DEF + 3 HP | Heavy duty, large in inventory |
| Tower Shield | 2×3 | 14 DEF (Knight set) | Resized from 2×2 to 2×3 |

ItemFactory random pool expanded from 10 → 12 types (added Buckler at case 10, Kite Shield at case 11).

### 9. Free Art Assets
**Downloaded to `assets/`:**
- `roguelikeitems.png` — 208×240px, 16×16 tile grid (13 columns × 15 rows)
  - CC-BY license by Joe Williamson (@JoeCreates)
  - Contains: swords, staves, potions, rings, ores, armor pieces, shields, scrolls

**Identified but not yet downloaded:**
- Dungeon Crawl 32×32 tileset — 3,000+ tiles, CC0 (public domain), for hero/enemy/environment sprites

---

## All Item Types (Current State)

### Normal Items (12 types in random pool)
| # | Item | Slot | Grid | Key Stats |
|---|------|------|------|-----------|
| 0 | Sword | WEAPON | 1×3 | ATK + SPD |
| 1 | Staff | WEAPON | 1×3 | ATK + MANA + SPD |
| 2 | Bow | WEAPON | 1×3 | ATK + CRIT + SPD |
| 3 | Shield | OFFHAND | 2×2 | DEF |
| 4 | Helmet | HELMET | 2×2 | DEF + HP |
| 5 | Chest Armor | CHEST | 2×3 | DEF + HP |
| 6 | Potion | CONSUMABLE | 1×1 | Heal amount |
| 7 | Ring | ACCESSORY | 1×1 | Various small bonuses |
| 8 | Boots | BOOTS | 2×1 | DEF + moveSpeed |
| 9 | Leggings | LEGS | 2×2 | DEF + HP |
| 10 | Buckler | OFFHAND | 1×2 | DEF + SPD |
| 11 | Kite Shield | OFFHAND | 2×3 | DEF + HP |

### Set Items (16 items across 4 sets)
| Set | Weapon | Offhand | Helmet | Chest |
|-----|--------|---------|--------|-------|
| Holy | Blessed Blade (1×3) | Blessed Shield (2×2) | Holy Helm (2×2) | Sacred Robe (2×3) |
| Death | Soul Reaper (1×3) | Grimoire (2×2) | Skull Helm (2×2) | Death Shroud (2×3) |
| Fire | Flame Sword (1×3) | Flame Orb (1×1) | Ember Crown (2×2) | Inferno Plate (2×3) |
| Knight | Knight's Lance (1×3) | Tower Shield (2×3) | Knight's Helm (2×2) | Knight's Plate (2×3) |

---

## Build & Run Commands

```powershell
# Compile all sources
javac --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics -cp lib\sqlite-jdbc-3.45.1.0.jar -d out2 @sources.txt

# Run the game
java --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics -cp out2;lib\sqlite-jdbc-3.45.1.0.jar Main
```

**Dependencies:**
- JavaFX SDK (in `javafx-sdk\lib\`)
- SQLite JDBC driver (`lib\sqlite-jdbc-3.45.1.0.jar`)
- Java 17+

---

## Remaining TODO
- [ ] Implement `spawnHeroAttackEffect()` in GameGUI (called but body missing — compile blocker)
- [ ] Add `javatower\gui\VisualEffect.java` to `sources.txt` (compile blocker)
- [ ] Download Dungeon Crawl 32×32 tileset for hero/enemy/environment sprites
- [ ] Create `ItemRenderer` class — load spritesheets, map item names to sprite coordinates
- [ ] Update `InventoryPanel` — visual grid rendering showing items covering width×height cells with sprite icons
- [ ] Inventory grid grows with hero level
- [ ] Bigger waves + enemy stat scaling per wave
- [ ] Compile, test, push to GitHub

---

## Design Patterns Used
- **Factory Pattern:** `EnemyFactory`, `ItemFactory`, `TowerFactory` — object creation separated from usage
- **Observer/Event:** Game loop checks state flags each frame (attackedThisFrame, firedThisFrame)
- **State Pattern:** `GameState` enum (MENU, PLAYING, PAUSED, GAME_OVER) drives scene transitions
- **Template Method:** `Entity` abstract base → `Hero`, `Enemy`, `Tower` subclasses override `update()` and `render()`
- **Strategy:** Different tower attack behaviors (ArrowTower single-target vs MagicTower AoE)
- **Composite:** Equipment set bonuses aggregated from individual piece contributions

## Tech Stack
| Component | Technology |
|-----------|-----------|
| Language | Java 17+ |
| UI Framework | JavaFX (Canvas, AnimationTimer, Scene graph) |
| Database | SQLite via `sqlite-jdbc-3.45.1.0.jar` |
| Build | `javac @sources.txt` with module path |
| Version Control | Git → GitHub |
| Art Assets | OpenGameArt.org (CC-BY, CC0) |


---

## Session: 2026-03-24 � Batch 2 Features Complete

**AI Assistant:** Kimi (Moonshot AI)
**Student:** Vincent Chamberlain (2424309)

### Work Completed

1. **Tower Synergies** � 6 combo types (Arcane Arrows, Gravity Well, Overclocked, Network, Volley)
2. **Elite Enemies** � 8 modifiers (Fast, Tanky, Vampiric, Explosive, Regen, Shielded, Splitter, Legendary)
3. **Wave Modifiers** � 9 types (Swarm, Elite Wave, Rush, Gold Rush, Horde, etc.)
4. **Dodge/Roll** � SHIFT key with invincibility frames
5. **Ultimate Ability** � RAGE mode (F key) with charge meter
6. **UI Redesign** � Left sidebar with icon buttons, bigger fonts
7. **Fullscreen Mode** � F11 toggle
8. **Bug Fix** � Monster growth off-screen issue
9. **Auto-Setup** � PowerShell scripts for dependency download

### New Files Created
- javatower/systems/TowerSynergyManager.java`n- javatower/systems/WaveModifier.java`n- javatower/entities/EliteModifier.java`n- CHAT_SESSION_2026-03-24_Kimi.md (this chat log)
- Setup.ps1 � Dependency installer
- Start.bat � Simple launcher

### Compilation Status
? All 57 files compiled successfully

See CHAT_SESSION_2026-03-24_Kimi.md for detailed conversation log.

