# Dual-AI Workflow — JavaTower Project

**CIS096-1 — Principles of Programming and Data Structures**
**Assessment 2 — OOP Architecture & Implementation**

---

## Overview

JavaTower was developed using a **dual-AI collaborative workflow** where two AI coding assistants — **GitHub Copilot** (Claude) and **Kimi** — worked in parallel on the same codebase under the direction of the developer (Vincent Chamberlain). This document describes how the workflow was structured, what rules governed collaboration, and what each AI contributed.

---

## Why Two AIs?

Using two AI assistants in parallel allowed:

1. **Faster development** — while one AI was implementing features, the other could work on a separate batch
2. **Specialisation** — each AI focused on different areas of the codebase
3. **Cross-validation** — features implemented by one AI were reviewed by the other
4. **Broader coverage** — 100 improvement ideas were brainstormed and divided between them

---

## Workflow Structure

```
┌──────────────────────────────────────────────────────────┐
│                     DEVELOPER (Vincent)                   │
│                                                          │
│  1. Creates game_improvements.json (100 ideas)           │
│  2. Selects Top 20 for implementation                    │
│  3. Assigns tasks to each AI                             │
│  4. Reviews and tests output                             │
│  5. Commits and pushes to Git                            │
└──────────────┬───────────────────────┬───────────────────┘
               │                       │
       ┌───────▼───────┐       ┌───────▼───────┐
       │   KIMI (AI 1) │       │ COPILOT (AI 2)│
       │               │       │               │
       │ Systems &     │       │ Visuals &     │
       │ Architecture  │       │ Polish &      │
       │               │       │ Documentation │
       └───────┬───────┘       └───────┬───────┘
               │                       │
               └───────────┬───────────┘
                           │
                    ┌──────▼──────┐
                    │  SHARED     │
                    │  CODEBASE   │
                    │  (Git repo) │
                    └─────────────┘
```

---

## Collaboration Rules

### File Ownership

To prevent merge conflicts and code corruption, files were divided into three categories:

#### Kimi-Exclusive Files (Copilot must NOT edit)
| File | Purpose |
|------|---------|
| GameEventBus.java | Observer event system |
| Logger.java | Logging utility |
| GameBalanceConfig.java | Balance constants |
| MiniMap.java | Mini-map overlay |
| SaveGameManager.java | Save/load system |
| SaveSlotInfo.java | Save slot metadata |
| GameState.java (data) | Save DTO |
| CombatLogPanel.java | Combat event log |
| PixelArtRenderer.java | Sprite rendering |
| TowerSynergyManager.java | Tower synergy logic |
| WaveModifier.java | Wave difficulty modifiers |
| EliteModifier.java | Enemy elite modifiers |
| GameMetrics.java | Analytics tracking |

#### Shared Files (APPEND ONLY — both AIs may add, neither may edit existing code)
| File | Purpose |
|------|---------|
| GameGUI.java | Main application + game loop |
| GameBoard.java | Canvas renderer |
| DatabaseManager.java | SQLite persistence |

#### Copilot-Owned Files (Kimi should not edit)
| File | Purpose |
|------|---------|
| FINAL_REPORT.md | Academic report |
| PRESENTATION.md | Presentation slides |
| COPILOT_CHAT_LOG.md | Copilot session log |
| AI_WORKFLOW.md | This file |

#### Shared-Edit Files (both AIs modified with care)
| File | Purpose |
|------|---------|
| Hero.java | Player character |
| Enemy.java | Abstract enemy |
| Tower.java | Abstract tower |
| VisualEffect.java | Visual effects |
| ItemFactory.java | Item generation |
| EnemyFactory.java | Enemy creation |
| WaveManager.java | Wave spawning |
| Constants.java | Game constants |
| sources.txt | Compilation file list |
| README.md | Project documentation |

### Communication Protocol

1. **Handoff documents** — When one AI completed a batch, a handoff .md file was created for the other (e.g., `KIMI_HANDOFF.md`, `COPILOT_HANDOFF_SAVE_SYSTEM.md`)
2. **Flag files** — `KIMI_TOP20_FLAGS.md` and `ACTIVE_IMPLEMENTATION_FLAGS.md` tracked which features were in progress to prevent duplicate work
3. **Task JSON** — `dev_tasks.json` maintained a machine-readable list of all tasks with ownership and status
4. **Review summaries** — `KIMI_REVIEW_SUMMARY.md` documented completed work for cross-review

---

## AI Contributions

### Kimi — Systems & Architecture

Kimi focused on core game systems, infrastructure, and backend architecture:

