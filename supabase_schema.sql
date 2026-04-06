-- Run this in Supabase SQL Editor to create the cloud tables
-- Go to: Your Project > SQL Editor > New Query > Paste this > Run

-- Main memory entries
CREATE TABLE IF NOT EXISTS memories (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    source TEXT NOT NULL,
    session_id TEXT,
    category TEXT,
    topic TEXT,
    content TEXT NOT NULL,
    tags TEXT,
    importance INTEGER DEFAULT 5,
    project TEXT DEFAULT 'javatower',
    device TEXT,
    synced_at TIMESTAMPTZ DEFAULT NOW()
);

-- Chat log archive
CREATE TABLE IF NOT EXISTS chat_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    source TEXT NOT NULL,
    session_date DATE,
    file_origin TEXT,
    role TEXT,
    content TEXT NOT NULL,
    tokens_approx INTEGER,
    device TEXT,
    synced_at TIMESTAMPTZ DEFAULT NOW()
);

-- Task tracking
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    source TEXT,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending',
    priority INTEGER DEFAULT 5,
    assigned_to TEXT,
    related_files TEXT,
    notes TEXT,
    device TEXT
);

-- Code decisions/rationale
CREATE TABLE IF NOT EXISTS decisions (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    source TEXT,
    decision TEXT NOT NULL,
    rationale TEXT,
    alternatives_considered TEXT,
    files_affected TEXT,
    reversible BOOLEAN DEFAULT TRUE,
    device TEXT
);

-- Sync tracking table
CREATE TABLE IF NOT EXISTS sync_log (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    device TEXT NOT NULL,
    action TEXT NOT NULL,
    records_synced INTEGER,
    direction TEXT
);

-- Enable Row Level Security (optional but recommended)
ALTER TABLE memories ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE decisions ENABLE ROW LEVEL SECURITY;

-- Create policies to allow all operations with anon key (simple setup)
CREATE POLICY "Allow all for memories" ON memories FOR ALL USING (true);
CREATE POLICY "Allow all for chat_logs" ON chat_logs FOR ALL USING (true);
CREATE POLICY "Allow all for tasks" ON tasks FOR ALL USING (true);
CREATE POLICY "Allow all for decisions" ON decisions FOR ALL USING (true);

-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_memories_source ON memories(source);
CREATE INDEX IF NOT EXISTS idx_memories_category ON memories(category);
CREATE INDEX IF NOT EXISTS idx_memories_timestamp ON memories(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_chat_logs_source ON chat_logs(source);

-- Full-text search (PostgreSQL native)
ALTER TABLE memories ADD COLUMN IF NOT EXISTS fts tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', coalesce(topic,'') || ' ' || coalesce(content,'') || ' ' || coalesce(tags,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_memories_fts ON memories USING GIN(fts);

ALTER TABLE chat_logs ADD COLUMN IF NOT EXISTS fts tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', coalesce(content,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_chat_logs_fts ON chat_logs USING GIN(fts);

-- Insert success marker
INSERT INTO memories (source, category, topic, content, importance, device)
VALUES ('system', 'setup', 'Cloud Database Ready', 'Supabase cloud database initialized and ready for cross-device sync.', 10, 'cloud');
