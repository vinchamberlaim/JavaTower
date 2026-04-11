# JavaTower — Presentation Slides

**CIS096-1 — Principles of Programming and Data Structures**
**Assessment 2 — OOP Architecture & Implementation**

> Vincent Chamberlain (2424309) | Nicolas Alfaro (2301126) | Emmanuel Adewumi (2507044)

---

## Slide 1: Title

# JavaTower
### A Real-Time Tower Defence RPG

- **Module**: CIS096-1 — Principles of Programming & Data Structures
- **Assessment**: 2 — OOP Architecture & Implementation
- **Tech Stack**: Java 21 + JavaFX + SQLite
- **Scale**: 58 Java files | 12 packages | 15,000+ lines

---

## Slide 2: What Is JavaTower?

### A tower defence game with RPG elements

- Player controls a **hero** with equipment, skills, and abilities
- Place **4 tower types** to defend against enemies
- Survive **scaling waves up to 1000** with undead + Lovecraft enemies
- Manage a **Tetris-style inventory**, **shop**, **forge**, and **skill trees**
- Game state **persists to SQLite** across 4 save slots

### Why a Game?

- Games are **complex enough** to require real OOP architecture
- Multiple entity types → natural **inheritance hierarchy**
- Real-time systems → practical use of **data structures**
- Many interacting systems → need for **design patterns**

### Code Notation Used In This Deck

- Method notation: `Class::method(params) -> returnType`
- Evidence notation: `[path:line]` from project source
- Complexity notation: `$O(1)$`, `$O(n)$`, etc.
- Pattern notation: `[Factory]`, `[Observer]`, `[Template Method]`

---

## Slide 3: OOP Pillar 1 — ABSTRACTION

### Hiding complexity behind simplified interfaces

```
abstract class Entity {
    abstract void takeTurn();        // Each entity defines its own behaviour
    int takeDamage(int damage);      // Shared damage formula
    protected void onDeath();        // Hook for subclass-specific death
}
```

| Abstraction | Where |
|-------------|-------|
| **Abstract classes** | Entity, Enemy, Tower — define contracts |
| **Abstract methods** | takeTurn(), attack(), selectTarget() |
| **Enums** | EnemyType, Rarity, TargetMode — encapsulate constants + data |
| **Information hiding** | Internal state only accessible via methods |

> **Key insight**: `Entity` has 110 lines of shared logic. Each of the 10 enemy subclasses needs only ~9 lines.

### Code Evidence (Abstraction)

```java
public abstract class Entity {
    public abstract void takeTurn();
    protected void onDeath() {}
}
```

- `Entity::takeTurn() -> void` [javatower/entities/Entity.java:18]
- `Entity::onDeath() -> void` [javatower/entities/Entity.java:69]

---

## Slide 4: OOP Pillar 2 — ENCAPSULATION

### Bundling data with methods that operate on it

```
class Inventory {
    private boolean[][] occupied;     // Cannot access directly
    private Item[][] itemGrid;

    public boolean canPlaceItem(Item, int, int);  // Validates first
    public boolean addItem(Item);                  // Then mutates
}
```

| Encapsulation | Where |
|---------------|-------|
| **Private fields** | All 58 classes use private access |
| **Getters/Setters** | Controlled state access |
| **Validation** | canPlaceItem() before addItem() |
| **Singleton** | DatabaseManager.getInstance() |
| **Immutable config** | Constants.java — static final values |

### Code Evidence (Encapsulation)

```java
private boolean[][] occupied;
private Item[][] itemGrid;

public boolean canPlaceItem(Item item, int x, int y) { ... }
public boolean addItem(Item item) { ... }
```

- `Inventory::canPlaceItem(Item,int,int) -> boolean` [javatower/systems/Inventory.java:25]
- `Inventory::addItem(Item) -> boolean` [javatower/systems/Inventory.java:50]
- Footprint check is bounded by item size (`w*h`) and grid scan is bounded by inventory area (`W*H`)

---

## Slide 5: OOP Pillar 3 — INHERITANCE

### Code reuse through class hierarchies

