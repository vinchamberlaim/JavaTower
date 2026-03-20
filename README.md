# JavaTower — Tower Defence RPG

**CIS096-1 — Principles of Programming and Data Structures**  
**Assessment 2 — OOP Architecture and Implementation (Week 9)**

| Field | Details |
|-------|---------|
| Student Name | Vincent Chamberlain |
| Student ID | 2424309 |
| Unit Code | CIS096-1 |
| Submission | Week 9 — OOP Architecture & Implementation |

## Overview

JavaTower is a real-time tower defence RPG built with **Java 21** and **JavaFX**. The player controls a hero navigating through 30 waves of undead enemies, placing defensive towers, managing a Tetris-style inventory, purchasing equipment from a shop, and progressing through skill trees. Game state is persisted via **SQLite (JDBC)**.

## How to Build & Run

### Prerequisites
- **Java 21** (OpenJDK)
- **JavaFX SDK 21.0.2** — place in `javafx-sdk/` folder
- **SQLite JDBC 3.45.1.0** — place in `lib/sqlite-jdbc-3.45.1.0.jar`

### Compile
```bash
javac --module-path "javafx-sdk/lib" --add-modules javafx.controls,javafx.graphics -cp "lib/sqlite-jdbc-3.45.1.0.jar" -d out2 @sources.txt
```

### Run
```bash
java --module-path "javafx-sdk/lib" --add-modules javafx.controls,javafx.graphics -cp "out2;lib/sqlite-jdbc-3.45.1.0.jar" Main
```

Or use the provided `RunJavaTower.bat` / `RunJavaTower.ps1` scripts.

## Project Structure

```
JavaTower/
├── Main.java                        # Application entry point
├── sources.txt                      # Compilation file list
├── RunJavaTower.bat / .ps1          # Launch scripts
├── javatower/
│   ├── util/                        # Constants, GameState enum
│   ├── entities/                    # Entity, Hero, Enemy, Item, Tower, BonePile
│   │   ├── enemies/                 # 10 enemy subclasses (Zombie → NecromancerKing)
│   │   └── towers/                  # 4 tower subclasses (Arrow, Magic, Siege, Support)
│   ├── systems/                     # Inventory, Shop, WaveManager, SkillTree, CombatSystem
│   ├── factories/                   # EnemyFactory, ItemFactory, TowerFactory
│   ├── gui/                         # GameGUI, GameBoard, all UI panels
│   └── database/                    # DatabaseManager (SQLite/JDBC)
```

## Documentation

See [JavaTower_Architecture_Documentation.md](JavaTower_Architecture_Documentation.md) for the full OOP architecture documentation including class diagrams, design patterns, and implementation details.
