@echo off
title Hackathon Buddy - AI / ML Service (Port 8000)
color 0A
setlocal

cd /d "%~dp0ai-service"

echo ======================================================================
echo           HACKATHON BUDDY - PYTHON AI / ML SERVICE
echo ======================================================================
echo Directory: %CD%
echo.

:: Check Python
python --version >nul 2>&1
if %errorlevel% NEQ 0 (
    echo [ERROR] Python was not found in your system PATH!
    echo Please install Python 3.10+ and make sure to check "Add Python to PATH".
    echo.
    pause
    exit /b 1
)

:: Activate Virtual Environment if exists
if exist "venv\Scripts\activate.bat" (
    echo [INFO] Activating virtual environment: venv...
    call "venv\Scripts\activate.bat"
) else if exist ".venv\Scripts\activate.bat" (
    echo [INFO] Activating virtual environment: .venv...
    call ".venv\Scripts\activate.bat"
)

echo [INFO] Checking dependencies (FastAPI, Uvicorn)...
python -c "import fastapi, uvicorn" >nul 2>&1
if %errorlevel% NEQ 0 (
    echo [WARNING] FastAPI or Uvicorn is not installed in the active environment.
    echo Installing requirements from requirements.txt...
    pip install -r requirements.txt
    if %errorlevel% NEQ 0 (
        echo [ERROR] Failed to install requirements!
        pause
        exit /b 1
    )
)

echo.
echo ======================================================================
echo  FastAPI AI Service running at: http://localhost:8000
echo  Swagger API Documentation at:  http://localhost:8000/docs
echo ======================================================================
echo.

python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

if %errorlevel% NEQ 0 (
    echo.
    echo [ERROR] AI service process exited with code %errorlevel%.
    pause
)
