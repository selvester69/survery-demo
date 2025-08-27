# Admin Module — Backend Functional Specification Document (FSD)

**Scope:** backend-only FSD for the Admin Module (no frontend code).
**Audience:** backend engineers, API designers, SREs, and implementation agents.

---

# Contents

1. Overview & Goals
2. Actors & Roles
3. Detailed Use Cases
4. Data Model & ERD
5. API Contracts (endpoints, request/response examples, errors)
6. Non-functional requirements (SLA, SLO, security, scale)
7. Operational considerations (monitoring, alerts, backups)
8. Acceptance criteria & QA checks
9. Agent instructions — how to convert this FSD into code (task list, priorities, tests, infra)

---

## 1. Overview & Goals

**Purpose:** Provide secure, auditable, scalable backend services for Admin Dashboard capabilities:

* Survey / link management
* Admin user & role management (RBAC)
* Analytics & reporting endpoints (aggregates)
* System health & operational endpoints
* Audit logging for admin actions

**Primary constraints / decisions**

* Language & stack: **Java (Spring Boot)** (matches previous conversation) — but the FSD is language-agnostic.
* Auth: OAuth2 / OIDC JWT for admins; RBAC enforced on every API.
* Source-of-Truth services:

  * Survey metadata: Survey Service (canonical) — Admin Module reads/writes via API (or shared DB when integrated).
  * Link info: Links Service can be queried for per-link metrics.
* DB: **PostgreSQL** (single logical DB for admin module metadata + audit logs)
* Queue/Events: **Kafka** for asynchronous tasks (export jobs, notifications)

---

## 2. Actors & Roles

* **Super Admin**

  * Full access: create/delete admin users, change roles, config system.
* **Survey Manager**

  * Create/edit surveys, manage links, export data.
* **Analyst**

  * Read-only access to analytics and reports.
* **Support User**

  * Limited actions: view surveys, run exports, open support tickets.

Each actor maps to RBAC roles stored in `admin_users` + `roles` tables.

---

## 3. Detailed Use Cases

Each use case includes: actor, preconditions, main flow, alternate/error flows, postconditions.

### UC-01: Admin Authentication (Login / Token Refresh)

* **Actor:** any admin user
* **Preconditions:** admin account exists; client holds credentials
* **Flow:**

  1. Admin client calls `POST /auth/token` (delegated to Auth Service) with credentials.
  2. Auth Service returns JWT (access token), refresh token.
  3. Backend validates JWT on each request via `Authorization: Bearer <token>`.
* **Alternate:** token expired → `POST /auth/refresh` with refresh token.
* **Postconditions:** authenticated session established.
* **Errors:** 401 for invalid credentials, 403 for locked accounts.

> Note: Auth Service is external; Admin Module must validate `aud`, `iss`, `exp` and enforce scopes/roles from token claims.

---

### UC-02: Create Survey (Admin)

* **Actor:** Survey Manager / Super Admin
* **Preconditions:** authenticated; has `surveys:write` scope
* **Flow:**

  1. Client calls `POST /admin/surveys` with survey payload.
  2. Admin Module validates payload; forwards to Survey Service (RPC/HTTP).
  3. On success, create audit entry and return 201 with created survey metadata.
* **Alternates:** validation fails → 400; Survey Service returns 409 (duplicate) → 409.
* **Postconditions:** survey exists in Survey Service; audit recorded.

---

### UC-03: Update Survey

* **Actor:** Survey Manager / Super Admin
* **Preconditions:** survey exists; permission
* **Flow:** `PATCH /admin/surveys/{id}` → validate → call Survey Service → audit → return 200.
* **Errors:** 404 if not found; 403 unauthorized.

---

### UC-04: List Surveys (paginated + filters)

* **Actor:** Analyst / Survey Manager
* **Preconditions:** authenticated; has `surveys:read`
* **Flow:** `GET /admin/surveys?page=&size=&status=&from=&to=&q=` → Admin Module queries Survey Service / DB, returns paginated list, with total count and links to stats.
* **Postconditions:** paginated response with filters applied.

