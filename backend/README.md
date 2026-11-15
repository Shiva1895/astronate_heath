# ASTRA HEALTH Backend

## Project overview

ASTRA HEALTH is a backend for astronaut health monitoring and early-warning workflows. It provides authorized access to health readings, alerts, medical follow-up, well-being reports, and mission/device operations. It is not a diagnostic system and does not provide medically validated thresholds.

### Problem and objectives

Long-duration missions need a secure way to collect health observations, make operational signals visible to authorized teams, and preserve a review trail without exposing private medical information to unrelated roles. The backend aims to:

- Protect health and identity data with authentication, role checks, ownership checks, and medical assignments.
- Store health readings and operational records with UTC timestamps, pagination, database constraints, and history.
- Support explainable, configurable software anomaly rules and alert workflows without presenting them as diagnoses.
- Keep astronaut well-being reports and audio private, and make report exports and audit records permission-checked.
- Provide stable `/api/v1` REST endpoints for a future web client.

### Technology stack

| Area | Technology |
| --- | --- |
| Runtime | Java 27, Spring Boot 4.1.2 |
| API | Spring MVC, Jakarta Validation, OpenAPI/Swagger UI |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Authentication | Spring Security, JWT bearer access tokens, BCrypt |
| Reports and audio | Apache PDFBox, private filesystem audio storage |
| Build and tests | Maven, JUnit 5, Mockito, MockMvc, H2 test profile |
| Local container demo | Docker, Docker Compose, PostgreSQL |

### Architecture

The application follows a layered structure: REST controllers validate/map HTTP requests, services implement transactions and authorization-sensitive workflows, repositories perform parameterized persistence queries, and JPA entities map relational records. Spring Security authenticates JWTs and loads current account roles from the database. Service-level checks enforce astronaut ownership and medical-officer assignments. API errors use the shared `ApiErrorResponse` shape.

There is no React/Vite frontend in this workspace. The backend exposes APIs and OpenAPI documentation but does not include UI components.

## Current scope

The backend provides Astronaut CRUD, JWT authentication, role-based access control, user/profile management, medical officer assignments, mission management, device registration/assignment/history, health readings and summaries, configurable software anomaly rules, alert workflows, private medical observations, well-being reporting, private audio storage, in-app notifications, role-checked report exports, audit history, and operational dashboard APIs. Authentication accounts are stored separately from astronaut profiles; public registration creates an `ASTRONAUT` account in `PENDING_ACTIVATION` state. An administrator must activate an account before it can log in. Public registration cannot assign privileged roles.

The administrator-only account activation endpoints are:

- `POST /api/v1/auth/admin/accounts/{id}/activate`
- `POST /api/v1/auth/admin/accounts/{id}/deactivate`

The rest of the API, except `GET /api/v1/health` and authentication entry points, requires a valid bearer token. Current account status and roles are loaded from the database for each bearer-authenticated request, so deactivation and role changes take effect immediately.

## API inventory

The API is versioned under `/api/v1`. OpenAPI JSON is served at `/v3/api-docs` and Swagger UI at `/swagger-ui/index.html` when `ASTRA_OPENAPI_ENABLED=true`. OpenAPI documents request/response DTO schemas and bearer authentication; role and ownership rules are summarized below and enforced by the backend.

Errors use a consistent JSON object with `status`, `error`, `message`, `path`, and `validationErrors`. Validation failures use HTTP 400 and populate `validationErrors` by field; authentication failures use 401, authorization failures use 403, missing resources use 404, and uniqueness conflicts use 409. Stack traces and framework exception details are not returned.

