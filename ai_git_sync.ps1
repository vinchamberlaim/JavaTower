# AI Memory Git Sync
# Syncs local SQLite database with a Git repository for cross-device sharing
#
# Usage:
#   .\ai_git_sync.ps1 push    # Export to JSON and push to remote
#   .\ai_git_sync.ps1 pull    # Pull from remote and import to local DB
#   .\ai_git_sync.ps1 status  # Check sync status
#   .\ai_git_sync.ps1 init    # Initialize the Git repo for memories

param(
    [Parameter(Position=0)]
    [ValidateSet("push", "pull", "status", "init", "")]
    [string]$Action = "status"
)

$MemoryDir = Join-Path $PSScriptRoot "ai_memories_repo"
$LocalDB = Join-Path $PSScriptRoot "ai_memory.db"
$MemoriesJson = Join-Path $MemoryDir "memories.json"
$ChatLogsJson = Join-Path $MemoryDir "chat_logs.json"
$TasksJson = Join-Path $MemoryDir "tasks.json"
$DeviceName = $env:COMPUTERNAME

function Write-Status($msg, $color = "Cyan") {
    Write-Host $msg -ForegroundColor $color
}

function Export-ToJson {
    Write-Status "Exporting local database to JSON..."
    
    # Export memories
    $memories = sqlite3 $LocalDB -json "SELECT * FROM memories ORDER BY timestamp;"
    if ($memories) {
        $memories | Out-File $MemoriesJson -Encoding utf8
        Write-Status "  Exported memories.json" "Green"
    }
    
    # Export chat logs
    $logs = sqlite3 $LocalDB -json "SELECT id, timestamp, source, session_date, file_origin, role, tokens_approx FROM chat_logs ORDER BY timestamp;"
    if ($logs) {
        $logs | Out-File $ChatLogsJson -Encoding utf8
        Write-Status "  Exported chat_logs.json (metadata only)" "Green"
    }
    
    # Export tasks
    $tasks = sqlite3 $LocalDB -json "SELECT * FROM tasks ORDER BY created_at;"
    if ($tasks) {
        $tasks | Out-File $TasksJson -Encoding utf8
        Write-Status "  Exported tasks.json" "Green"
    }
}

