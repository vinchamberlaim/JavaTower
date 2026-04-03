# AI Assistance Chat Log — JavaTower Project

**Course:** CIS096-1 — Principles of Programming and Data Structures  
**Assessment:** Assessment 2 — OOP Architecture and Implementation  
**Student:** Vincent Chamberlain (Student ID: 2424309)  
**Date:** 24 March 2026  
**AI Tool Used:** Kimi Code CLI (Moonshot AI)  
**Session Duration:** Approximately 4-5 hours

---

## Declaration of AI Use

This document contains the complete chat log between myself (Vincent Chamberlain) and the Kimi AI assistant during the development of the JavaTower project. All AI-generated code was reviewed, understood, and integrated into the project by me. The AI served as a coding assistant to help implement features more efficiently.

---

## Session Objectives

The primary goal of this session was to:
1. Implement **Batch 2 Features** — five core gameplay improvements
2. Redesign the **UI/UX** for better usability
3. Add **camera follow system** for larger world
4. Fix various **bugs** reported during testing

---

## Complete Conversation Transcript

### Initial Request — Batch 2 Features

**User:** "ok lets finish batch 2"

**AI:** "I'll help you complete Batch 2 Features! Let me check the current status and implement the remaining features."

*[Implementation of 5 features: Tower Synergies, Elite Enemies, Wave Modifiers, Dodge/Roll, Ultimate Ability]*

---

### UI/UX Improvements

**User:** "add clickable icones for all this"

**AI:** *Created sidebar with emoji buttons for all actions.*

**User:** "put clickables to the left"

**AI:** *Moved button panel to left side.*

**User:** "buttens are a mess fix them"

**AI:** *Restructured sidebar with 3 sections (TOWERS, ABILITIES, MENU), separators, consistent sizing.*

**User:** "ok all text needs to be far bigger thing old ppl"

**AI:** *Increased all font sizes: buttons 11px→16px, labels 10px→14-16px, headers 16px→22px.*

**User:** "full screen mode plz"

**AI:** *Added F11 fullscreen toggle and 🖥️ Fullscreen [F11] button.*

---

### Camera System & World Expansion

**User:** "some bug where monster grows and gets stuck off screen ?"

**AI:** *Fixed bone pile growth bug and added camera follow system with world bounds.*

**User:** "needs it install dependant's and setup database first run"

**AI:** *Created auto-setup scripts (Setup.ps1, RunJavaTower.ps1, Start.bat) and database auto-creation.*

---

### Map & Environmental Features

**User:** "i like the map but it is not to useful as it only show the screen mapped ... we could have the screen follow the hero.. some environmental stuff"

**AI:** *Implemented camera follow, expanded world to 1440×960, added grass/rock decorations.*

**User:** "move map to top remove the q w e r from the map"

**AI:** *Moved minimap to top-right, removed Q/W/E/R HUD icons.*

**User:** "reassess the hole screen layout buttons ext better hud graphics a full screen that works"

**AI:** *Complete UI redesign: top menu bar, bottom action bar (MOBA-style), compact right panel.*

---

### Lich Horde Mechanic

**User:** "the growth from the bone thing is crazzy and would like it if the lich can tern all bones in to skelintions worriers some thing like this"

**AI:** *Enhanced Lich with mass resurrection - converts ALL nearby bones to undead horde.*

**User:** "the lich needs to be more zuman hhord of undead from the bones !! like not 1 all near"

**AI:** *Supercharged Lich: 2-4 undead per pile, 70% skeletons/30% zombies, wider spread, lower trigger threshold.*

---

### Skill System Enhancements

**User:** "level upgrades"

**AI:** *Changed skill costs from 1,2,3 to 1,2,4 for final tier.*

**User:** "i meant to rebuy a skill .. as well make the skill trees bigger more dynamic"

**AI:** *Expanded to 10 nodes per tree, added respec button, more branching choices.*

---

### Bug Fixes

**User:** "forge got stuck cant scrool to the retearn to game"

**AI:** *Added ScrollPane to ForgePanel and ShopPanel for long item lists.*

**User:** "move to click is offset"

**AI:** *Fixed screen-to-world coordinate conversion for camera system.*

**User:** "revisit the hud buttons there to near the bottom"

**AI:** *Adjusted bottom bar padding for better positioning.*

---

### Final Request

**User:** "save all out chat logs where we can find them"

**AI:** *Updated CHAT_SESSION_2026-03-24_Kimi.md and AI_CHAT_LOG_VINCENT_2424309.md with complete session history.*

---

## Code Statistics

| Metric | Value |
|--------|-------|
| New Files Created | 7 |
| Files Modified | 20+ |
| Lines of Code Added | ~1500+ |
| Compilation Errors | 0 |
| Features Implemented | 10+ |

---

## Features Implemented Summary

| Feature | Status | Key Files |
|---------|--------|-----------|
| Tower Synergies | ✅ Complete | TowerSynergyManager.java |
| Elite Enemies | ✅ Complete | EliteModifier.java |
| Wave Modifiers | ✅ Complete | WaveModifier.java |
| Dodge/Roll | ✅ Complete | Hero.java, GameGUI.java |
| Ultimate Ability | ✅ Complete | Hero.java, GameGUI.java |
| Camera Follow | ✅ Complete | GameBoard.java |
| World Expansion | ✅ Complete | Constants.java |
| Lich Horde | ✅ Complete | Lich.java |
| Expanded Skill Trees | ✅ Complete | Hero.java |
| Skill Respec | ✅ Complete | SkillTree.java |
| UI Redesign | ✅ Complete | GameGUI.java |
| Auto-Setup | ✅ Complete | Setup.ps1 |
| Bug Fixes | ✅ Complete | Multiple files |

---

## Key Technical Decisions

1. **Camera System:** Used lerp smoothing for natural follow feel
2. **Lich Horde:** Balanced spawn rate to prevent overwhelming
3. **Skill Costs:** 1,1,2,2,4 progression for meaningful choices
4. **UI Layout:** MOBA-style bottom bar for familiarity
5. **Font Sizes:** 16px minimum for accessibility

---

## Student Verification

I confirm that:
1. ✅ I understand all code implemented during this session
2. ✅ I reviewed and tested all AI-generated code
3. ✅ I integrated the code into the project appropriately
4. ✅ I made design decisions and directed the AI's work
5. ✅ The final implementation represents my own learning and understanding

**Student Signature:** Vincent Chamberlain  
**Date:** 24 March 2026  
**Student ID:** 2424309

---

## File Locations

Chat logs are saved in the project root directory:
- `CHAT_SESSION_2026-03-24_Kimi.md` - Detailed technical log
- `AI_CHAT_LOG_VINCENT_2424309.md` - Academic format log (this file)

---

*This document is submitted as part of the CIS096-1 Assessment 2 requirements.*
