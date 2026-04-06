# Start AI Memory Database Server
# Run this to start the REST API server for the AI memory database
# Default: http://localhost:8001

param(
    [int]$Port = 8001,
    [string]$Host = "0.0.0.0"  # 0.0.0.0 allows external connections (for port forwarding)
)

$datasette = "C:/Users/Vincent/AppData/Local/Programs/Python/Python313/Scripts/datasette.exe"
$db = "$PSScriptRoot\ai_memory.db"

Write-Host "Starting AI Memory Database Server..." -ForegroundColor Cyan
Write-Host "  Database: $db" -ForegroundColor Gray
Write-Host "  URL: http://${Host}:${Port}" -ForegroundColor Green
Write-Host ""
Write-Host "API Endpoints:" -ForegroundColor Yellow
Write-Host "  Browse:     http://localhost:$Port" -ForegroundColor White
Write-Host "  Memories:   http://localhost:$Port/ai_memory/memories.json" -ForegroundColor White
Write-Host "  Chat Logs:  http://localhost:$Port/ai_memory/chat_logs.json" -ForegroundColor White
Write-Host "  Search:     http://localhost:$Port/ai_memory/memories.json?_search=QUERY" -ForegroundColor White
Write-Host "  SQL:        http://localhost:$Port/ai_memory?sql=SELECT+*+FROM+memories" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Gray
Write-Host ""

# Start datasette with:
# - write access enabled for inserts
# - CORS enabled for cross-origin requests
# - binding to specified host/port
& $datasette $db --host $Host --port $Port --cors --setting allow_download off
