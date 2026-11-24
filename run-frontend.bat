@echo off
title Hackathon Buddy - React Frontend (Port 5173)
color 09
setlocal

cd /d "%~dp0frontend"

echo ======================================================================
echo           HACKATHON BUDDY - REACT FRONTEND (VITE)
echo ======================================================================
echo Directory: %CD%
echo.

:: Check Node
node -v >nul 2>&1
if %errorlevel% NEQ 0 (
    echo [ERROR] Node.js is not found in your system PATH!
    echo Please install Node.js - LTS version - from https://nodejs.org
    echo.
    pause
    exit /b 1
)

:: Check dependencies
if not exist "node_modules\" (
    echo [INFO] node_modules not detected. Installing dependencies now...
    call npm install
    if %errorlevel% NEQ 0 (
        echo [ERROR] npm install failed!
        pause
        exit /b 1
    )
)

echo.
echo ======================================================================
echo  Vite Dev Server starting at: http://localhost:5173
echo ======================================================================
echo.

call npm run dev

if %errorlevel% NEQ 0 (
    echo.
    echo [ERROR] Frontend process exited with code %errorlevel%.
    pause
)
