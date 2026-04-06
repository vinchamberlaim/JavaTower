-- AI Shared Memory Database
-- For use by Copilot, Kimi, and other AI assistants

-- Main memory entries
CREATE TABLE IF NOT EXISTS memories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    source TEXT NOT NULL,
    session_id TEXT,
    category TEXT,
    topic TEXT,
    content TEXT NOT NULL,
    tags TEXT,
    importance INTEGER DEFAULT 5,
    project TEXT DEFAULT 'javatower'
);

-- Chat log archive
CREATE TABLE IF NOT EXISTS chat_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    source TEXT NOT NULL,
    session_date DATE,
    file_origin TEXT,
    role TEXT,
    content TEXT NOT NULL,
    tokens_approx INTEGER
);

-- Task tracking
CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    source TEXT,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending',
    priority INTEGER DEFAULT 5,
    assigned_to TEXT,
    related_files TEXT,
    notes TEXT
);

-- Code decisions/rationale
CREATE TABLE IF NOT EXISTS decisions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    source TEXT,
    decision TEXT NOT NULL,
    rationale TEXT,
    alternatives_considered TEXT,
    files_affected TEXT,
    reversible BOOLEAN DEFAULT 1
);

-- Full-text search indexes
CREATE VIRTUAL TABLE IF NOT EXISTS memories_fts USING fts5(
    topic, content, tags, category,
    content='memories',
    content_rowid='id'
);

CREATE VIRTUAL TABLE IF NOT EXISTS chat_logs_fts USING fts5(
    content, role,
    content='chat_logs',
    content_rowid='id'
);

-- Triggers to keep FTS in sync
CREATE TRIGGER IF NOT EXISTS memories_ai AFTER INSERT ON memories BEGIN
    INSERT INTO memories_fts(rowid, topic, content, tags, category) 
    VALUES (new.id, new.topic, new.content, new.tags, new.category);
END;

CREATE TRIGGER IF NOT EXISTS chat_logs_ai AFTER INSERT ON chat_logs BEGIN
    INSERT INTO chat_logs_fts(rowid, content, role) 
    VALUES (new.id, new.content, new.role);
END;

-- Initial metadata
INSERT INTO memories (source, category, topic, content, tags, importance)
VALUES ('copilot', 'context', 'Database Created', 
        'AI shared memory database initialized. Schema supports memories, chat logs, tasks, and decisions with full-text search.',
        'setup,database,initialization', 10);
