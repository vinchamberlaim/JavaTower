# AI Shared Memory System

**Location:** `C:\Users\Vincent\OneDrive\Desktop\Java\ai_memory.db`  
**Created:** April 6, 2026  
**Purpose:** Persistent memory database shared between Copilot, Kimi, and other AI assistants

---

## Quick Reference

### Search Memories
```powershell
.\ai_search.ps1 "search term"           # Full-text search
.\ai_search.ps1 -Recent 10              # Last 10 memories
.\ai_search.ps1 -Source kimi            # Kimi's entries only
.\ai_search.ps1 -Source copilot         # Copilot's entries only
.\ai_search.ps1 -Category feature       # Feature implementations
.\ai_search.ps1 -Logs                   # List all chat logs
```

### Add New Memory
```powershell
.\ai_add_memory.ps1 -Topic "Topic" -Content "Details" -Source "kimi" -Category "decision"
```

### Log Current Chat Immediately
```powershell
.\ai_log_chat.ps1 -Content "Short summary of this session" -Source copilot -Role session_note -FileOrigin manual -AlsoMemory
```

### Direct SQL (for AI assistants)
```powershell
# Search
sqlite3 ai_memory.db "SELECT topic, content FROM memories WHERE topic LIKE '%tower%';"

# Add memory
sqlite3 ai_memory.db "INSERT INTO memories (source, category, topic, content, tags) VALUES ('kimi', 'task', 'Working on X', 'Details here', 'tag1,tag2');"

# View recent
sqlite3 ai_memory.db "SELECT datetime(timestamp), source, topic FROM memories ORDER BY timestamp DESC LIMIT 10;"
```

---

## Database Schema

### `memories` - Main memory storage
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Auto-increment primary key |
| timestamp | DATETIME | When created (auto) |
| source | TEXT | 'copilot', 'kimi', 'claude', 'user' |
| session_id | TEXT | Groups related entries |
| category | TEXT | 'code', 'decision', 'context', 'task', 'error', 'solution', 'feature' |
| topic | TEXT | Short searchable title |
| content | TEXT | Full content |
| tags | TEXT | Comma-separated tags |
| importance | INTEGER | 1-10 (default 5) |
| project | TEXT | Default 'javatower' |

### `chat_logs` - Full conversation archives
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Auto-increment primary key |
| timestamp | DATETIME | When imported |
| source | TEXT | 'copilot', 'kimi', etc. |
| session_date | DATE | Original session date |
| file_origin | TEXT | Original filename |
| role | TEXT | 'user', 'assistant', 'full_log' |
| content | TEXT | Full text |

### `tasks` - Task tracking
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| title | TEXT | Task name |
| status | TEXT | 'pending', 'in_progress', 'completed', 'blocked' |
| assigned_to | TEXT | 'copilot', 'kimi', 'user' |
| priority | INTEGER | 1-10 |

### `decisions` - Code decisions/rationale
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| decision | TEXT | What was decided |
| rationale | TEXT | Why |
| files_affected | TEXT | Affected files |

---

## Full-Text Search

The database uses FTS5 for fast full-text search:
- `memories_fts` - Indexes topic, content, tags, category
- `chat_logs_fts` - Indexes content, role

Example searches:
```sql
-- Search memories
SELECT * FROM memories WHERE id IN (
  SELECT rowid FROM memories_fts WHERE memories_fts MATCH 'tower AND synergy'
);

-- Search chat logs
SELECT * FROM chat_logs WHERE id IN (
  SELECT rowid FROM chat_logs_fts WHERE chat_logs_fts MATCH 'hotkey'
);
```

---

## For Kimi (Copy this to start a session)

**AI Memory System Active!**

I can read/write to a shared SQLite database at `ai_memory.db`. To use it:

```powershell
# Search past context
.\ai_search.ps1 "topic"

# Add what I learn/do
.\ai_add_memory.ps1 -Topic "Topic" -Content "What I did" -Source "kimi" -Category "task"

# Direct SQL query
sqlite3 ai_memory.db "SELECT topic FROM memories WHERE source='copilot' ORDER BY timestamp DESC LIMIT 5;"
```

At the END of each session, run:
```powershell
.\ai_add_memory.ps1 -Topic "Session Summary" -Content "Describe what was accomplished" -Source "kimi" -Category "session" -Importance 8
```

---

## For Copilot

I automatically check this database at session start. Key commands:

```powershell
# Check what Kimi did
.\ai_search.ps1 -Source kimi -Recent 10

# Record my work
.\ai_add_memory.ps1 -Topic "Implemented X" -Content "Details" -Source "copilot" -Category "feature"
```

---

## Server Deployment (Future)

To sync across devices, options:
1. **Dropbox/OneDrive** - Put ai_memory.db in synced folder (current location already on OneDrive!)
2. **Remote SQLite** - Use LiteFS or rqlite for distributed SQLite
3. **PostgreSQL** - Migrate schema to remote PostgreSQL
4. **Supabase** - Free tier PostgreSQL with REST API

The OneDrive location (`C:\Users\Vincent\OneDrive\Desktop\Java`) already syncs across devices!

---

## Files

| File | Purpose |
|------|---------|
| `ai_memory.db` | SQLite database |
| `ai_search.ps1` | Search utility |
| `ai_add_memory.ps1` | Add new memories |
| `ai_log_chat.ps1` | Add direct chat/session log entry |
| `ai_memory_import.ps1` | Bulk import chat logs |
| `setup_memory_db.sql` | Schema definition |
| `AI_MEMORY_README.md` | This documentation |

---

*Database contains 71 memories and 8 chat log archives as of April 6, 2026*
