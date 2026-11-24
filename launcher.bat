@echo off
title Hackathon Buddy - Control Panel
color 0B
setlocal EnableDelayedExpansion

:menu
cls
echo ======================================================================
echo                  HACKATHON BUDDY - CONTROL PANEL
echo ======================================================================
echo.
echo   [1] Start All Services (Frontend + Backend + AI Service)
echo   [2] Start React Frontend Only (Port 5173)
echo   [3] Start Spring Boot Backend Only (Port 8081)
echo   [4] Start Python AI / ML Service Only (Port 8000)
echo.
echo   [5] Check Running Status (Scan Ports 5173, 8081, 8000)
echo   [6] Stop All Running Services
echo.
echo   [7] Open Frontend in Browser (http://localhost:5173)
echo   [8] Open AI Swagger Documentation (http://localhost:8000/docs)
echo.
echo   [0] Exit
echo ======================================================================
set /p "CHOICE=Enter choice [0-8]: "

if "%CHOICE%"=="1" goto start_all
if "%CHOICE%"=="2" goto start_frontend
if "%CHOICE%"=="3" goto start_backend
if "%CHOICE%"=="4" goto start_ai
if "%CHOICE%"=="5" goto check_status
if "%CHOICE%"=="6" goto stop_all
if "%CHOICE%"=="7" goto open_frontend
if "%CHOICE%"=="8" goto open_swagger
if "%CHOICE%"=="0" exit /b 0

echo.
echo [!] Invalid option. Please enter a number between 0 and 8.
ping 127.0.0.1 -n 2 >nul
goto menu

:start_all
echo.
echo Launching all services...
call "%~dp0start-all.bat"
goto menu

:start_frontend
echo.
echo Launching React Frontend...
start "Hackathon Buddy - React Frontend (Port 5173)" cmd /k call "%~dp0run-frontend.bat"
ping 127.0.0.1 -n 2 >nul
goto menu

:start_backend
echo.
echo Launching Spring Boot Backend...
start "Hackathon Buddy - Spring Boot Backend (Port 8081)" cmd /k call "%~dp0run-backend.bat"
ping 127.0.0.1 -n 2 >nul
goto menu

:start_ai
echo.
echo Launching Python AI / ML Service...
start "Hackathon Buddy - AI/ML Service (Port 8000)" cmd /k call "%~dp0run-ai.bat"
ping 127.0.0.1 -n 2 >nul
goto menu

:check_status
cls
echo ======================================================================
echo                SERVICE PORT LISTENING STATUS
echo ======================================================================
echo.
echo  Port 5173 (React Frontend):
netstat -aon | findstr ":5173" | findstr "LISTENING"
if errorlevel 1 echo    [NOT RUNNING]
echo.
echo  Port 8081 (Spring Boot Backend):
netstat -aon | findstr ":8081" | findstr "LISTENING"
if errorlevel 1 echo    [NOT RUNNING]
echo.
echo  Port 8000 (Python AI Service):
netstat -aon | findstr ":8000" | findstr "LISTENING"
if errorlevel 1 echo    [NOT RUNNING]
echo.
echo ======================================================================
echo.
pause
goto menu

:stop_all
echo.
echo Stopping all services...
call "%~dp0stop-all.bat"
goto menu

:open_frontend
start http://localhost:5173
goto menu

:open_swagger
start http://localhost:8000/docs
goto menu
