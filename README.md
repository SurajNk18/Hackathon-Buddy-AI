# Hackathon Buddy

AI-Powered Hackathon Team Formation & Collaboration Platform.

## System Architecture

The system consists of three main components:
1. **Frontend**: React.js + Vite (`frontend/`) -> Port 5173
2. **Backend**: Spring Boot 3 + MySQL / TiDB (`backend/`) -> Port 8081
3. **AI Service**: Python FastAPI + Sentence Transformers (`ai-service/`) -> Port 8000

---

## Quick Start on Windows (.bat Files)

We provide modular and interactive Windows Batch scripts to easily launch and control your project:

### 1. Interactive Control Panel (Recommended)
Double-click `launcher.bat` in the root folder. It provides an interactive menu:
- **[1]** Start All Services (Frontend + Backend + AI)
- **[2]** Start React Frontend Only (Port 5173)
- **[3]** Start Spring Boot Backend Only (Port 8081)
- **[4]** Start Python AI / ML Service Only (Port 8000)
- **[5]** Check Service Status (Port listener verification)
- **[6]** Stop All Running Services
- **[7]** Open Web App in Browser
- **[8]** Open AI Swagger Documentation

### 2. Start Everything at Once
Double-click:
```cmd
start-all.bat
```
This launches 3 separate terminal windows for Frontend, Backend, and AI Service.

### 3. Run Services Individually
- **Frontend only:** `run-frontend.bat` (Starts React/Vite on http://localhost:5173)
- **Backend only:** `run-backend.bat` (Starts Spring Boot on http://localhost:8081)
- **AI Service only:** `run-ai.bat` (Starts FastAPI on http://localhost:8000)

### 4. Stop All Services
Double-click:
```cmd
stop-all.bat
```
This terminates processes and child processes holding ports 5173, 8081, and 8000.

---

## Service Endpoints & Documentation

| Component | Port | URL |
| :--- | :--- | :--- |
| **Frontend Web App** | 5173 | [http://localhost:5173](http://localhost:5173) |
| **Spring Boot Backend** | 8081 | [http://localhost:8081](http://localhost:8081) |
| **Python AI / ML Service** | 8000 | [http://localhost:8000](http://localhost:8000) |
| **AI Swagger UI** | 8000 | [http://localhost:8000/docs](http://localhost:8000/docs) |

---

## Manual Setup & Prerequisites

### Prerequisites
- Node.js (v18+) & npm
- Java JDK 17
- Python 3.10+
- MySQL or TiDB Cloud (configured in `backend/src/main/resources/application.yml`)

### Manual Commands

#### 1. Python AI Service
```cmd
cd ai-service
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000
```

#### 2. Backend (Spring Boot)
```cmd
cd backend
mvnw.cmd spring-boot:run
```

#### 3. Frontend (React + Vite)
```cmd
cd frontend
npm install
npm run dev
```