---

### UC-05: View Link Metrics for Survey

* **Actor:** Survey Manager / Analyst
* **Preconditions:** survey exists
* **Flow:**

  1. `GET /admin/surveys/{id}/links?from=&to=&groupBy=day`
  2. Admin Module queries Links Service and Analytics (or reads aggregated tables) and returns link list with counts and trends.
* **Alternate:** large date window → return streamed export job (async).
* **Postconditions:** aggregated metrics returned or export started (202).

---

### UC-06: Regenerate Short Link / QR Code

* **Actor:** Survey Manager / Super Admin
* **Preconditions:** link exists; permission
* **Flow:**

  1. `POST /admin/links/{linkId}/regenerate` (optional body with `preserveOld`)
  2. Admin Module invokes Links Service; on success, updates local caches, writes audit.
  3. If QR regeneration requested, enqueue job to Kafka to generate new QR and upload to S3.
* **Postconditions:** new link/QR available; audit entry.

---

### UC-07: User Management (Create / Update Roles)

* **Actor:** Super Admin
* **Preconditions:** authenticated
* **Flow:**

  1. `POST /admin/users` with user info & roles.
  2. Create user record in `admin_users` table and optionally create account in Auth Service.
  3. Audit the action.
* **Errors:** 409 if email exists.

---

### UC-08: Export Data (Sync/Async)

* **Actor:** Survey Manager / Analyst
* **Preconditions:** permission: `exports:create`
* **Flow:**

  * Small exports: `GET /admin/surveys/{id}/export?format=csv` → stream response (200).
  * Large exports: `POST /admin/exports` → returns 202 with jobId; Admin Module enqueues export job to Kafka; worker processes and stores result in S3; user polls `GET /admin/exports/{jobId}`.
* **Postconditions:** export file created in S3 or streamed directly.

---

### UC-09: System Health & Metrics

* **Actor:** Support User / Super Admin
* **Preconditions:** permission: `system:read`
* **Flow:** `GET /admin/health` returns aggregated service status (DB, Redis, Kafka, Links Service connectivity, Survey Service connectivity), lag metrics, and recent alerts.

---

### UC-10: Audit Trail Retrieval

* **Actor:** Super Admin
* **Preconditions:** authenticated
* **Flow:** `GET /admin/audit?entityType=survey&entityId=...&from=&to=` returns paginated audit events.
* **Security:** only Super Admins or role with `audit:read`.

---

## 4. Data Model & ERD

Below are admin-related tables. Use UUID PKs, references by foreign keys.

### Mermaid ERD (copy to any mermaid renderer)

```mermaid
erDiagram
    admin_users {
      UUID id PK
      VARCHAR email UNIQUE
      VARCHAR name
      BOOLEAN active
      TIMESTAMPTZ created_at
      TIMESTAMPTZ updated_at
    }

    roles {
      UUID id PK
      VARCHAR name UNIQUE
      TEXT description
      TIMESTAMPTZ created_at
    }

    user_roles {
      UUID id PK
      UUID user_id FK -> admin_users.id
      UUID role_id FK -> roles.id
      TIMESTAMPTZ assigned_at
      UUID assigned_by
    }

    audit_events {
      BIGSERIAL id PK
      UUID actor_id FK -> admin_users.id NULLABLE
      VARCHAR actor_email
      VARCHAR action
      TEXT entity_type
      VARCHAR entity_id
      JSONB payload
      TIMESTAMPTZ occurred_at
      VARCHAR ip
      VARCHAR user_agent
    }

    export_jobs {
      UUID id PK
      UUID requested_by FK -> admin_users.id
      TEXT status  /* PENDING, RUNNING, COMPLETED, FAILED */
      VARCHAR format
      JSONB parameters
      VARCHAR s3_key
      TIMESTAMPTZ created_at
      TIMESTAMPTZ completed_at
    }

    link_overrides {
      UUID id PK
      VARCHAR link_id UNIQUE
      UUID survey_id
      VARCHAR target_url
      BOOLEAN active
      TIMESTAMPTZ created_at
    }

    admin_users ||--o{ user_roles : has
    roles ||--o{ user_roles : assigned
    admin_users ||--o{ audit_events : performs
    admin_users ||--o{ export_jobs : requests
```