| API group | Main routes |
| --- | --- |
| Health and authentication | `GET /health`; `POST /auth/register`, `/login`, `/refresh`, `/logout`; administrator account activation/deactivation |
| Astronauts | `/astronauts` collection and `/{id}` CRUD |
| Users | `/users/me`, `/users`, `/{id}`, profile, roles, astronaut-profile link, activate/deactivate |
| Medical assignments | `/medical-assignments`, `/me/astronauts`, `/{id}` |
| Missions | `/missions`, `/{id}`, `/{missionId}/astronauts/{astronautId}` |
| Devices | `/devices`, `/{id}`, assignment, status, history, and `/astronauts/{astronautId}` |
| Health | `/health/readings`, `/readings/bulk`, astronaut reading history/latest, daily/weekly summaries, period comparison |
| Anomaly rules and alerts | `/anomaly-rules`; `/alerts`, astronaut alert listing, workflow transitions, and history |
| Medical observations | `/medical-observations`, astronaut listing, history, follow-up actions and inbox |
| Dashboards | `/dashboard/astronauts/**`, `/dashboard/medical-officer/astronauts`, `/dashboard/missions` |
| Well-being and notifications | `/wellbeing/reports`, audio drafts, inbox, review and playback; `/notifications` and read-state routes |
| Reports and audit | `/reports/**` CSV/PDF exports; administrator-only `/audit-events` |

Detailed REST Client request examples, including role-specific tokens and upload examples, are maintained in [astronaut-api.http](astronaut-api.http). They are examples for manual use and are not evidence of live API verification.

## Database structure

The logical table inventory is `astronauts`, `user_accounts`, `user_account_roles`, `refresh_tokens`, `missions`, `mission_astronauts`, `devices`, `device_history`, `medical_assignments`, `health_readings`, `anomaly_rules`, `health_alerts`, `health_alert_history`, `medical_observations`, `medical_observation_history`, `medical_follow_up_actions`, `wellbeing_reports`, `wellbeing_audio_recordings`, `in_app_notifications`, and `audit_events`. JPA mappings define foreign keys, unique constraints, indexes, and non-cascading relationships for retained health records.

Tests use a separate in-memory H2 database. The default local profile currently uses Hibernate `ddl-auto=update`; this is not a production migration strategy. **Versioned Flyway migrations and a PostgreSQL migration rehearsal have not been completed. Do not treat the Compose setup as production-ready.**

### Role access currently implemented

| Role | Current access |
| --- | --- |
| `ASTRONAUT` | Read own linked profile, health readings/alerts, devices, and well-being reports; submit readings and well-being reports; update own user profile. |
| `MEDICAL_OFFICER` | Read assigned astronaut profiles, health readings/summaries, alerts, well-being reports, and observations; manage alerts, reviews, and follow-ups for assigned astronauts. |
| `MISSION_CONTROL` | Read mission/device operational data and mission-level operational summaries; cannot access raw readings or private medical observations. |
| `ADMIN` | Manage astronauts, users, roles, account status, astronaut links, medical assignments, missions, devices, anomaly rules, and health data. |

Authorization is enforced by Spring Security and service-level astronaut ownership/assignment checks; frontend role checks are not trusted. Astronaut collections and mutation APIs are administrator-only. User responses do not include passwords or password hashes. Mission deletion is a soft archive, medical assignments are explicitly managed, and device assignment/status changes are recorded as history. Health data, well-being report text, and private observations are not exposed through mission-control dashboard responses. Mission Control receives only mission-level counts/status and latest-reading timestamps.

## Health monitoring APIs

The REST Client examples for these routes are in `astronaut-api.http`. Replace example astronaut, device, alert, observation, mission, and rule IDs with records in your local database. Use an active administrator token and a medical-officer token assigned to the target astronaut. The request examples are prepared for manual execution; they have not been run as live API requests.

