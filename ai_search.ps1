# AI Memory Search Utility
# Usage: .\ai_search.ps1 "search term"
# Usage: .\ai_search.ps1 -Recent 10
# Usage: .\ai_search.ps1 -Source kimi
# Usage: .\ai_search.ps1 -Category feature

param(
    [Parameter(Position=0)]
    [string]$Query,
    
    [int]$Recent = 0,
    [string]$Source = "",
    [string]$Category = "",
    [switch]$Logs,
    [switch]$Tasks,
    [switch]$Help
)

$dbPath = "ai_memory.db"

if ($Help -or (-not $Query -and $Recent -eq 0 -and -not $Source -and -not $Category -and -not $Logs -and -not $Tasks)) {
    Write-Host @"

AI MEMORY SEARCH
================
Usage:
  .\ai_search.ps1 "search term"     # Full-text search across all memories
  .\ai_search.ps1 -Recent 10        # Show 10 most recent memories
  .\ai_search.ps1 -Source kimi      # Filter by source (copilot, kimi, user)
  .\ai_search.ps1 -Category feature # Filter by category
  .\ai_search.ps1 -Logs            # Show chat log entries
  .\ai_search.ps1 -Tasks           # Show task status

Examples:
  .\ai_search.ps1 "tower synergy"
  .\ai_search.ps1 "hotkey" -Source copilot
  .\ai_search.ps1 -Recent 5 -Category feature

"@
    return
}

Write-Host "`n=== AI Memory Search ===" -ForegroundColor Cyan

if ($Query) {
    Write-Host "Searching for: '$Query'" -ForegroundColor Yellow
    
    # Search memories
    $sql = @"
SELECT printf('%-12s', source) || ' | ' || printf('%-15s', category) || ' | ' || topic
FROM memories 
WHERE id IN (SELECT rowid FROM memories_fts WHERE memories_fts MATCH '$Query')
ORDER BY importance DESC, timestamp DESC
LIMIT 20;
"@
    Write-Host "`nMemories:" -ForegroundColor Green
    sqlite3 $dbPath $sql
    
    # Search chat logs
    $sql = @"
SELECT printf('%-12s', source) || ' | ' || printf('%-10s', session_date) || ' | ' || substr(content, 1, 80) || '...'
FROM chat_logs 
WHERE id IN (SELECT rowid FROM chat_logs_fts WHERE chat_logs_fts MATCH '$Query')
LIMIT 10;
"@
    Write-Host "`nChat Logs (matching sections):" -ForegroundColor Green
    sqlite3 $dbPath $sql
}

if ($Recent -gt 0) {
    Write-Host "Recent $Recent memories:" -ForegroundColor Yellow
    $sql = "SELECT datetime(timestamp) || ' | ' || source || ' | ' || topic FROM memories ORDER BY timestamp DESC LIMIT $Recent;"
    sqlite3 $dbPath $sql
}

if ($Source) {
    Write-Host "Memories from source: $Source" -ForegroundColor Yellow
    $sql = "SELECT printf('%-15s', category) || ' | ' || topic FROM memories WHERE source='$Source' ORDER BY timestamp DESC LIMIT 20;"
    sqlite3 $dbPath $sql
}

if ($Category) {
    Write-Host "Memories in category: $Category" -ForegroundColor Yellow
    $sql = "SELECT printf('%-12s', source) || ' | ' || topic FROM memories WHERE category='$Category' ORDER BY importance DESC LIMIT 20;"
    sqlite3 $dbPath $sql
}

if ($Logs) {
    Write-Host "Chat log entries:" -ForegroundColor Yellow
    $sql = "SELECT printf('%-12s', source) || ' | ' || session_date || ' | ' || file_origin FROM chat_logs ORDER BY timestamp DESC;"
    sqlite3 $dbPath $sql
}

if ($Tasks) {
    Write-Host "Task status:" -ForegroundColor Yellow
    $sql = "SELECT printf('%-12s', status) || ' | ' || printf('%-12s', assigned_to) || ' | ' || title FROM tasks ORDER BY priority DESC, created_at DESC;"
    sqlite3 $dbPath $sql
}

Write-Host ""