### Column details & indexes (recommended)

#### `admin_users`

* `id UUID PK` default `gen_random_uuid()`
* `email VARCHAR(254) UNIQUE NOT NULL`
* `name VARCHAR(200)`
* `active BOOLEAN DEFAULT true`
* `created_at TIMESTAMPTZ DEFAULT now()`
* `updated_at TIMESTAMPTZ DEFAULT now()`
* Index: `idx_admin_users_email(email)`

#### `roles`

* `id UUID PK`
* `name VARCHAR(100) UNIQUE NOT NULL`
* `description TEXT`

#### `user_roles`

* `id UUID PK`
* `user_id UUID NOT NULL`
* `role_id UUID NOT NULL`
* `assigned_at TIMESTAMPTZ DEFAULT now()`
* Constraint unique(user\_id, role\_id)
* Index: `idx_user_roles_userid`

#### `audit_events`

* `id BIGSERIAL PK`
* `actor_id UUID NULL`
* `actor_email VARCHAR(254) NULL`
* `action VARCHAR(200) NOT NULL`
* `entity_type VARCHAR(100) NOT NULL`
* `entity_id VARCHAR(100) NULL`
* `payload JSONB`
* `occurred_at TIMESTAMPTZ DEFAULT now()`
* Index: `idx_audit_entity (entity_type, entity_id)`
* Retention: archive to S3 or purge after policy (e.g., 1 year), GDPR-friendly

#### `export_jobs`

* `id UUID PK`
* `requested_by UUID`
* `status VARCHAR(20) NOT NULL`
* `format VARCHAR(10)`
* `parameters JSONB`
* `s3_key VARCHAR(512) NULL`
* `created_at TIMESTAMPTZ DEFAULT now()`
* `completed_at TIMESTAMPTZ NULL`
* Index: `idx_export_jobs_status(created_at, status)`

#### `link_overrides`

* For admin overrides of Links Service if needed
* `link_id` unique; index `idx_link_overrides_linkid`

---

## 5. API Contracts

> Standard API behavior:
>
> * All `/admin/**` endpoints require Authorization header `Bearer <token>`.
> * Response JSON envelope for errors:
>
> ```json
> {
>   "error": {
>     "code": "ERROR_CODE",
>     "message": "Human readable message",
>     "details": {}
>   },
>   "requestId": "uuid"
> }
> ```
>
> * All successful responses have `requestId` in response headers.
> * Dates are ISO-8601 in UTC (e.g., `"2025-08-26T14:10:00Z"`).
> * Use pagination model: `page`, `size`, and `total`, `items[]`.

---

### 5.1 Authentication / Health

#### GET `/admin/health`

* **Purpose:** aggregate health of dependencies
* **Auth:** requires `system:read`
* **Response 200**

```json
{
  "status": "OK",
  "components": {
    "postgres": { "status": "OK", "latencyMs": 12 },
    "redis": { "status": "OK", "latencyMs": 4 },
    "kafka": { "status": "WARN", "lag": 1000 },
    "surveyService": { "status": "OK", "latencyMs": 50 },
    "linksService": { "status": "OK", "latencyMs": 22 }
  },
  "timestamp": "2025-08-26T14:10:00Z"
}
```

---

### 5.2 Admin Users (RBAC)

#### POST `/admin/users`

* **Auth:** `user:create` or `SUPER_ADMIN`
* **Request:**

```json
{
  "email": "alice@example.com",
  "name": "Alice Admin",
  "roles": ["SURVEY_MANAGER", "ANALYST"],
  "sendInvite": true
}
```

