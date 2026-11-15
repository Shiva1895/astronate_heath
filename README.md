# ASTRA HEALTH frontend

This is the existing React/Vite frontend. It integrates with the Spring Boot API under `/api/v1`; it does not provide a mock API or substitute successful responses for backend failures.

## Run locally

1. Install Node dependencies with `npm install`.
2. Copy `.env.example` to `.env.local` and set `VITE_API_BASE_URL` to the backend API root, for example `http://localhost:8080/api/v1`.
3. Start the frontend with `npm run dev`. Vite normally serves it at `http://localhost:5173`.

The backend must allow the frontend origin through `ASTRA_CORS_ALLOWED_ORIGINS` (the backend's default includes `http://localhost:5173`). Configure the backend and database separately; do not put credentials in the Vite environment because `VITE_*` values are exposed to browser code.

## Authentication and access

- Login, registration, refresh, logout, and the current-user lookup use the backend auth and user endpoints.
- Access and refresh tokens are held in memory only. An expired access token triggers one refresh attempt; a failed refresh clears the local session. Reloading the page requires signing in again.
- The frontend limits navigation by role; the backend remains authoritative for role and per-astronaut access.
- Public registration requests an astronaut account and may require administrator activation. The backend has no password-reset endpoint, so the forgot-password screen reports that limitation instead of claiming success.
- Mission Control surfaces mission-level summaries, notifications, devices, and reports only. It must not expose individual health readings or private medical records.
- Well-being audio is fetched on demand from authorized endpoints and held as a temporary browser object URL. It is not persisted by the frontend.

## Implemented API-backed areas

Authentication; role-protected astronaut, medical, and Mission Control workspaces; astronaut readings, alerts, devices, and well-being reports; assigned medical rosters, summaries, alert transitions/history, observations/follow-ups, report review/audio, notifications, and administrator audit history/astronaut management; Mission Control summaries, device inventory, notifications, and mission report exports.

Sleep/activity summaries and some preference controls remain presentation-only because the backend does not expose corresponding endpoints. No automated frontend test script or test suite is currently configured. Use `npm run lint` and `npm run build` for available frontend checks.
