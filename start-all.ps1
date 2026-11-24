# Hackathon Buddy - PowerShell Startup Script
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          HACKATHON BUDDY - FULL STACK STARTUP LAUNCHER" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = $PSScriptRoot

# 1. AI / ML Service (Port 8000)
Write-Host "[1/3] Starting Python AI / ML Service (Port 8000)..." -ForegroundColor Yellow
$aiScript = @"
Set-Location '$rootDir\ai-service'
if (Test-Path 'venv\Scripts\Activate.ps1') { . 'venv\Scripts\Activate.ps1' }
Write-Host 'Starting FastAPI AI Service on http://localhost:8000...' -ForegroundColor Green
python -m uvicorn app.main:app --reload --port 8000
"@
Start-Process powershell -ArgumentList "-NoExit", "-Command", $aiScript

Start-Sleep -Seconds 2

# 2. Spring Boot Backend (Port 8081)
Write-Host "[2/3] Starting Spring Boot Backend (Port 8081)..." -ForegroundColor Yellow
$backendScript = @"
Set-Location '$rootDir\backend'
Write-Host 'Starting Spring Boot Backend on http://localhost:8081...' -ForegroundColor Green
.\mvnw.cmd spring-boot:run
"@
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendScript

Start-Sleep -Seconds 2

# 3. React Frontend (Port 5173)
Write-Host "[3/3] Starting React Frontend (Port 5173)..." -ForegroundColor Yellow
$frontendScript = @"
Set-Location '$rootDir\frontend'
Write-Host 'Starting Vite Dev Server on http://localhost:5173...' -ForegroundColor Green
npm run dev
"@
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendScript

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "                    ALL SERVICES LAUNCHED!" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  * Frontend Web App:     http://localhost:5173" -ForegroundColor White
Write-Host "  * Spring Boot API:      http://localhost:8081" -ForegroundColor White
Write-Host "  * AI / ML Service:      http://localhost:8000" -ForegroundColor White
Write-Host "  * AI Swagger Docs:      http://localhost:8000/docs" -ForegroundColor White
Write-Host ""
Write-Host "Each service is running in its own terminal window." -ForegroundColor Gray
Write-Host "To stop all services, run: .\stop-all.bat" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Cyan