* **Response 201:**

```json
{
  "id": "uuid",
  "email": "alice@example.com",
  "name": "Alice Admin",
  "roles": ["SURVEY_MANAGER", "ANALYST"],
  "active": true,
  "createdAt": "2025-08-25T09:00:00Z"
}
```

* **Errors:** 400, 409 (email exists), 403 (insufficient permission)

#### GET `/admin/users?page=1&size=50&query=alice`

* **Auth:** `user:read`
* **Response 200:**

```json
{
  "page": 1,
  "size": 50,
  "total": 2,
  "items": [
    { "id":"uuid","email":"alice@example.com","name":"Alice","roles":["SURVEY_MANAGER"],"active":true,"createdAt":"..." }
  ]
}
```

#### PATCH `/admin/users/{userId}`

* **Auth:** `user:update`
* **Body:** partial updates (roles, active)
* **Response 200:** updated user

#### DELETE `/admin/users/{userId}`

* **Auth:** `SUPER_ADMIN`
* **Behavior:** soft-delete -> set `active=false`; audit record
* **Response 204**

---

### 5.3 Roles

#### GET `/admin/roles`

* **Auth:** `roles:read`
* **Response:** list of roles and permissions

#### POST `/admin/roles`

* **Auth:** `SUPER_ADMIN`
* Create new role with a set of permissions. Response 201.

---

### 5.4 Surveys & Links (Admin-level endpoints)

> Implementation note: Admin Module may proxy or orchestrate calls to Survey Service and Links Service. Contracts below assume Admin Module orchestrates.

#### POST `/admin/surveys`

* **Auth:** `surveys:write`
* **Request:** (minimal)

```json
{
  "title": "Customer Feedback Q4",
  "description": "Quarterly NPS and feedback",
  "ownerId": "uuid",
  "settings": { "collectGeo": true }
}
```

* **Response 201:** returns survey metadata as created by Survey Service.

#### PATCH `/admin/surveys/{surveyId}`

* **Auth:** `surveys:write`
* **Request:** partial updates
* **Response 200**

#### GET `/admin/surveys?page=1&size=25&status=active&from=2025-01-01&to=2025-08-26`

* **Auth:** `surveys:read`
* **Response 200:** paginated list including `linkSummary` (count, totalClicks)

Example single survey item:

```json
{
  "id": "survey-uuid",
  "title": "Customer Feedback Q4",
  "status": "ACTIVE",
  "createdAt": "2025-07-01T10:00:00Z",
  "ownerId": "uuid",
  "linkSummary": {
    "linksCount": 3,
    "totalClicks": 1523,
    "lastClickAt": "2025-08-25T12:12:00Z"
  }
}
```

#### GET `/admin/surveys/{surveyId}/links?from=&to=&groupBy=day`

* **Auth:** `surveys:read`
* **Response 200:** list of links with daily aggregates:

```json
{
  "links": [
    {
      "linkId": "aB3xZ9",
      "shortUrl": "https://domain.com/s/aB3xZ9",
      "clicks": 105,
      "daily": [{"date":"2025-08-20","count":10}, ...],
      "createdAt": "2025-07-01T10:00:00Z"
    }
  ]
}
```

#### POST `/admin/links/{linkId}/regenerate`

* **Auth:** `links:write`
* **Request:**

```json
{ "preserveOld": false, "regenQRCode": true }
```

* **Response 200:** new link metadata or 202 if async.

---

### 5.5 Export Jobs

#### POST `/admin/exports`

* **Auth:** `exports:create`
* **Request:**

```json
{
  "type": "survey_responses",
  "surveyId": "uuid",
  "format": "csv",
  "filters": { "from": "2025-01-01", "to":"2025-08-26" },
  "notifyWhenReady": true
}
```

* **Behavior:** if estimated size small (< threshold) then perform inline stream; else create job (202) with `jobId`.
* **Response 202:**

```json
{ "jobId": "uuid", "status": "PENDING", "estimatedSize": 12345 }
```

