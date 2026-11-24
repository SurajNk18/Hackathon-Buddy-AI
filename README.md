# Hackathon Buddy AI

An AI-powered hackathon platform that helps students discover hackathons, find teammates based on skills, and generate innovative project ideas using AI.

AI-Powered Hackathon Team Formation & Collaboration Platform.

## System Architecture

The system consists of three main components:

1. **Frontend:** React.js + Vite (`frontend/`) — Port 5173
2. **Backend:** Spring Boot 3 + MySQL / TiDB (`backend/`) — Port 8081
3. **AI Service:** Python FastAPI + Sentence Transformers (`ai-service/`) — Port 8000

---

## Quick Start on Windows (.bat Files)

We provide modular Windows Batch scripts to launch and control the project.

### 1. Interactive Control Panel (Recommended)

Double-click `launcher.bat` in the root folder. It provides an interactive menu:

- **[1]** Start All Services
- **[2]** Start React Frontend Only
- **[3]** Start Spring Boot Backend Only
- **[4]** Start Python AI / ML Service Only
- **[5]** Check Service Status
- **[6]** Stop All Running Services
- **[7]** Open Web App in Browser
- **[8]** Open AI Swagger Documentation

### 2. Start Everything at Once

Double-click:

```cmd
start-all.bat