- `POST /api/v1/health/readings` and `POST /api/v1/health/readings/bulk` ingest validated readings. `GET /api/v1/health/readings/{id}`, `GET /api/v1/health/astronauts/{astronautId}/readings`, and `/readings/latest` retrieve authorized records. History supports reading-type, UTC date-range, and pagination filters. Import/device source event IDs provide duplicate-event protection.
- `GET /api/v1/health/astronauts/{astronautId}/summaries/daily`, `/weekly`, and `/compare` provide database-aggregated min/max/average/latest/count and period comparison results without loading entire histories into application memory.
- `GET/POST/PUT /api/v1/anomaly-rules` manages administrator-configured range and recent-baseline change rules. There are deliberately **no enabled or seeded rules by default**. Administrators must define and review their own operational policy before enabling any rule. These rules are software workflow signals only: they are not medically validated, do not diagnose illness, and are not clinical recommendations.
- `/api/v1/alerts` supports alert retrieval and creation, acknowledge/escalate/resolve/close transitions, and paginated history. Medical officers may manage alerts only for assigned astronauts; astronauts may access their own permitted alert data.
- `/api/v1/medical-observations` supports private observations, scoped retrieval, version history, and follow-up actions. Only administrators and medical officers assigned to the astronaut can access these records. Observation text is never included in dashboard summaries.
- `/api/v1/dashboard` provides latest readings, active alerts, health summaries/trends/comparisons, medical-officer assigned astronaut summaries, and mission operational summaries. Raw health data remains limited to administrators, the linked astronaut, and assigned medical officers. Mission Control is limited to operational mission summaries and cannot use its role to retrieve readings or private observations.

Health readings, alerts, observation snapshots, follow-up history, well-being reports, and audio metadata use relational constraints and are not configured for cascade deletion. Deleting a referenced astronaut or record may therefore be rejected by the database rather than deleting retained health information. `DEVICE` is a data-source label, not evidence of authenticated device connectivity; hardware integration and device identity verification remain future work.

## Well-being reporting, audio, and notifications

- `POST /api/v1/wellbeing/reports` submits a written report and/or a previously uploaded audio draft. Categories are `TIRED`, `STRESSED`, `DIZZY`, `HEADACHE`, `PAIN`, `BREATHING_DIFFICULTY`, and `OTHER`. Astronauts can edit only while the report remains `SUBMITTED`; their history and status are available under `/api/v1/wellbeing/reports/mine`.
- `POST /api/v1/wellbeing/reports/audio-drafts` accepts multipart audio uploads, with a default 10 MiB limit, MIME allow-list, file-signature checks, generated storage names, and SHA-256 metadata. Audio bytes are stored outside the classpath/web root at `${user.home}/.astra-health/audio` by default; only metadata is stored in PostgreSQL. Owners can preview or delete unsubmitted drafts. Playback of an attached recording is authorized for its owner, an assigned medical officer, or an administrator and is audited.
- Configure `ASTRA_AUDIO_STORAGE_DIR` to use a private durable directory. Configure `ASTRA_AUDIO_MAX_FILE_SIZE` (Spring multipart size, e.g. `10MB`), `ASTRA_AUDIO_MAX_REQUEST_SIZE` (e.g. `11MB`), and `ASTRA_AUDIO_MAX_FILE_SIZE_BYTES` (service byte limit, default `10485760`) together if changing upload limits. The application does not encrypt audio files itself; production storage must use appropriately restricted, encrypted storage and tested backup/retention policies.
- Unsubmitted audio drafts have no automatic expiration or cleanup job. Astronauts can delete their own unsubmitted drafts; operators should define and monitor a retention/cleanup policy for abandoned files and database metadata.
- Medical officers use `GET /api/v1/wellbeing/reports/inbox` for assigned astronauts and `PUT /api/v1/wellbeing/reports/{id}/review` to change review status, add a private medical observation, or assign a follow-up. Follow-up and report-status notifications contain generic text only and are scoped to each recipient.
- `TranscriptionProvider` is an extension interface. No provider is configured, so transcription is reported as `UNAVAILABLE`; no transcript is generated, and the original audio remains available to permitted users. Any future transcription is assistive only, not a diagnosis.
- `GET /api/v1/notifications`, `/unread-count`, `PUT /{id}/read`, and `PUT /read-all` provide per-account in-app notification history and unread state. Notifications are recipient-scoped; no WebSocket service is configured, so clients can poll these APIs.

## Dashboards, exports, and audit

The existing `/api/v1/dashboard` endpoints provide health summaries, latest readings, active alerts, assigned-astronaut summaries, and mission operational summaries. Astronauts can also retrieve their own device list. Medical officers have a well-being report inbox and a paginated pending follow-up inbox at `/api/v1/medical-observations/follow-ups/inbox`. Mission Control is restricted to operational summaries and device communication/status data; it cannot retrieve private reports, raw readings, or medical observations. This workspace contains the backend only—there is no React/Vite frontend or `package.json`, so MediaRecorder controls, upload progress UI, and visual dashboards are not implemented here.

