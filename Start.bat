@echo off
title JavaTower
cls
echo.
echo   ========================================
echo        JavaTower - Tower Defence RPG
echo   ========================================
echo.
echo   First run will download dependencies
echo   and create the database automatically.
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0RunJavaTower.ps1"
if errorlevel 1 (
    echo.
    echo   Press any key to exit...
    pause >nul
)
