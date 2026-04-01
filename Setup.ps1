# JavaTower Setup Script
# Downloads and installs dependencies for first run

param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    JavaTower Setup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$JavaFXVersion = "21.0.2"
$JavaFXUrl = "https://download2.gluonhq.com/openjfx/$JavaFXVersion/openjfx-$JavaFXVersion_windows-x64_bin-sdk.zip"
$SQLiteJdbcUrl = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"

$ProjectRoot = $PSScriptRoot
$JavaFXDir = Join-Path $ProjectRoot "javafx-sdk"
$LibDir = Join-Path $ProjectRoot "lib"
$TempDir = Join-Path $ProjectRoot "temp_setup"

# Check Java installation
Write-Host "[1/5] Checking Java installation..." -ForegroundColor Yellow
$JavaVersion = $null
try {
    $JavaVersion = java -version 2>&1 | Select-String -Pattern '"([0-9]+)"' | ForEach-Object { $_.Matches.Groups[1].Value }
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install Java 21 or later from: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

if ([int]$JavaVersion -ge 21) {
    Write-Host "      ✓ Java $JavaVersion detected" -ForegroundColor Green
} else {
    Write-Host "WARNING: Java $JavaVersion found, but Java 21+ is recommended" -ForegroundColor Yellow
}

# Create directories
Write-Host "[2/5] Creating directories..." -ForegroundColor Yellow
if (-not (Test-Path $LibDir)) {
    New-Item -ItemType Directory -Path $LibDir -Force | Out-Null
}
if (-not (Test-Path $TempDir)) {
    New-Item -ItemType Directory -Path $TempDir -Force | Out-Null
}
Write-Host "      ✓ Directories created" -ForegroundColor Green

# Download JavaFX SDK
$JavaFXOk = Test-Path (Join-Path $JavaFXDir "lib\javafx.controls.jar")
if ($Force -or -not $JavaFXOk) {
    Write-Host "[3/5] Downloading JavaFX SDK $JavaFXVersion..." -ForegroundColor Yellow
    Write-Host "      This may take a few minutes..." -ForegroundColor Gray
    
    $ZipFile = Join-Path $TempDir "javafx-sdk.zip"
    
    try {
        Invoke-WebRequest -Uri $JavaFXUrl -OutFile $ZipFile -UseBasicParsing
        Write-Host "      ✓ Download complete" -ForegroundColor Green
        
        Write-Host "      Extracting JavaFX SDK..." -ForegroundColor Yellow
        if (Test-Path $JavaFXDir) {
            Remove-Item $JavaFXDir -Recurse -Force
        }
        Expand-Archive -Path $ZipFile -DestinationPath $ProjectRoot -Force
        Write-Host "      ✓ JavaFX SDK extracted" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Failed to download JavaFX SDK" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[3/5] JavaFX SDK already installed (use -Force to reinstall)" -ForegroundColor Green
}

# Download SQLite JDBC
$SQLiteJar = Join-Path $LibDir "sqlite-jdbc-3.45.1.0.jar"
if ($Force -or -not (Test-Path $SQLiteJar)) {
    Write-Host "[4/5] Downloading SQLite JDBC driver..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $SQLiteJdbcUrl -OutFile $SQLiteJar -UseBasicParsing
        Write-Host "      ✓ SQLite JDBC downloaded" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Failed to download SQLite JDBC" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[4/5] SQLite JDBC already installed (use -Force to reinstall)" -ForegroundColor Green
}

# Cleanup
Write-Host "[5/5] Cleaning up..." -ForegroundColor Yellow
if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
Write-Host "      ✓ Cleanup complete" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To run the game, use one of these methods:" -ForegroundColor White
Write-Host "  1. Run: .\RunJavaTower.ps1" -ForegroundColor Yellow
Write-Host "  2. Run: .\RunJavaTower.bat" -ForegroundColor Yellow
Write-Host ""
Write-Host "First run will create the database automatically." -ForegroundColor Gray
Write-Host ""