`/api/v1/reports` provides permission-checked CSV and PDF exports for astronaut health readings, mission operational summaries, alerts, assigned medical follow-ups, and astronaut well-being reports. Health exports require a valid UTC time range; exports are capped at 5,000 rows. CSV fields are quoted and formula-prefixed cells are neutralized. Exports are returned with `no-store` cache headers. Mission Control can export mission operational summaries only.

Successful write requests are recorded in `audit_events`; successful login/logout are recorded by the authentication service, and authorized audio playback is recorded without storing audio contents. Events include actor (when authenticated), action, resource identifiers, timestamp, and limited request-method metadata. Passwords, bearer/refresh tokens, report text, observation text, and audio bytes are never included in audit metadata. `GET /api/v1/audit-events` is administrator-only. Audit retention and tamper-resistant archival still require an operational policy.

The REST Client examples for users, medical assignments, missions, and devices are in `astronaut-api.http`. Replace example IDs with existing local records and supply tokens for the indicated roles. To obtain a mission-control or medical-officer token, an administrator must assign the role to an active user, then that user must log in. The examples are prepared for manual execution and have not been run as live API requests.

## Local configuration

Configure these environment variables before starting the application:

- `ASTRA_DATABASE_PASSWORD`: Password for the local PostgreSQL role. The default local database URL and username are `jdbc:postgresql://localhost:5432/astra_health_db` and `postgres`; override them with `ASTRA_DATABASE_URL` and `ASTRA_DATABASE_USERNAME` if needed. The password is required and is never stored in the repository.
- `ASTRA_JWT_SECRET`: Base64-encoded random key containing at least 32 bytes. The application refuses to start without a valid key.
- `ASTRA_BOOTSTRAP_ADMIN_EMAIL` and `ASTRA_BOOTSTRAP_ADMIN_PASSWORD`: Set both to provision the initial active administrator. The password must have at least 12 characters and be no more than 72 UTF-8 bytes. The initializer never promotes an existing non-administrator account or resets an existing administrator's password.
- `ASTRA_CORS_ALLOWED_ORIGINS`: Comma-separated exact browser origins. Defaults to `http://localhost:5173`; do not use `*` for an authenticated deployment.
- `ASTRA_OPENAPI_ENABLED`: Enables Swagger/OpenAPI endpoints; defaults to `true` for local development. Set to `false` when public API documentation is not wanted.

PowerShell example (replace placeholders locally; do not commit secrets):

```powershell
$env:ASTRA_DATABASE_URL = "jdbc:postgresql://localhost:5432/astra_health_db"
$env:ASTRA_DATABASE_USERNAME = "postgres"
$databasePassword = Read-Host "PostgreSQL password" -AsSecureString
$env:ASTRA_DATABASE_PASSWORD = [System.Net.NetworkCredential]::new("", $databasePassword).Password
$env:ASTRA_JWT_SECRET = "<base64-encoded random key of at least 32 bytes>"
$env:ASTRA_BOOTSTRAP_ADMIN_EMAIL = "<administrator email>"
$env:ASTRA_BOOTSTRAP_ADMIN_PASSWORD = "<unique strong password>"
$env:ASTRA_CORS_ALLOWED_ORIGINS = "http://localhost:5173"
mvn spring-boot:run
```

### Docker Compose local demonstration

Docker Compose starts a local PostgreSQL service and the backend. It is a local/demo configuration only; it uses Hibernate `ddl-auto=update` and is not a production migration path.

```powershell
Copy-Item .env.example .env
# Edit .env locally with unique database/admin passwords and a valid Base64 JWT key.
docker compose up --build
```

The backend listens on `http://localhost:8080`; health is checked at `/actuator/health` and OpenAPI UI at `/swagger-ui/index.html`. Compose stores PostgreSQL data and private audio in named volumes. `docker compose down` stops containers but preserves those volumes; do not use `down --volumes` if you need to keep local data. The `.env` file is ignored by Git. Never use these local secrets in a shared or production environment.

