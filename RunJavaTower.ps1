<#
.SYNOPSIS
    JavaTower Launcher - Checks, installs deps, compiles, and runs the game.
#>

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

function Write-Step  { param($msg) Write-Host $msg -ForegroundColor Yellow }
function Write-Ok    { param($msg) Write-Host ("  [OK] " + $msg) -ForegroundColor Green }
function Write-Err   { param($msg) Write-Host ("  [FAIL] " + $msg) -ForegroundColor Red }
function Write-Dl    { param($msg) Write-Host ("  >> " + $msg) -ForegroundColor DarkYellow }

Write-Host ""
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host "       JavaTower - Setup and Launch"        -ForegroundColor Cyan
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host ""

# ---------- STEP 1: Java ----------
Write-Step "[1/5] Checking Java..."
$javaOutput = cmd /c "java -version 2>&1"
$javaLine = ($javaOutput | Select-Object -First 1) -as [string]
if ($javaLine -match '"(\d+)') {
    $jv = [int]$Matches[1]
    if ($jv -lt 17) {
        Write-Err "Java 17+ required (found $jv)"
        Read-Host "Enter to exit"; exit 1
    }
    Write-Ok "Java $jv detected"
} else {
    Write-Err "Java not found. Install Java 17+ and add to PATH."
    Read-Host "Enter to exit"; exit 1
}

# ---------- STEP 2: JavaFX SDK ----------
Write-Step "[2/5] Checking JavaFX SDK..."
$javafxDir = Join-Path $ProjectDir "javafx-sdk"
$javafxLib = Join-Path $javafxDir "lib"

if (Test-Path (Join-Path $javafxLib "javafx.controls.jar")) {
    $jarCount = (Get-ChildItem $javafxLib -Filter "*.jar").Count
    Write-Ok "JavaFX SDK found ($jarCount jars)"
} else {
    Write-Dl "JavaFX SDK missing. Downloading OpenJFX 21.0.2..."
    $javafxZip = Join-Path $ProjectDir "javafx-sdk.zip"
    $javafxUrl = "https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $wc = New-Object System.Net.WebClient
        $wc.DownloadFile($javafxUrl, $javafxZip)
        Write-Host "  Extracting..." -ForegroundColor Gray
        Expand-Archive -Path $javafxZip -DestinationPath $ProjectDir -Force
        $ext = Get-ChildItem $ProjectDir -Directory -Filter "javafx-sdk-*" | Select-Object -First 1
        if ($ext -and $ext.Name -ne "javafx-sdk") {
            if (Test-Path $javafxDir) { Remove-Item $javafxDir -Recurse -Force }
            Rename-Item $ext.FullName "javafx-sdk"
        }
        Remove-Item $javafxZip -Force -ErrorAction SilentlyContinue
        Write-Ok "JavaFX SDK installed"
    } catch {
        Write-Err "Download failed. Get it from https://openjfx.io"
        Read-Host "Enter to exit"; exit 1
    }
}

# ---------- STEP 3: SQLite JDBC ----------
Write-Step "[3/5] Checking SQLite JDBC..."
$libDir = Join-Path $ProjectDir "lib"
if (-not (Test-Path $libDir)) { New-Item -ItemType Directory -Path $libDir | Out-Null }

$sqliteJar = Get-ChildItem $libDir -Filter "sqlite-jdbc-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($sqliteJar) {
    Write-Ok ("SQLite JDBC found (" + $sqliteJar.Name + ")")
} else {
    Write-Dl "SQLite JDBC missing. Downloading 3.45.1.0..."
    $sqliteTarget = Join-Path $libDir "sqlite-jdbc-3.45.1.0.jar"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $wc = New-Object System.Net.WebClient
        $wc.DownloadFile("https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar", $sqliteTarget)
        $sqliteJar = Get-Item $sqliteTarget
        Write-Ok "SQLite JDBC installed"
    } catch {
        Write-Err "Download failed. Place sqlite-jdbc jar in lib/ manually."
        Read-Host "Enter to exit"; exit 1
    }
}

# ---------- STEP 4: Compile all sources in one pass ----------
Write-Step "[4/5] Compiling JavaTower..."
$outDir = Join-Path $ProjectDir "out2"
if (Test-Path $outDir) { Remove-Item $outDir -Recurse -Force }
New-Item -ItemType Directory -Path $outDir | Out-Null

$sqlitePath = $sqliteJar.FullName

# Collect all java files and write relative paths to sources.txt
$allJava = @()
$allJava += (Get-ChildItem -Recurse -Filter "*.java" -Path javatower).FullName
$allJava += (Resolve-Path "Main.java").Path
$relFiles = $allJava | ForEach-Object { $_.Replace(($ProjectDir + "\"), "") }
$relFiles | Set-Content -Path "sources.txt" -Encoding ASCII

Write-Host ("  Found " + $allJava.Count + " source files") -ForegroundColor DarkGray

$compileCmd = 'javac --module-path "' + $javafxLib + '" --add-modules javafx.controls,javafx.graphics -cp "' + $sqlitePath + '" -d "' + $outDir + '" @sources.txt'
$errOutput = cmd /c "$compileCmd 2>&1"
$exitCode = $LASTEXITCODE

if ($exitCode -eq 0) {
    $classCount = (Get-ChildItem $outDir -Recurse -Filter "*.class").Count
    Write-Ok ("Compiled successfully (" + $classCount + " class files)")
} else {
    Write-Err "Compilation failed:"
    foreach ($line in $errOutput) {
        if ($line) { Write-Host ("    " + $line) -ForegroundColor Red }
    }
    Read-Host "Enter to exit"; exit 1
}

# ---------- STEP 5: Launch ----------
Write-Step "[5/5] Launching JavaTower..."
Write-Host ""
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host "       Starting game... Have fun!"          -ForegroundColor Cyan
Write-Host "       (Database will auto-create on first run)" -ForegroundColor DarkGray
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host ""

$runCp      = '"' + $outDir + ";" + $sqlitePath + '"'
$runModPath = '"' + $javafxLib + '"'
$runCmd     = "java --module-path " + $runModPath + " --add-modules javafx.controls,javafx.graphics -cp " + $runCp + " Main"
cmd /c "$runCmd 2>&1"

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Err ("Game exited with code " + $LASTEXITCODE)
    Read-Host "Enter to exit"
}
