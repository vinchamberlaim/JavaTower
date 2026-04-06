# AI Memory Add Utility
# Usage: .\ai_add_memory.ps1 -Topic "Topic" -Content "Content" -Source "copilot"

param(
    [Parameter(Mandatory=$true)]
    [string]$Topic,
    
    [Parameter(Mandatory=$true)]
    [string]$Content,
    
    [string]$Source = "copilot",
    [string]$Category = "note",
    [string]$Tags = "",
    [int]$Importance = 5,
    [string]$SessionId = ""
)

$dbPath = "ai_memory.db"

function Escape-SQLString($str) {
    return $str -replace "'", "''"
}

$escapedTopic = Escape-SQLString $Topic
$escapedContent = Escape-SQLString $Content
$escapedTags = Escape-SQLString $Tags
$escapedSession = Escape-SQLString $SessionId

$sql = @"
INSERT INTO memories (source, session_id, category, topic, content, tags, importance)
VALUES ('$Source', '$escapedSession', '$Category', '$escapedTopic', '$escapedContent', '$escapedTags', $Importance);
"@

$sql | sqlite3 $dbPath

if ($LASTEXITCODE -eq 0) {
    Write-Host "Memory added successfully!" -ForegroundColor Green
    Write-Host "  Topic: $Topic" -ForegroundColor Cyan
    Write-Host "  Source: $Source" -ForegroundColor Cyan
    Write-Host "  Category: $Category" -ForegroundColor Cyan
} else {
    Write-Host "Failed to add memory!" -ForegroundColor Red
}
