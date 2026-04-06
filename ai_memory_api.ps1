# Add Memory via REST API
# Works from any device that can reach the server

param(
    [Parameter(Mandatory=$true)]
    [string]$Topic,
    
    [Parameter(Mandatory=$true)]
    [string]$Content,
    
    [string]$Source = "api",
    [string]$Category = "context",
    [string]$Tags = "",
    [int]$Importance = 5,
    [string]$Project = "javatower",
    [string]$ServerUrl = "http://localhost:8001"
)

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

$body = @{
    row = @{
        timestamp = $timestamp
        source = $Source
        category = $Category
        topic = $Topic
        content = $Content
        tags = $Tags
        importance = $Importance
        project = $Project
    }
} | ConvertTo-Json -Depth 3

try {
    $response = Invoke-RestMethod -Uri "$ServerUrl/-/insert/ai_memory/memories" -Method POST -Body $body -ContentType "application/json"
    Write-Host "Memory added successfully!" -ForegroundColor Green
    Write-Host "  Topic: $Topic" -ForegroundColor White
    Write-Host "  Source: $Source" -ForegroundColor Gray
} catch {
    Write-Host "Error adding memory: $_" -ForegroundColor Red
    Write-Host "Is the server running? Start with: .\start_memory_server.ps1" -ForegroundColor Yellow
}