| Feature | Files |
|---------|-------|
| Observer event system | GameEventBus.java |
| Logging utility | Logger.java |
| Game balance configuration | GameBalanceConfig.java |
| Mini-map system | MiniMap.java |
| Wave countdown timer | WaveInfoPanel.java |
| Tower range visualisation | GameBoard.java |
| Pause menu | GameGUI.java |
| Combat log panel | CombatLogPanel.java |
| Projectile trail system | VisualEffect.java |
| Tower synergy system | TowerSynergyManager.java |
| Elite enemy modifiers | EliteModifier.java |
| Wave modifiers | WaveModifier.java |
| Dodge/roll mechanic | Hero.java, GameGUI.java |
| Ultimate ability | Hero.java, GameGUI.java |
| Pixel art rendering system | PixelArtRenderer.java |
| Save system (SQLite) | SaveGameManager.java, DatabaseManager.java |
| Screen shake + placement preview | GameBoard.java |
| Hotkey system (1-4, Q, W, ESC, TAB, M) | GameGUI.java |
| Per-wave analytics | GameMetrics.java |

**Total: ~19 features across ~20 files**

### Copilot — Visuals, Polish & Documentation

Copilot focused on visual effects, HUD, polish features, and academic documentation:

| Feature | Files |
|---------|-------|
| Hero pixel-art procedural sprite | GameBoard.java |
| Death dissolve animation | VisualEffect.java, GameGUI.java |
| Level-up golden flash effect | Hero.java, VisualEffect.java, GameGUI.java |
| Equipment-based hero colour | GameBoard.java |
| Enemy item drops (tier-based %) | GameGUI.java, ItemFactory.java |
| Passive mana regeneration | Hero.java |
| Bone consumption growth | Enemy.java |
| Lich bone-seeking AI | Lich.java |
| Tower targeting toggle | Tower.java + 3 subclasses |
| Tower kill counter + tooltip | Tower.java + 3 subclasses, GameBoard.java |
| Tower sell confirmation | GameGUI.java, GameBoard.java |
| Boss legendary drops | ItemFactory.java |
| Unique boss items (4 named) | ItemFactory.java |
| Ability cooldown HUD | GameBoard.java, GameGUI.java |
| Mana bar on hero | GameBoard.java |
| Gold change notification | GameGUI.java |
| Run timer display | GameGUI.java, GameBoard.java |
| README.md (comprehensive rewrite) | README.md |
| Final academic report | FINAL_REPORT.md |
| Presentation slides | PRESENTATION.md |

**Total: 17 code features + 3 documentation files across ~15 files**

---

## Timeline

| Date | Event | AI |
|------|-------|----|
| 20 Mar 2026 | Initial submission (commit `057b409`) | Manual |
| 20 Mar 2026 | Equipment sets, forge, skill progression (commit `190ed70`) | Manual |
| 20 Mar 2026 | Equipment overhaul (commit `fe43a12`) | Manual |
| 22–24 Mar 2026 | Batch 1: Wave timer, range viz, pause, combat log, projectiles | Kimi |
| 24 Mar 2026 | Batch 2: Synergies, elite enemies, wave mods, dodge, ultimate | Kimi |
| 24 Mar 2026 | Quality pass + pixel art system | Kimi |
| 24–31 Mar 2026 | Save system, hotkeys, mini-map, analytics | Kimi |
| 31 Mar 2026 | 16 features implemented in rapid session | Copilot |
| 31 Mar 2026 | Commit `540d717` — all features pushed | Copilot |
| 1 Apr 2026 | README, final report, presentation created | Copilot |
| 1 Apr 2026 | Commit `c9692c2` — documentation pushed | Copilot |
| 1 Apr 2026 | Chat logs and workflow documentation | Both |

---

## Quality Assurance

### Preventing Conflicts
- File ownership rules prevented both AIs from editing the same code
- Shared files used APPEND ONLY — new methods added at the end, existing code untouched
- `sources.txt` updated when new files were created
- Compilation checked after every batch of changes

### Testing
- Game compiled with zero errors after every AI session
- Game launched and manually tested 6+ times
- All 30 waves playable with new features active

### Integration Verification
- Copilot checked Kimi's flag files before starting work
- Kimi left handoff documents for Copilot
- Both AIs used `game_improvements.json` as the single source of truth for task status

---

## Chat Logs

| AI | Log File | Contents |
|----|----------|----------|
| **Copilot** | [COPILOT_CHAT_LOG.md](COPILOT_CHAT_LOG.md) | Full session history — planning, implementation, documentation |
| **Kimi** | [KIMI_CHAT_LOG.md](KIMI_CHAT_LOG.md) | *(Ask Kimi to save its chat log to this file)* |

---

## Conclusion

The dual-AI workflow proved effective for this project. By dividing responsibilities — Kimi on systems/architecture and Copilot on visuals/documentation — the team achieved 36+ feature implementations across 58 Java files in a compressed timeline. The file ownership rules and handoff protocol prevented conflicts while allowing both AIs to work on the same codebase simultaneously.

The key success factors were:
1. **Clear file ownership** — no merge conflicts
2. **Handoff documents** — context preserved between sessions
3. **Single task tracker** — `game_improvements.json` as source of truth
4. **Frequent compilation** — errors caught immediately
5. **Manual testing** — developer verified all output