```
Entity (abstract)
├── Hero                    — player character
├── Enemy (abstract)        — 10 concrete subclasses
│   ├── Zombie              Tier 1
│   ├── Skeleton            Tier 2
│   ├── ...
│   ├── Lich                Tier 8 — unique bone-seeking AI
│   └── NecromancerKing     Tier 10 — final boss
│
└── Tower (abstract)        — 4 concrete subclasses
    ├── ArrowTower          Fast single-target
    ├── MagicTower          Hits phasing enemies
    ├── SiegeTower          Slow AoE
    └── SupportTower        Heals + buffs
```

> **3-level hierarchy**: Entity → Enemy/Tower → 14 concrete subclasses
> Each level **adds** functionality. No code is duplicated.

### Code Evidence (Inheritance + Factory)

```java
switch (type) {
    case GHOUL: enemy = new Ghoul(waveLevel); break;
    case LICH: enemy = new Lich(waveLevel); break;
    case SHOGGOTH: enemy = new Shoggoth(waveLevel); break;
}
```

- `EnemyFactory::createEnemy(EnemyType,int,WaveModifier) -> Enemy` [javatower/factories/EnemyFactory.java:16] [Factory]

---

## Slide 6: OOP Pillar 4 — POLYMORPHISM

### Same method call, different behaviour

```java
// All towers respond to attack() differently:
ArrowTower.attack()  → fires single projectile at target
SiegeTower.attack()  → AoE explosion hitting all nearby enemies
SupportTower.attack() → heals the hero instead

// In the game loop — one line handles all tower types:
for (Tower tower : towers) {
    tower.update(dt, enemies);  // Calls correct subclass method
}
```

| Polymorphism Type | Example |
|-------------------|---------|
| **Method overriding** | Tower.attack() — 4 different implementations |
| **Subtype polymorphism** | List&lt;Enemy&gt; holds 10 different types |
| **Factory polymorphism** | EnemyFactory returns correct subclass |
| **Strategy via enum** | TargetMode changes selectTarget() behaviour |

### Code Evidence (Polymorphism)

```java
for (Enemy e : new ArrayList<>(enemies)) {
    e.update(dt, hero);
}
```

- `GameGUI::gameUpdate(double) -> void` [javatower/gui/GameGUI.java:538]
- Dynamic dispatch call site: [javatower/gui/GameGUI.java:564]

---

## Slide 7: Design Patterns (11 Applied)

| Pattern | Class | Purpose |
|---------|-------|---------|
| **Abstract Factory** | EnemyFactory, TowerFactory | Create entities without knowing concrete type |
| **Factory Method** | ItemFactory | Delegate item creation to specialised methods |
| **Singleton** | DatabaseManager | Single DB connection |
| **Observer** | GameEventBus&lt;T&gt; | Decoupled pub/sub events |
| **Strategy** | TargetMode, TowerSynergyManager | Swappable algorithms |
| **Template Method** | Entity.takeDamage() / onDeath() | Algorithm skeleton + hooks |
| **Decorator** | EliteModifier | Dynamically modify enemy stats |
| **State** | GameState enum | Game phase management |
| **Composite** | Inventory grid | Uniform item/cell treatment |
| **DTO** | GameState, SaveSlotInfo | Data transfer objects |
| **MVC** | GameGUI / GameBoard / Entities | Separate logic from rendering |

---

## Slide 8: What We Improved Recently

### Reliability + UX fixes (latest sprint)

- **No item loss transactions** during equip/swap/unequip
- **Safe inventory overflow handling** (auto-expand fallback for drops/returns)
- **Wave-end autosave upgrades**: inventory, equipped items, skill trees, and skill progression now persisted
- **Load-from-autosave restore path** with legacy fallback support
- **Control clarity improvements** in the in-game action hints and feedback log

### Why this matters for marking

- Demonstrates robust state management, not only feature quantity
- Shows defensive programming and data integrity in a live system

### Code Evidence (Reliability)

- No-loss equipment swap:
    `Hero::equipItemWithDisplaced(Item) -> List<Item>`
    [javatower/entities/Hero.java:934]
- Inventory save serialization:
    `SaveGameManager::serializeInventory(Hero) -> String`
    [javatower/systems/SaveGameManager.java:271]
- Inventory restore path:
    `SaveGameManager::deserializeInventory(Hero,String) -> void`
    [javatower/systems/SaveGameManager.java:424]

---

## Slide 9: Data Structures

