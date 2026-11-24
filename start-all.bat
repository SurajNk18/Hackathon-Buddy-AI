@echo off
title Hackathon Buddy - Orchestrator
color 0B
setlocal

echo ======================================================================
echo           HACKATHON BUDDY - FULL STACK STARTUP LAUNCHER
echo ======================================================================
echo.

set "ROOT_DIR=%~dp0"

echo [1/3] Starting Python AI / ML Service (Port 8000)...
start "Hackathon Buddy - AI/ML Service (Port 8000)" cmd /k call "%ROOT_DIR%run-ai.bat"

:: Safe delay without stdin redirection errors
ping 127.0.0.1 -n 3 >nul

echo [2/3] Starting Spring Boot Backend (Port 8081)...
start "Hackathon Buddy - Spring Boot Backend (Port 8081)" cmd /k call "%ROOT_DIR%run-backend.bat"

:: Safe delay
ping 127.0.0.1 -n 4 >nul

echo [3/3] Starting React Frontend (Port 5173)...
start "Hackathon Buddy - React Frontend (Port 5173)" cmd /k call "%ROOT_DIR%run-frontend.bat"

echo.
echo ======================================================================
echo                    ALL SERVICES LAUNCHED!
echo ======================================================================
echo.
echo   * Frontend Web App:     http://localhost:5173
echo   * Spring Boot API:      http://localhost:8081
echo   * AI / ML Service:      http://localhost:8000
echo   * AI Swagger Docs:      http://localhost:8000/docs
echo.
echo Each service is running in its own terminal window.
echo To stop all services at once, run: stop-all.bat
echo.
echo You can also run services individually:
echo   - run-frontend.bat
echo   - run-backend.bat
echo   - run-ai.bat
echo ======================================================================
echo.
pause
