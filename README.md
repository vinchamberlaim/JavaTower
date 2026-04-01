# JavaTower — Tower Defence RPG

**CIS096-1 — Principles of Programming and Data Structures**  
**Assessment 2 — OOP Architecture and Implementation (Week 9)**

| Field | Details |
|-------|---------|
| Unit Code | CIS096-1 |
| Submission | Week 9 — OOP Architecture & Implementation |

### Group Members

| Name | Student ID |
|------|------------|
| Vincent Chamberlain | 2424309 |
| Nicolas Alfaro | 2301126 |
| Emmanuel Adewumi | 2507044 |

## Overview

JavaTower is a real-time tower defence RPG built with **Java 21** and **JavaFX**. The player controls a hero navigating through 30 waves of undead enemies, placing defensive towers, managing a Tetris-style inventory, purchasing equipment from a shop, and progressing through skill trees. Game state is persisted via **SQLite (JDBC)**.

## Quick Start (First Time Setup)

### Option 1: Easy Launch (Recommended)
Just double-click **`RunJavaTower.bat`** — it will automatically:
1. ✓ Check for Java 21+
2. ✓ Download JavaFX SDK (if missing)
3. ✓ Download SQLite JDBC (if missing)
4. ✓ Compile all source files
5. ✓ Create database on first run
6. ✓ Launch the game!

### Option 2: PowerShell
```powershell
.\RunJavaTower.ps1
```

### Option 3: Manual Setup
If you prefer manual setup:

```powershell
# 1. Run setup script to download dependencies
.\Setup.ps1

# 2. Compile
javac --module-path "javafx-sdk/lib" --add-modules javafx.controls,javafx.graphics -cp "lib/sqlite-jdbc-3.45.1.0.jar" -d out2 @sources.txt

# 3. Run (database auto-creates on first run)
java --module-path "javafx-sdk/lib" --add-modules javafx.controls,javafx.graphics -cp "out2;lib/sqlite-jdbc-3.45.1.0.jar" Main
```

## Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| Java | 21+ | https://adoptium.net/ |
| JavaFX SDK | 21.0.2 | Auto-downloaded |
| SQLite JDBC | 3.45.1.0 | Auto-downloaded |

**Note:** The game database (`javatower.db`) is created automatically on first run. No manual database setup required!

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
