# AI Memory Cloud Operations - Supabase REST API
# This module provides functions for cloud database operations

$script:ConfigPath = Join-Path $PSScriptRoot ".env.supabase"
$script:DeviceName = $env:COMPUTERNAME

function Get-SupabaseConfig {
    if (-not (Test-Path $script:ConfigPath)) {
        Write-Host "Config not found at $script:ConfigPath" -ForegroundColor Red
        Write-Host "Run: .\ai_cloud_setup.ps1" -ForegroundColor Yellow
        return $null
    }
    
    $config = @{}
    Get-Content $script:ConfigPath | ForEach-Object {
        if ($_ -match "^([^#=]+)=(.+)$") {
            $config[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
    
    if (-not $config.SUPABASE_URL -or -not $config.SUPABASE_ANON_KEY) {
        Write-Host "Missing SUPABASE_URL or SUPABASE_ANON_KEY in .env.supabase" -ForegroundColor Red
        return $null
    }
    
    return $config
}

function Invoke-SupabaseQuery {
    param(
        [string]$Table,
        [string]$Select = "*",
        [string]$Filter = "",
        [string]$Order = "",
        [int]$Limit = 100
    )
    
    $config = Get-SupabaseConfig
    if (-not $config) { return $null }
    
    $url = "$($config.SUPABASE_URL)/rest/v1/$Table"
    $params = @("select=$Select")
    if ($Filter) { $params += $Filter }
    if ($Order) { $params += "order=$Order" }
    if ($Limit) { $params += "limit=$Limit" }
    
    $fullUrl = $url + "?" + ($params -join "&")
    
    $headers = @{
        "apikey" = $config.SUPABASE_ANON_KEY
        "Authorization" = "Bearer $($config.SUPABASE_ANON_KEY)"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-RestMethod -Uri $fullUrl -Headers $headers -Method Get
        return $response
    }
    catch {
        Write-Host "Query failed: $_" -ForegroundColor Red
        return $null
    }
}

function Add-SupabaseRecord {
    param(
        [string]$Table,
        [hashtable]$Data
    )
    
    $config = Get-SupabaseConfig
    if (-not $config) { return $false }
    
    $url = "$($config.SUPABASE_URL)/rest/v1/$Table"
    
    $Data.device = $script:DeviceName
    
    $headers = @{
        "apikey" = $config.SUPABASE_ANON_KEY
        "Authorization" = "Bearer $($config.SUPABASE_ANON_KEY)"
        "Content-Type" = "application/json"
        "Prefer" = "return=representation"
    }
    
    try {
        $body = $Data | ConvertTo-Json -Depth 10
        $response = Invoke-RestMethod -Uri $url -Headers $headers -Method Post -Body $body
        return $response
    }
    catch {
        Write-Host "Insert failed: $_" -ForegroundColor Red
        return $null
    }
}

function Search-SupabaseMemories {
    param(
        [string]$Query,
        [string]$Source = "",
        [string]$Category = "",
        [int]$Limit = 20
    )
    
    $config = Get-SupabaseConfig
    if (-not $config) { return $null }
    
    $url = "$($config.SUPABASE_URL)/rest/v1/memories"
    $params = @("select=id,timestamp,source,category,topic,content,importance")
    
    if ($Query) {
        # Use PostgreSQL full-text search
        $params += "fts=fts.@.$Query"
    }
    if ($Source) {
        $params += "source=eq.$Source"
    }
    if ($Category) {
        $params += "category=eq.$Category"
    }
    
    $params += "order=importance.desc,timestamp.desc"
    $params += "limit=$Limit"
    
    $fullUrl = $url + "?" + ($params -join "&")
    
    $headers = @{
        "apikey" = $config.SUPABASE_ANON_KEY
        "Authorization" = "Bearer $($config.SUPABASE_ANON_KEY)"
    }
    
    try {
        $response = Invoke-RestMethod -Uri $fullUrl -Headers $headers -Method Get
        return $response
    }
    catch {
        Write-Host "Search failed: $_" -ForegroundColor Red
        return $null
    }
}

# Export functions
Export-ModuleMember -Function Get-SupabaseConfig, Invoke-SupabaseQuery, Add-SupabaseRecord, Search-SupabaseMemories
