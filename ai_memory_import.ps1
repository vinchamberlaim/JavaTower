# Import Chat Logs into AI Memory Database
# Run this script to consolidate all existing chat logs

$dbPath = "ai_memory.db"

function Escape-SQLString($str) {
    return $str -replace "'", "''"
}

function Import-ChatLog($file, $source, $category) {
    $content = Get-Content $file -Raw -ErrorAction SilentlyContinue
    if (-not $content) { return }
    
    $fileName = Split-Path $file -Leaf
    $escapedContent = Escape-SQLString $content
    
    # Extract date from filename or content if possible
    $sessionDate = "2026-03-24"
    if ($fileName -match "(\d{4}-\d{2}-\d{2})") {
        $sessionDate = $matches[1]
    }
    
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
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  AI Memory Database - Import Tool" -ForegroundColor Green  
Write-Host "========================================`n" -ForegroundColor Green

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
    $escapedTopic = Escape-SQLString $fact.Topic
    $escapedContent = Escape-SQLString $fact.Content
    $sql = @"
INSERT INTO memories (source, category, topic, content, tags, importance)
VALUES ('copilot', 'context', '$escapedTopic', '$escapedContent', '$($fact.Tags)', 9);
"@
    $sql | sqlite3 $dbPath
    Write-Host "  Added: $($fact.Topic)" -ForegroundColor DarkCyan
}

# Summary
Write-Host "`n========================================" -ForegroundColor Green
$counts = sqlite3 $dbPath "SELECT 'Chat logs: ' || COUNT(*) FROM chat_logs; SELECT 'Memories: ' || COUNT(*) FROM memories;"
$counts | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Import complete! Use ai_search.ps1 to query memories." -ForegroundColor Green
