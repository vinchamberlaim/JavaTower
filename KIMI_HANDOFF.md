# Kimi Handoff Prompt — JavaTower

Copy everything below this line and paste it to Kimi:

---

## Context

You are working on **JavaTower**, a JavaFX tower defense RPG in `C:\Users\Vincent\OneDrive\Desktop\Java`. The project compiles with:

```
javac --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics -cp lib\sqlite-jdbc-3.45.1.0.jar -d out2 @sources.txt
```

## What You Already Did (uncommitted)

Your previous session added these changes (still uncommitted on master):
- **GameEventBus.java** — observer pattern event bus (NEW file)
- **Logger.java** — logging utility with DEBUG/INFO/WARN/ERROR (NEW file)
- **GameBalanceConfig.java** — centralized balance constants (NEW file)
- **GameBoard.java** — screen shake system (`applyScreenShake`, `updateScreenShake`), tower placement preview (ghost tower + range circle), mouse tracking
- **GameGUI.java** — keyboard hotkeys (1-4 towers, Q=melee AoE skill, W=ranged AoE special ability, ESC=cancel, TAB=shop), `useSpecialAbility()`, `setTowerPlacementMode()`
- **sources.txt** — added your 3 new files

## What Copilot Just Added (on top of your changes)

Copilot implemented these features **without modifying any of your code** — only added new switch cases and new methods:

### Hero.java
- **Arrow key movement**: `moveUp/Down/Left/Right` boolean fields + directional movement in `update()` that overrides click-to-move while keys are held
- **Kill stats**: `totalKills`, `totalDamageDealt`, `totalGoldEarned`, `totalXPEarned` fields with `recordKill()` method and getters

### GameGUI.java
- **setupHotkeys additions**: Arrow keys (UP/DOWN/LEFT/RIGHT for movement), E (self heal), R (tower boost), S (sell tower) — added as new cases after your existing SPACE case, plus a `setOnKeyReleased` handler for arrow key release
- **selfHeal()** — E key: 20 mana, heals 30% max HP, shows heal number
- **towerBoost()** — R key: 25 mana, doubles ALL tower damage for 5 seconds, screen shake feedback
- **sellTower()** — S key: sells selected tower for 50% of upgrade cost refund
- **Kill stats** in `showGameOver()` — displays kills, damage dealt, gold/XP earned
- **Action bar** — added Sell (S), Heal (E), Boost (R) buttons
- **Tower boost timer** — `towerBoostTimer`, `boostedTowers`, `boostedOriginalDamage` fields with restoration logic in `gameUpdate()`
- Kill recording on enemy death via `hero.recordKill()`

### GameBoard.java
- **Enemy hover tooltip** — `drawEnemyHoverInfo()` renders a tooltip box showing name, HP, ATK, tier when mouse is near an enemy. Added `FontWeight` import.

## Coordination File

Read `dev_tasks.json` in the project root for full task tracking. Update it when you start/complete tasks.

## Key Bindings Summary (current state)
| Key | Action | Owner |
|-----|--------|-------|
| 1-4 | Place tower types | Kimi |
| Q | Melee AoE skill (15 mana) | Kimi |
| W | Ranged AoE special (25 mana) | Kimi |
| E | Self heal (20 mana, 30% HP) | Copilot |
| R | Tower boost (25 mana, 5s 2x dmg) | Copilot |
| S | Sell selected tower (50% refund) | Copilot |
| ESC | Cancel tower placement | Kimi |
| TAB | Open shop | Kimi |
| Arrow keys | Continuous movement | Copilot |
| Mouse click | Move hero / place tower / select tower | Original |

## Suggested Tasks for You

Pick from these (also listed in `dev_tasks.json`):

1. **Minimap** — small corner overlay showing hero (green dot), towers (blue), enemies (red) positions
2. **Procedural pixel sprites** — replace colored circles with generated pixel art for enemies
3. **Sound effects** — use `javafx.scene.media.AudioClip` for attack, hit, death, level-up sounds
4. **Wave countdown timer HUD** — show remaining time/enemies in a persistent HUD overlay
5. **Enemy loot tables** — enemies drop items on death (probability-based), auto-add to inventory
6. **Projectile trail particles** — add fading trail dots behind arrow/magic/siege projectiles
7. **Save/load tower placements** — persist tower grid positions to SQLite alongside hero data

## Rules
- **Do NOT modify** Copilot's new methods (`selfHeal`, `towerBoost`, `sellTower`, `drawEnemyHoverInfo`, `recordKill`) or the arrow key / E / R / S hotkey cases
- **Do** add new features by appending new methods, new files, or new switch cases for unused keys
- **Update** `dev_tasks.json` with your task status as you work
- **Update** `sources.txt` if you create new files
- Compile frequently with the command above to catch errors early
- The project prints `java.sql.SQLException: No suitable driver found` at startup — this is a pre-existing classpath issue with SQLite; the game handles it gracefully with try/catch

---