#### GET `/admin/exports/{jobId}`

* **Auth:** `exports:read`
* **Response 200:**

```json
{
  "jobId":"uuid",
  "status":"COMPLETED",
  "s3Key":"s3://bucket/exports/export-uuid.csv",
  "downloadUrl":"https://s3-presigned-url",
  "createdAt":"2025-08-25T10:00:00Z",
  "completedAt":"2025-08-25T10:02:00Z"
}
```

---

### 5.6 Audit Logs

#### GET `/admin/audit?entityType=survey&entityId=&from=&to=&page=&size=`

* **Auth:** `audit:read` (Super Admin)
* **Response 200:**

```json
{
  "items":[
    {
      "id": 12345,
      "actorId": "uuid",
      "actorEmail": "alice@example.com",
      "action": "SURVEY.UPDATE",
      "entityType": "survey",
      "entityId": "survey-uuid",
      "payload": {"before": {...}, "after": {...}},
      "occurredAt": "2025-08-25T09:12:00Z",
      "ip": "1.2.3.4"
    }
  ],
  "page":1,"size":50,"total":1
}
```

---

### 5.7 System Config & Settings

#### GET `/admin/config/{key}`

* **Auth:** `system:read`
* **Response 200:** config value.

#### PATCH `/admin/config/{key}`

* **Auth:** `system:write` (Super Admin)
* Update in DB / Config Store and audit.

---

### 5.8 Error Codes (common)

* `400` — `BAD_REQUEST` payload validation failure
* `401` — `UNAUTHENTICATED`
* `403` — `FORBIDDEN` (insufficient scopes/roles)
* `404` — `NOT_FOUND`
* `409` — `CONFLICT` (duplicate)
* `410` — `GONE`
* `429` — `RATE_LIMIT_EXCEEDED`
* `500` — `INTERNAL_ERROR`
* `503` — `DEPENDENCY_UNAVAILABLE` (Survey Service or Links Service down)

Each error response includes the `error` object (see envelope above).

---

## 6. Non-functional Requirements

### Performance & Scale

* Serve 100 concurrent admin users with sub-second list queries for paginated pages (page size 50).
* Support export jobs: parallel workers (configurable) process jobs from Kafka.
* DB sizing: plan for audit\_events to accumulate — use partitioning by month for `audit_events` and `export_jobs`.

### Availability & SLOs

* **Admin API Availability:** 99.9% monthly
* **Health endpoints:** 30s scrape for metrics
* **Export job latency:** small exports < 30s inline; large exports processed within 10 minutes (depends on size)

### Security

* JWT validation: check signature, `iss`, `aud`, `exp`.
* Enforce RBAC: map token claims (`roles` or `scope`) into allowed actions.
* Rate limit admin endpoints: 600/min per admin account; stricter for write operations.
* Audit all write operations with actor, timestamp, IP, and user-agent.
* Store PII with care (admin emails ok); mask IPs if required for privacy.

### Data Retention

* Audit logs: retain 12 months online; archive older to S3 Glacier.
* Export files: retain in S3 for configurable TTL (default 30 days).

### Observability & Tracing

* OpenTelemetry traces across calls to Survey & Links service.
* Metrics:

  * request\_count, request\_latency\_seconds (p50/p95/p99), error\_rate
  * cache\_hit\_rate for any caching layers
  * export\_job\_lag, export\_job\_duration
* Logs must include `requestId`, `actorId`, `correlationId`.

---

## 7. Operational Considerations

### Backups & DR

* PG backups daily; point-in-time recovery enabled.
* Export job metadata and audit logs backed up to S3 (or archived monthly).

### Deployments

* Blue/green or Canary deployments for admin APIs.
* Feature flags for rollout of analytics endpoints.

### Runbooks (short)

* **DB unavailable:** return `503` for admin listing endpoints; non-critical read endpoints return cached data (if available).
* **Export queue backlog:** scale up workers; alert if lag > 5 minutes.
* **Audit log growth spike:** rotate/compact partitions; AD-hoc purge if abuse detected.