No Flyway migrations, production profile, frontend Dockerfile, or production deployment configuration is provided yet. Before deployment, create and review versioned PostgreSQL migrations, rehearse them on a copy of the target schema/data, switch schema handling to validation, configure TLS/reverse proxy and secret management, and set encrypted durable audio storage and retention controls. No production deployment has been performed.

## Frontend

The frontend folder was not present in this workspace or its Documents parent when inspected; no `package.json` or Vite configuration was found. Consequently no frontend API client, protected routes, role navigation, dashboards, recording UI, responsive browser testing, frontend build, or frontend tests can be claimed. Provide the existing frontend's absolute path to continue integration. Do not treat backend role checks as a substitute for UI navigation; server-side authorization remains authoritative.

Access tokens are signed using HS256 and expire after 15 minutes by default. Refresh tokens are random, rotated on use, and stored only as SHA-256 hashes. Reuse of a rotated refresh token revokes its token family. Logout revokes the refresh-token family; an already issued access token remains valid until its short expiration. Account registration, login, refresh, health, and admin account activation are demonstrated in `astronaut-api.http`; replace its token/email/password placeholders with values from your local run.

The REST Client file captures access and refresh tokens from the named login response. For the astronaut flow, register an account, log in as the bootstrapped administrator, activate the returned account ID, then run the astronaut login before using protected requests. The expired-token request should be run after the access token's 15-minute lifetime. The refresh-after-logout request should return 401.

The datasource password was previously stored in the application configuration. Treat it as compromised: rotate the PostgreSQL role password using `\password <role>` in an interactive `psql` session, then set `ASTRA_DATABASE_PASSWORD` to the new value in your local environment. This preserves existing database records. Avoid putting the password in SQL command text, shell history, or source control. SQL statement logging is disabled in the default configuration.

Tests use a separate in-memory H2 database under the `test` profile; test schema creation is isolated and does not modify PostgreSQL records. The application still uses Hibernate `ddl-auto=update` for local PostgreSQL; production schema changes should move to controlled Flyway migrations. Review generated schema changes and introduce versioned migrations before deploying to production. No cascading deletion of health history or other retained records is configured.

## Verification

Run the automated suite with:

```powershell
mvn test
```

The application test profile uses an isolated H2 database with `create-drop`; it does not connect to or reset the development PostgreSQL database. The full suite and the unit/security tests are automated; no browser workflows, live PostgreSQL connection, or live authentication/API requests have been verified as part of this workspace.

## Demonstration workflow

1. Start PostgreSQL and the backend using the local setup above; confirm `/actuator/health`.
2. Register an astronaut account and use the bootstrap administrator account to activate it.
3. Log in and use the returned bearer tokens in `astronaut-api.http`; replace example IDs with records from the local database.
4. As administrator, create/link astronaut profiles, create a mission/device, and assign a medical officer.
5. Submit a health reading and well-being report; upload audio only with a valid local sample file.
6. Log in as the assigned medical officer to review the report, create an observation/follow-up, and test recipient-scoped notifications.
7. Verify Mission Control sees only operational mission/device summaries and cannot retrieve private reports, raw readings, or observations.
8. Use permitted CSV/PDF exports and administrator audit history. These are software workflows and sample records, not clinical advice or real hardware data.

## Feature verification checklist

| Area | Status |
| --- | --- |
| Backend authentication, role checks, ownership and assignment checks | Implemented; automated coverage exists, but live HTTP verification is pending |
| Astronaut, user, mission, device, health, alert, observation, well-being, notification, export, and audit APIs | Implemented in backend; automated coverage exists, but full live-system verification is pending |
| OpenAPI/Swagger and CORS configuration | Configured in backend; runtime/browser verification is pending |
| React/Vite integration, responsive UI, browser voice recording and frontend tests | Not implemented in this workspace; frontend path required |
| PostgreSQL migration system / Flyway | Not implemented; production schema changes are not ready |
| Docker Compose local demo and backend container recipe | Added; Docker image/Compose execution has not been verified here |
| Production deployment, TLS, encrypted audio storage, retention, and audit archival | Not implemented or verified |
