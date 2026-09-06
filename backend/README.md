# Elephant Guard — Backend API Service

High-performance, event-driven REST & WebSocket backend service powered by **FastAPI**, **SQLite/SQLAlchemy**, and **Pydantic**.

## Features

- 🔐 **JWT & Role-Based Authentication**: Secure access for Field Officers, Forest Rangers, and Central Administrators.
- 📡 **Real-Time Telemetry WebSockets**: Bi-directional streaming for live device coordinates, edge AI detection events, and battery telemetry.
- 🐘 **Elephant Intrusion Alert Engine**: Multi-tiered alert pipeline calculating geofence proximity (0.5 km, 2 km, 5 km).
- 🚨 **Instant SOS & SMS Broadcast Dispatch**: Integrates with notification gateways to alert nearby villagers and quick-response teams.
- 📊 **Incident & Solution Management**: Full CRUD and lifecycle tracking for wildlife-human conflict resolutions.

## Quick Start

### Local Setup (Python 3.11+)

1. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: .\venv\Scripts\activate
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Run the development server:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

4. Interactive API Docs:
   - Swagger UI: [http://localhost:8000/docs](http://localhost:8000/docs)
   - ReDoc: [http://localhost:8000/redoc](http://localhost:8000/redoc)

### Docker

```bash
docker build -t elephant-guard-backend .
docker run -p 8000:8000 elephant-guard-backend
```
