# Import Chat Logs into AI Memory Database
# Incremental import with duplicate protection

$dbPath = "ai_memory.db"

function Escape-SQLString($str) {
    return $str -replace "'", "''"
}

function Ensure-ImportStateTable {
    $sql = @"
CREATE TABLE IF NOT EXISTS import_state (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_origin TEXT NOT NULL,
    source TEXT NOT NULL,
    last_hash TEXT,
    last_size INTEGER,
    last_write_time TEXT,
    imported_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(file_origin, source)
);
"@
    $sql | sqlite3 $dbPath | Out-Null
}

function Get-FileHashSafe($filePath) {
    try {
        return (Get-FileHash -Path $filePath -Algorithm SHA256).Hash
    }
    catch {
        return ""
    }
}

function Get-SessionDate($fileName, $content) {
    if ($fileName -match "(\d{4}-\d{2}-\d{2})") {
        return $matches[1]
    }

    if ($content -match "(?im)^\*\*Date:\*\*\s*(\d{4}-\d{2}-\d{2})") {
        return $matches[1]
    }

    return (Get-Date -Format "yyyy-MM-dd")
}

function Should-ImportFile($filePath, $fileName, $source) {
    $hash = Get-FileHashSafe $filePath
    $size = (Get-Item $filePath).Length
    $writeTime = (Get-Item $filePath).LastWriteTimeUtc.ToString("o")

    $escapedFile = Escape-SQLString $fileName
    $escapedSource = Escape-SQLString $source
    $existing = sqlite3 $dbPath "SELECT COALESCE(last_hash,'') || '|' || COALESCE(last_size,0) || '|' || COALESCE(last_write_time,'') FROM import_state WHERE file_origin='$escapedFile' AND source='$escapedSource' LIMIT 1;"

    if (-not $existing) {
        return @{ ShouldImport = $true; Hash = $hash; Size = $size; WriteTime = $writeTime }
    }

    $parts = $existing -split "\|", 3
    $oldHash = if ($parts.Count -ge 1) { $parts[0] } else { "" }
    $oldSize = if ($parts.Count -ge 2) { [string]$parts[1] } else { "0" }
    $oldWrite = if ($parts.Count -ge 3) { $parts[2] } else { "" }

    $changed = ($hash -ne $oldHash) -or ([string]$size -ne $oldSize) -or ($writeTime -ne $oldWrite)
    return @{ ShouldImport = $changed; Hash = $hash; Size = $size; WriteTime = $writeTime }
}

function Update-ImportState($fileName, $source, $hash, $size, $writeTime) {
    $escapedFile = Escape-SQLString $fileName
    $escapedSource = Escape-SQLString $source
    $escapedHash = Escape-SQLString $hash
    $escapedWrite = Escape-SQLString $writeTime

    $sql = @"
INSERT INTO import_state (file_origin, source, last_hash, last_size, last_write_time, imported_at)
VALUES ('$escapedFile', '$escapedSource', '$escapedHash', $size, '$escapedWrite', CURRENT_TIMESTAMP)
ON CONFLICT(file_origin, source)
DO UPDATE SET
    last_hash = excluded.last_hash,
    last_size = excluded.last_size,
    last_write_time = excluded.last_write_time,
    imported_at = CURRENT_TIMESTAMP;
"@

    $sql | sqlite3 $dbPath | Out-Null
}

function Add-MemoryIfMissing($source, $category, $topic, $content, $tags, $importance) {
    $escapedSource = Escape-SQLString $source
    $escapedCategory = Escape-SQLString $category
    $escapedTopic = Escape-SQLString $topic
    $escapedContent = Escape-SQLString $content

    $existsSql = @"
SELECT COUNT(*)
FROM memories
WHERE source='$escapedSource'
  AND category='$escapedCategory'
  AND topic='$escapedTopic'
  AND content='$escapedContent';
"@

    $exists = ($existsSql | sqlite3 $dbPath)
    if ($exists -and [int]$exists -gt 0) {
        return $false
    }

    $escapedTags = Escape-SQLString $tags
    $insertSql = @"
INSERT INTO memories (source, category, topic, content, tags, importance)
VALUES ('$escapedSource', '$escapedCategory', '$escapedTopic', '$escapedContent', '$escapedTags', $importance);
"@
    $insertSql | sqlite3 $dbPath | Out-Null
    return $true
}