---

## 8. Acceptance Criteria & QA checks

**General**

* All endpoints secured & validated with JWT and RBAC.
* Unit tests cover 80%+ of service logic.
* Integration tests (Testcontainers) for DB and Redis.
* Contract tests for interactions with Survey & Links services (consumer-driven contracts).

**API-specific**

* POST/GET/PATCH flows for surveys and users working end-to-end.
* Export jobs: create → process → S3 object is available and `GET /admin/exports/{jobId}` returns `COMPLETED`.
* Audit events present for every write action.

**Performance**

* Paging endpoints: P95 latency < 500ms for page size 50 under 100 concurrent users.
* Health check responds < 500ms.

---

## 9. Agent Instructions — Convert this FSD into Implementation (no code, actionable plan)

This is an agentic task list with priorities, sub-tasks, tests, infra, and expected deliverables. Use branches and PRs with CI.

### Overall implementation approach

* Use feature-branch workflow: `feature/admin-backend/<short-desc>`
* Create smaller PRs per submodule (users, roles, surveys, links, exports, audit)
* Each PR must include: API contract addition (OpenAPI spec), unit tests, integration tests, and DB migration.

---

### Priority 1 — Core infra & auth glue (Sprint 0)

**Objective:** wire up base app, auth validation, DB, and audit trail.

Tasks:

1. Setup project skeleton (Maven modules or single service).
2. Add JWT validation middleware: validate `iss`,`aud`,`exp` and extract `sub`, `email`, `roles`.
3. Implement `admin_users`, `roles`, `user_roles`, `audit_events` DB migrations (Flyway/Liquibase).
4. Implement audit logger component (used by services to write events).
5. Implement health check endpoint (`/admin/health`) that checks PG, Redis, Kafka, and downstream services.

Acceptance:

* JWT validation tested (unit test mocks).
* DB migrations applied on startup in dev.
* Health check returns OK when dependencies healthy.

---

### Priority 2 — User & Role management

**Objective:** CRUD for admin users & roles + RBAC enforcement.

Tasks:

1. Implement `POST /admin/users`, `GET /admin/users`, `PATCH /admin/users/{id}`, `DELETE` (soft).
2. Implement `GET /admin/roles`, `POST /admin/roles`.
3. Implement `user_roles` management (assign/remove roles).
4. Add unit tests and integration tests using Testcontainers.
5. Add OpenAPI schemas and examples.

Acceptance:

* API contract coverage by tests.
* Role assignment enforced across endpoints.
* Audit entries written for create/update/delete.

---

### Priority 3 — Surveys orchestration & links metrics endpoints

**Objective:** orchestration layer to Survey Service and Links Service.

Tasks:

1. Implement `GET /admin/surveys` (paged) that proxies to Survey Service or aggregates DB where available.
2. Implement `POST /admin/surveys` and `PATCH /admin/surveys/{id}` (orchestrate to Survey Service and persist metadata in admin DB if needed).
3. Implement `GET /admin/surveys/{id}/links` that aggregates link metrics by calling Links Service (or reading aggregate tables).
4. Add caching for link metrics with Redis (TTL 60s); invalidations on link/ survey update.

Acceptance:

* Endpoint returns expected data shape.
* Caching tests validate cache usage and invalidation behavior.
* Circuits: if Links Service down, return helpful 503 with fallback to cached data.

---

### Priority 4 — Export jobs & async processing

**Objective:** implement export job creation, processing pipeline, storage in S3.

Tasks:

1. Implement `POST /admin/exports` (decide inline vs async based on estimator).
2. Produce jobs to Kafka topic `admin-exports`.
3. Implement worker skeleton (separate process) to consume Kafka, generate exports (stream from Survey/Response service), upload to S3, update `export_jobs`.
4. Implement `GET /admin/exports/{jobId}` and secure S3 presigned URL generation.
5. Tests: end-to-end with local S3 (MinIO) and Kafka (Testcontainers).

