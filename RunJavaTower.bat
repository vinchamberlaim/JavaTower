@echo off
title JavaTower Launcher
echo Starting JavaTower Setup...
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0RunJavaTower.ps1"
if errorlevel 1 pause
