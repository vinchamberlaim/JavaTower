# AI Chat Log Utility
# Usage:
#   .\ai_log_chat.ps1 -Content "Session summary text"
#   .\ai_log_chat.ps1 -Content "..." -Source copilot -Role session_note -FileOrigin manual

param(
    [Parameter(Mandatory=$true)]
    [string]$Content,

    [string]$Source = "copilot",
    [string]$Role = "session_note",
    [string]$FileOrigin = "manual",
    [string]$SessionDate = "",
    [switch]$AlsoMemory,
    [string]$MemoryTopic = "Session Summary",
    [string]$MemoryCategory = "session",
    [int]$MemoryImportance = 8
)

$dbPath = "ai_memory.db"

function Escape-SQLString($str) {
    return $str -replace "'", "''"
}

if (-not (Test-Path $dbPath)) {
    Write-Host "Database not found: $dbPath" -ForegroundColor Red
    exit 1
}

if (-not $SessionDate) {
    $SessionDate = Get-Date -Format "yyyy-MM-dd"
}

$escapedContent = Escape-SQLString $Content
$escapedSource = Escape-SQLString $Source
$escapedRole = Escape-SQLString $Role
$escapedFileOrigin = Escape-SQLString $FileOrigin
$escapedSessionDate = Escape-SQLString $SessionDate

$tokensApprox = [int]([Math]::Ceiling(($Content.Length) / 4.0))

$sql = @"
INSERT INTO chat_logs (source, session_date, file_origin, role, content, tokens_approx)
VALUES ('$escapedSource', '$escapedSessionDate', '$escapedFileOrigin', '$escapedRole', '$escapedContent', $tokensApprox);
"@

$sql | sqlite3 $dbPath

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to insert chat log entry." -ForegroundColor Red
    exit 1
}

Write-Host "Chat log entry saved." -ForegroundColor Green
Write-Host "  Source: $Source" -ForegroundColor Cyan
Write-Host "  Role: $Role" -ForegroundColor Cyan
Write-Host "  Session Date: $SessionDate" -ForegroundColor Cyan
Write-Host "  Tokens Approx: $tokensApprox" -ForegroundColor Cyan

if ($AlsoMemory) {
    $escapedTopic = Escape-SQLString $MemoryTopic
    $escapedCategory = Escape-SQLString $MemoryCategory

    $memSql = @"
INSERT INTO memories (source, category, topic, content, importance)
VALUES ('$escapedSource', '$escapedCategory', '$escapedTopic', '$escapedContent', $MemoryImportance);
"@

    $memSql | sqlite3 $dbPath

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Also saved to memories table." -ForegroundColor Green
    }
    else {
        Write-Host "Chat log saved, but memories insert failed." -ForegroundColor Yellow
    }
}
