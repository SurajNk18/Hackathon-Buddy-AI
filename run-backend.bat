@echo off
title Hackathon Buddy - Spring Boot Backend (Port 8081)
color 0E
setlocal

cd /d "%~dp0backend"

echo ======================================================================
echo          HACKATHON BUDDY - SPRING BOOT BACKEND API
echo ======================================================================
echo Directory: %CD%
echo.

:: Check Java
java -version >nul 2>&1
if %errorlevel% NEQ 0 (
    echo [ERROR] Java was not found in your system PATH!
    echo Please install JDK 17 or higher and configure JAVA_HOME.
    echo.
    pause
    exit /b 1
)

echo [INFO] Java is available.
echo [INFO] Starting Spring Boot Backend via Maven Wrapper...
echo.
echo ======================================================================
echo  Backend API running at: http://localhost:8081
echo  Health/API status at:  http://localhost:8081/api
echo ======================================================================
echo.

call .\mvnw.cmd spring-boot:run

if %errorlevel% NEQ 0 (
    echo.
    echo [ERROR] Spring Boot backend process exited with code %errorlevel%.
    pause
)
