@echo off
title Hackathon Buddy - Stop All Services
color 0C
setlocal

echo ======================================================================
echo           HACKATHON BUDDY - STOPPING ALL RUNNING SERVICES
echo ======================================================================
echo.

echo [1/3] Stopping React Frontend on Port 5173...
set "FOUND1=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":5173" ^| findstr "LISTENING"') do (
    set "FOUND1=1"
    echo  - Terminating Frontend (PID %%a)...
    taskkill /F /T /PID %%a >nul 2>&1
)
if "%FOUND1%"=="0" echo  - No active service found on port 5173.

echo.
echo [2/3] Stopping Spring Boot Backend on Port 8081...
set "FOUND2=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8081" ^| findstr "LISTENING"') do (
    set "FOUND2=1"
    echo  - Terminating Spring Boot Backend (PID %%a)...
    taskkill /F /T /PID %%a >nul 2>&1
)
if "%FOUND2%"=="0" echo  - No active service found on port 8081.

echo.
echo [3/3] Stopping Python AI / ML Service on Port 8000...
set "FOUND3=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8000" ^| findstr "LISTENING"') do (
    set "FOUND3=1"
    echo  - Terminating Python AI Service (PID %%a)...
    taskkill /F /T /PID %%a >nul 2>&1
)
if "%FOUND3%"=="0" echo  - No active service found on port 8000.

echo.
echo ======================================================================
echo              ALL HACKATHON BUDDY SERVICES STOPPED!
echo ======================================================================
echo.
pause
