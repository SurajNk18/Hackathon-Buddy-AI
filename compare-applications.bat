@echo off
title Hackathon Buddy - Cross-Application ML Benchmark Comparison
color 0B
cls
echo ======================================================================
echo    HACKATHON BUDDY - BENCHMARK COMPARISON ACROSS APPLICATIONS
echo ======================================================================
echo.
echo Comparing Hackathon Buddy AI Engine against:
echo   1. Devpost / Devfolio Style Tag Filter (Exact String Search)
echo   2. Upwork / Freelance Style Jaccard Index Matcher
echo   3. Standard Job Portal Unweighted TF-IDF Matcher
echo   4. Hackathon Buddy AI Hybrid Recommendation Engine
echo.
echo Evaluated across 4 distinct industry application types:
echo   - AI / GenAI Health Diagnostic Platform
echo   - FinTech / Web3 Micro-Lending Portal
echo   - Smart City / IoT Telemetry Hub
echo   - EdTech Cross-Platform Collaboration App
echo.

cd /d "%~dp0ai-service"

python benchmark_compare_applications.py

echo.
echo ======================================================================
echo Benchmark complete! Press any key to exit...
echo ======================================================================
pause >nul
