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
- Survive **30 waves** of undead enemies (Zombie → Necromancer King)
- Manage a **Tetris-style inventory**, **shop**, **forge**, and **skill trees**
- Game state **persists to SQLite** across 4 save slots

### Why a Game?

- Games are **complex enough** to require real OOP architecture
- Multiple entity types → natural **inheritance hierarchy**
- Real-time systems → practical use of **data structures**
- Many interacting systems → need for **design patterns**

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

## Slide 8: Data Structures

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

## Slide 9: Architecture & Database

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

## Slide 10: Demo & Summary

### Live Demo

1. Main menu → New Game
2. Place towers (1–4 keys)
3. Fight wave (hero auto-attacks, WASD to move)
4. Between waves: Shop → Inventory → Skill Tree → Forge
5. Boss fight at wave 5 with unique drops

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
| Tower types (polymorphism) | 4 subclasses |
| Game waves | 30 |
| Save slots | 4 (SQLite) |

### Assessment Criteria Met

- **Abstraction**: Abstract classes, abstract methods, enums, information hiding
- **Encapsulation**: Private fields, controlled access, validation, Singleton
- **Inheritance**: 3-level hierarchy, 14 concrete subclasses, code reuse
- **Polymorphism**: Method overriding, subtype polymorphism, factories, strategy
- **Data Structures**: ArrayList, HashMap, 2D arrays, LinkedHashMap, HashSet, enums
- **Design Patterns**: 11 patterns (Factory, Observer, Singleton, Strategy, Template, Decorator, State, Composite, DTO, MVC)
- **Persistence**: SQLite via JDBC with save/load
- **GUI**: JavaFX canvas + 8 UI screens

---

## Thank You

**Repository**: [github.com/vinchamberlaim/JavaTower](https://github.com/vinchamberlaim/JavaTower)

| Name | Student ID |
|------|------------|
| Vincent Chamberlain | 2424309 |
| Nicolas Alfaro | 2301126 |
| Emmanuel Adewumi | 2507044 |