| Structure | Usage | Complexity |
|-----------|-------|-----------|
| **ArrayList&lt;T&gt;** | Enemies, towers, effects, items | O(1) access, O(1) amortised append |
| **HashMap&lt;K,V&gt;** | Stat bonuses, event listeners, skill XP | O(1) average lookup |
| **2D boolean[][]** | Inventory occupancy grid | O(1) cell access |
| **2D Item[][]** | Inventory item references | O(1) cell access |
| **Enum with data** | EnemyType, Rarity — constants + values | O(1) access |
| **LinkedHashMap** | Combat log — insertion-ordered | O(1) insert + ordered iteration |
| **HashSet&lt;String&gt;** | Unlocked items tracking | O(1) membership test |

### Why These Choices?

- 60 FPS game loop = **16ms budget per frame**
- Need O(1) lookups for stat calculations every frame
- ArrayList for iteration-heavy entity rendering
- 2D arrays for fixed-size grid — no Collection overhead

---

## Slide 10: Architecture & Database

### MVC Architecture

```
Controller:  GameGUI (game loop, input, state)
View:        GameBoard (canvas), UI Panels (8 screens)
Model:       Entity hierarchy, Systems (inventory, waves, skills)
```

### SQLite Database (4 Save Slots)

```
meta_progression  → unlocked items, max wave, total gold
save_state        → hero stats, wave number, timestamp
tower_data        → tower positions and upgrades
hero_data         → full hero serialisation (JSON)
```

### Key Infrastructure

- **GameEventBus** — Observer pattern decouples 10+ event types
- **SaveGameManager** — Serialises complex game state to SQLite
- **GameBalanceConfig** — 50+ balance constants in one place

---

## Slide 11: Demo & Summary

### Live Demo

1. Main menu → New Game
2. Place towers (1–4 keys)
3. Fight wave (hero auto-attacks, WASD to move)
4. Between waves: Shop → Inventory → Skill Tree → Forge
5. Show autosave/reload preserving gear + skills
6. Boss/Late-wave enemy showcase with visible scaling

### Demo Speaking Notation

- "No item loss is handled by `Hero::equipItemWithDisplaced` [javatower/entities/Hero.java:934]."
- "Enemy runtime updates are dispatched in `GameGUI::gameUpdate` [javatower/gui/GameGUI.java:564]."
- "Save payload is built and restored by `SaveGameManager` at [javatower/systems/SaveGameManager.java:271] and [javatower/systems/SaveGameManager.java:424]."

### Summary Statistics

| Metric | Value |
|--------|-------|
| Java source files | 58 |
| Packages | 12 |
| Lines of code | 15,000+ |
| OOP pillars demonstrated | 4/4 |
| Design patterns applied | 11 |
| Data structures used | 7 |
| Enemy types (inheritance) | 10 subclasses |
| Additional late-wave enemy set | Cultist, Deep One, Shoggoth |
| Tower types (polymorphism) | 4 subclasses |
| Game waves | Scales to 1000 |
| Save slots | 4 (SQLite) |

### Assessment Criteria Met

- **Abstraction**: Abstract classes, abstract methods, enums, information hiding
- **Encapsulation**: Private fields, controlled access, validation, Singleton
- **Inheritance**: 3-level hierarchy, 14 concrete subclasses, code reuse
- **Polymorphism**: Method overriding, subtype polymorphism, factories, strategy
- **Data Structures**: ArrayList, HashMap, 2D arrays, LinkedHashMap, HashSet, enums
- **Design Patterns**: 11 patterns (Factory, Observer, Singleton, Strategy, Template, Decorator, State, Composite, DTO, MVC)
- **Persistence**: SQLite via JDBC with save/load and wave-end autosave
- **GUI**: JavaFX canvas + 8 UI screens

---

## Slide 12: Team Delivery Plan (Video)

### Suggested split for recorded contributions

1. **Vincent**: architecture + game loop + systems integration
2. **Nicolas**: enemy/tower behaviour + combat design
3. **Emmanuel**: items/inventory/persistence data flow

### Recording approach

- Each member records their own section
- Merge into one final submission with title cards per speaker
- Keep sections short, technical, and evidence-based (show code + running game)

---

## Thank You

**Repository**: [github.com/vinchamberlaim/JavaTower](https://github.com/vinchamberlaim/JavaTower)

| Name | Student ID |
|------|------------|
| Vincent Chamberlain | 2424309 |
| Nicolas Alfaro | 2301126 |
| Emmanuel Adewumi | 2507044 |
