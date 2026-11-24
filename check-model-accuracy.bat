@echo off
title Hackathon Buddy - Real Database ML Model Accuracy
color 0A
cls
echo ======================================================================
echo     HACKATHON BUDDY - REAL DATABASE ML MODEL ACCURACY EVALUATOR
echo ======================================================================
echo.
echo Connecting to live project database and evaluating ML models...
echo (Calculating accuracy on actual registered users, skills, and projects)
echo.

cd /d "%~dp0ai-service"

python check_database_model_accuracy.py

echo.
echo ======================================================================
echo Evaluation complete! Press any key to exit...
echo ======================================================================
pause >nul