function Import-ChatLog($file, $source, $category) {
    $content = Get-Content $file -Raw -ErrorAction SilentlyContinue
    if (-not $content) { return }
    
    $fileName = Split-Path $file -Leaf
    $check = Should-ImportFile $file $fileName $source
    if (-not $check.ShouldImport) {
        Write-Host "  Skipping unchanged: $fileName" -ForegroundColor DarkGray
        return
    }

    $escapedContent = Escape-SQLString $content
    $escapedFileName = Escape-SQLString $fileName
    $escapedSource = Escape-SQLString $source
    
    # Extract date from filename or content if possible
    $sessionDate = Get-SessionDate $fileName $content
    
    Write-Host "  Importing: $fileName ($([int]($content.Length/1024))KB)" -ForegroundColor Cyan
    
    # Insert into chat_logs
    $sql = @"
INSERT INTO chat_logs (source, session_date, file_origin, role, content)
VALUES ('$source', '$sessionDate', '$fileName', 'full_log', '$escapedContent');
"@
    
    $sql | sqlite3 $dbPath
    
    # Also extract key memories/insights
    $lines = $content -split "`n"
    $currentSection = ""
    
    foreach ($line in $lines) {
        if ($line -match "^##\s+(.+)") {
            $currentSection = $matches[1]
        }
        elseif ($line -match "^###\s+.*Feature.*#(\d+).*:?\s*(.+)" -or 
                $line -match "^\|\s*(\d+)\s*\|\s*(.+?)\s*\|") {
            $topic = "$source Feature: $($matches[2])"
            $escapedTopic = Escape-SQLString $topic
            $sql = @"
INSERT INTO memories (source, category, topic, content, tags, importance)
VALUES ('$source', 'feature', '$escapedTopic', '$escapedTopic', 'feature,implemented', 7);
"@
            $sql | sqlite3 $dbPath
        }
    }

    Update-ImportState $fileName $source $check.Hash $check.Size $check.WriteTime
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  AI Memory Database - Import Tool" -ForegroundColor Green  
Write-Host "========================================`n" -ForegroundColor Green

Ensure-ImportStateTable

# Import all chat log files
$logFiles = @(
    @{Path="COPILOT_CHAT_LOG.md"; Source="copilot"; Category="session"},
    @{Path="AI_CHAT_LOG_VINCENT_2424309.md"; Source="kimi"; Category="session"},
    @{Path="CHAT_SESSION_2026-03-24_Kimi.md"; Source="kimi"; Category="session"},
    @{Path="KIMI_REVIEW_SUMMARY.md"; Source="kimi"; Category="review"},
    @{Path="KIMI_HANDOFF.md"; Source="kimi"; Category="handoff"},
    @{Path="KIMI_TOP20_FLAGS.md"; Source="kimi"; Category="tracking"},
    @{Path="SESSION_SUMMARY.md"; Source="shared"; Category="summary"},
    @{Path="DEVLOG.md"; Source="user"; Category="devlog"}
)

Write-Host "Importing chat logs..." -ForegroundColor Yellow

foreach ($log in $logFiles) {
    if (Test-Path $log.Path) {
        Import-ChatLog $log.Path $log.Source $log.Category
    }
}

# Import key project facts as memories
Write-Host "`nImporting project context..." -ForegroundColor Yellow

$projectFacts = @(
    @{Topic="Build Command"; Content="javac --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics -cp lib\sqlite-jdbc-3.45.1.0.jar -d out2 @sources.txt"; Tags="build,compile,java"},
    @{Topic="Project Structure"; Content="Main packages: javatower/gui, javatower/entities, javatower/systems, javatower/factories, javatower/config, javatower/data, javatower/events, javatower/util"; Tags="structure,packages"},
    @{Topic="Key Classes"; Content="GameGUI (main), GameBoard (rendering), Hero, Enemy, Tower + subclasses, WaveManager, CombatSystem, Inventory, Shop"; Tags="classes,architecture"},
    @{Topic="AI Workflow"; Content="Copilot and Kimi collaborate via KIMI_HANDOFF.md. Tasks tracked in dev_tasks.json. Game improvements in game_improvements.json."; Tags="workflow,coordination"},
    @{Topic="Hotkeys Implemented"; Content="1-4=towers, Q=melee AoE, W=ranged special, E=heal, R=boost, S=sell, ESC=pause, TAB=shop, SHIFT=dodge, F=ultimate, Arrow keys=move"; Tags="controls,hotkeys"},
    @{Topic="Database"; Content="SQLite via sqlite-jdbc-3.45.1.0.jar in lib/. DatabaseManager.java handles save/load. Tables: game_saves, slot_info, heroes"; Tags="database,sqlite"}
)

foreach ($fact in $projectFacts) {
    $added = Add-MemoryIfMissing "copilot" "context" $fact.Topic $fact.Content $fact.Tags 9
    if ($added) {
        Write-Host "  Added: $($fact.Topic)" -ForegroundColor DarkCyan
    }
    else {
        Write-Host "  Skipping existing context: $($fact.Topic)" -ForegroundColor DarkGray
    }
}

# Summary
Write-Host "`n========================================" -ForegroundColor Green
$counts = sqlite3 $dbPath "SELECT 'Chat logs: ' || COUNT(*) FROM chat_logs; SELECT 'Memories: ' || COUNT(*) FROM memories;"
$counts | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Import complete! Use ai_search.ps1 to query memories." -ForegroundColor Green