function Import-FromJson {
    Write-Status "Importing JSON to local database..."
    
    if (Test-Path $MemoriesJson) {
        $memories = Get-Content $MemoriesJson -Raw | ConvertFrom-Json
        $imported = 0
        
        foreach ($m in $memories) {
            # Check if already exists (by timestamp + source + topic)
            $exists = sqlite3 $LocalDB "SELECT COUNT(*) FROM memories WHERE timestamp='$($m.timestamp)' AND source='$($m.source)' AND topic='$($m.topic -replace "'","''")'"
            
            if ($exists -eq "0") {
                $imp = if ($m.importance) { $m.importance } else { 5 }
                $proj = if ($m.project) { $m.project } else { "javatower" }
                $sql = @"
INSERT INTO memories (timestamp, source, session_id, category, topic, content, tags, importance, project)
VALUES ('$($m.timestamp)', '$($m.source)', '$($m.session_id)', '$($m.category)', 
        '$($m.topic -replace "'","''")', '$($m.content -replace "'","''")', 
        '$($m.tags)', $imp, '$proj');
"@
                sqlite3 $LocalDB $sql 2>$null
                $imported++
            }
        }
        Write-Status "  Imported $imported new memories" "Green"
    }
    
    if (Test-Path $TasksJson) {
        $tasks = Get-Content $TasksJson -Raw | ConvertFrom-Json
        $imported = 0
        
        foreach ($t in $tasks) {
            $exists = sqlite3 $LocalDB "SELECT COUNT(*) FROM tasks WHERE title='$($t.title -replace "'","''")' AND created_at='$($t.created_at)'"
            
            if ($exists -eq "0") {
                $pri = if ($t.priority) { $t.priority } else { 5 }
                $sql = @"
INSERT INTO tasks (created_at, updated_at, source, title, description, status, priority, assigned_to, related_files, notes)
VALUES ('$($t.created_at)', '$($t.updated_at)', '$($t.source)', 
        '$($t.title -replace "'","''")', '$($t.description -replace "'","''")',
        '$($t.status)', $pri, '$($t.assigned_to)', '$($t.related_files)', '$($t.notes -replace "'","''")');
"@
                sqlite3 $LocalDB $sql 2>$null
                $imported++
            }
        }
        Write-Status "  Imported $imported new tasks" "Green"
    }
}

switch ($Action) {
    "init" {
        Write-Status "`n=== Initializing AI Memory Git Repo ===" "Yellow"
        
        if (-not (Test-Path $MemoryDir)) {
            New-Item -ItemType Directory -Path $MemoryDir | Out-Null
        }
        
        Push-Location $MemoryDir
        
        if (-not (Test-Path ".git")) {
            git init
            
            # Create .gitignore
            @"
*.db
*.db-journal
*.log
"@ | Out-File ".gitignore" -Encoding utf8
            
            # Create README
            @"
# AI Shared Memory Repository

This repository stores AI assistant memories for cross-device synchronization.

## Structure
- `memories.json` - Main memory entries
- `chat_logs.json` - Chat log metadata  
- `tasks.json` - Task tracking

## Usage
From the JavaTower project directory:
``````powershell
.\ai_git_sync.ps1 push    # Upload local changes
.\ai_git_sync.ps1 pull    # Download remote changes
.\ai_git_sync.ps1 status  # Check sync status
``````

## Devices
This is synced across: $DeviceName
"@ | Out-File "README.md" -Encoding utf8
            
            git add .
            git commit -m "Initial AI memory repository"
            
            Write-Status "`nRepo initialized at: $MemoryDir" "Green"
            Write-Status "`nNext steps:" "Yellow"
            Write-Status "1. Create a GitHub repo (e.g., 'ai-memories')" "White"
            Write-Status "2. Run: cd ai_memories_repo" "White"
            Write-Status "3. Run: git remote add origin https://github.com/YOUR_USERNAME/ai-memories.git" "White"
            Write-Status "4. Run: git push -u origin master" "White"
        }
        else {
            Write-Status "Git repo already exists at $MemoryDir" "Yellow"
        }
        
        Pop-Location
    }
    
    "push" {
        Write-Status "`n=== Pushing AI Memory to Git ===" "Yellow"
        
        if (-not (Test-Path "$MemoryDir\.git")) {
            Write-Status "No git repo found. Run: .\ai_git_sync.ps1 init" "Red"
            return
        }
        
        # Export current state
        Export-ToJson
        
        Push-Location $MemoryDir
        
        # Git operations
        git add -A
        $status = git status --porcelain
        
        if ($status) {
            $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm"
            $memCount = (Get-Content $MemoriesJson -Raw | ConvertFrom-Json).Count
            git commit -m "Sync from $DeviceName @ $timestamp ($memCount memories)"
            
            # Try to push
            $remotes = git remote
            if ($remotes) {
                Write-Status "Pushing to remote..." "Cyan"
                git push 2>&1
                if ($LASTEXITCODE -eq 0) {
                    Write-Status "Successfully pushed to remote!" "Green"
                }
                else {
                    Write-Status "Push failed - check your remote settings" "Red"
                }
            }
            else {
                Write-Status "No remote configured. Changes committed locally." "Yellow"
                Write-Status "Add remote: git remote add origin YOUR_REPO_URL" "White"
            }
        }
        else {
            Write-Status "No changes to push." "Yellow"
        }
        
        Pop-Location
    }
    
    "pull" {
        Write-Status "`n=== Pulling AI Memory from Git ===" "Yellow"
        
        if (-not (Test-Path "$MemoryDir\.git")) {
            Write-Status "No git repo found. Run: .\ai_git_sync.ps1 init" "Red"
            return
        }
        
        Push-Location $MemoryDir
        
        $remotes = git remote
        if ($remotes) {
            Write-Status "Pulling from remote..." "Cyan"
            git pull 2>&1
            
            if ($LASTEXITCODE -eq 0) {
                Pop-Location
                Import-FromJson
                Write-Status "Sync complete!" "Green"
            }
            else {
                Write-Status "Pull failed - check for conflicts" "Red"
                Pop-Location
            }
        }
        else {
            Write-Status "No remote configured." "Yellow"
            Pop-Location
        }
    }
    
    "status" {
        Write-Status "`n=== AI Memory Sync Status ===" "Yellow"
        
        # Local DB stats
        if (Test-Path $LocalDB) {
            $memCount = sqlite3 $LocalDB "SELECT COUNT(*) FROM memories;"
            $logCount = sqlite3 $LocalDB "SELECT COUNT(*) FROM chat_logs;"
            $taskCount = sqlite3 $LocalDB "SELECT COUNT(*) FROM tasks;"
            
            Write-Status "`nLocal Database:" "Cyan"
            Write-Status "  Memories: $memCount"
            Write-Status "  Chat Logs: $logCount"
            Write-Status "  Tasks: $taskCount"
        }
        else {
            Write-Status "Local database not found!" "Red"
        }
        
        # Git status
        if (Test-Path "$MemoryDir\.git") {
            Push-Location $MemoryDir
            
            Write-Status "`nGit Repository:" "Cyan"
            $branch = git branch --show-current
            Write-Status "  Branch: $branch"
            
            $remotes = git remote -v
            if ($remotes) {
                Write-Status "  Remote: $($remotes[0])"
            }
            else {
                Write-Status "  Remote: (not configured)" "Yellow"
            }
            
            $status = git status --porcelain
            if ($status) {
                Write-Status "  Status: Uncommitted changes" "Yellow"
            }
            else {
                Write-Status "  Status: Clean" "Green"
            }
            
            Pop-Location
        }
        else {
            Write-Status "`nGit repo not initialized. Run: .\ai_git_sync.ps1 init" "Yellow"
        }
        
        Write-Status ""
    }
    
    default {
        Write-Status @"

AI Memory Git Sync
==================
Usage:
  .\ai_git_sync.ps1 init     Initialize git repo for memories
  .\ai_git_sync.ps1 push     Export DB and push to remote
  .\ai_git_sync.ps1 pull     Pull from remote and import to DB
  .\ai_git_sync.ps1 status   Show sync status

"@
    }
}