Acceptance:

* Export jobs created and taken by worker.
* Completed exports produce S3 object and `GET` returns presigned URL.

---

### Priority 5 — Links management & QR generation orchestration

**Objective:** admin-level link operations (regenerate) and QR job enqueuing.

Tasks:

1. Implement `POST /admin/links/{linkId}/regenerate` — call Links Service for regeneration.
2. Enqueue QR generation job to Kafka if requested; worker uses ZXing to generate and uploads to S3.
3. Audit and invalidate local caches.

Acceptance:

* Regeneration successful, audit entry present, QR file in S3 after worker completes.

---

### Priority 6 — Monitoring, metrics, SLOs, and runbooks

**Objective:** production readiness.

Tasks:

1. Add Prometheus metrics instrumentation (request latency, error rates, job durations).
2. Add OpenTelemetry tracing for cross-service flows.
3. Add alert rules for high error rate, export backlog, DB connection loss.
4. Provide runbook excerpts for critical failures.

Acceptance:

* Dashboards for Admin API operations exist.
* Alerts fire in canary test scenarios.

---

### Testing & QA instructions (agent must implement)

* **Unit tests:** mock external services (Survey, Links, Auth), cover validation and business logic.
* **Integration tests:** use Testcontainers for PostgreSQL, Redis, Kafka, and MinIO.
* **Contract tests:** use Pact or similar to verify interactions with Survey & Links services.
* **Performance tests:** load test paginated list endpoints with k6 to meet latency targets.
* **Security tests:** static SAST (e.g., SpotBugs/SpotBugs), dependency scan (Snyk/Trivy), and auth/permission fuzzing.

---

### CI/CD & Deployment notes

* CI pipeline steps:

  1. Build & unit tests
  2. Static analysis & security scan
  3. Integration tests (Testcontainers)
  4. Build Docker image & push to registry
  5. Run contract tests (or on PR merge)
* Deploy to EKS with Rolling/Canary; use Feature Flags for new API endpoints.
* Secrets: use AWS Secrets Manager. DB credentials read from Secrets Manager at startup.

---

### Branching & PR rules

* `main` protected; PRs require 2 reviewers and passing CI.
* Semantic PR titles, link to FSD use cases implemented.
* Each PR must include OpenAPI change (if any) and updated DB migrations.

---

### Observability & Troubleshooting (developer tips)

* Log format: JSON with fields: `timestamp, requestId, userId, actorEmail, path, method, status, durationMs, error`.
* Correlate using `requestId` propagated to Survey & Links service calls.
* When debugging export jobs, inspect Kafka topic `admin-exports` and worker logs.

---

### Deliverables for each feature PR

* Implemented endpoint(s)
* Unit & integration tests
* DB migration file (SQL or Liquibase/Flyway)
* OpenAPI changes + examples
* README excerpt for that module (how to run locally)
* At least one end-to-end test (if external services are simulated)

---

## Appendix — Example DB migration snippets (conceptual, not code)

* Create `admin_users` table
* Create `roles` and `user_roles`
* Create `audit_events` (partitioned monthly)
* Create `export_jobs`

> Use Flyway or Liquibase to version migrations.

---

## Final Notes & Next Steps

1. **Confirm scope & integration mode:**

   * Do admin endpoints orchestrate Survey & Links services (recommended) or write directly into shared DBs? (Orchestration preferred.)
2. **Pick tech choices for worker / export processing:**

   * Reuse same service with separate worker profile or separate microservice?
3. **Prioritize sprint backlog** based on Priority list above. Implement in small PRs with clear acceptance criteria.
4. **If you want, I can now:**

   * produce OpenAPI YAML for all admin endpoints above (detailed schemas + examples), **or**
   * produce Flyway migration SQL templates for the ERD, **or**
   * produce a concrete sprint backlog (issues/tasks) ready to import to a tracker.

Which of those would you like next?
