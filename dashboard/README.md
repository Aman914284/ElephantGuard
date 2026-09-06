# Elephant Guard — Tactical Command Center Dashboard

State-of-the-art interactive web command dashboard built with **React 18**, **TypeScript**, **Vite**, **Tailwind CSS**, and **Leaflet Maps**.

## Key Features

- 🗺️ **Live Radar & Conflict Map**: Real-time GPS plotting of detected elephant herds, patrol units, and village geofences.
- 🎯 **Tactical Dark Mode UI**: Military-grade, high-contrast user interface engineered for 24/7 night vision field operations.
- 🔊 **Acoustic Deterrence Controls**: Remote triggering of bio-acoustic frequencies and sirens to harmlessly steer elephants away.
- 🚨 **Incident Dispatch & SOS Feed**: Priority triage matrix with instant live resolution modals and officer dispatch controls.
- 🛡️ **Role-Based Workflows**: Dedicated views for Field Officers, Forest Commandants, and Admin Supervisors.

## Quick Start

### Local Development

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start the development server:
   ```bash
   npm run dev
   ```

3. Build for production:
   ```bash
   npm run build
   ```

### Docker

```bash
docker build -t elephant-guard-dashboard .
docker run -p 3000:80 elephant-guard-dashboard
```
