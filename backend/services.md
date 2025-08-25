# Service Definitions and Data Mapping

This document outlines the responsibilities of each microservice and maps the database tables from the original monolithic design to their new owners.

---

### 1. Auth Service

*   **Responsibilities:** Handles user authentication, registration, and management. It is responsible for issuing and validating JWTs to secure access to other services.
*   **Mapped Tables:**
    *   `users` (A new table, as the original schema in the README didn't explicitly define one, but it's a necessary component for authentication).

---

### 2. Survey Service

*   **Responsibilities:** Manages the core survey content. This includes creating, reading, updating, and deleting surveys and their associated questions. It holds the "master record" for survey structures.
*   **Mapped Tables:**
    *   `surveys`
    *   `questions`

---

### 3. Links Service

*   **Responsibilities:** Creates and manages the unique, shareable links for each survey. It functions as a "tiny URL" service, tracking basic click metrics and resolving a link to its corresponding `survey_id`.
*   **Mapped Tables:**
    *   `survey_links`

---

### 4. Response Service

*   **Responsibilities:** Handles the high-throughput ingestion of survey submissions. It is optimized for write-heavy workloads to ensure that no response is lost, even under heavy traffic. It validates and stores the raw response data.
*   **Mapped Tables:**
    *   `responses`
    *   `answers`

---

### 5. Analytics Service

*   **Responsibilities:** Provides aggregated insights and data visualization. It consumes events (e.g., "response submitted") from a message bus, processes this data, and stores it in a format optimized for read-heavy queries from the analytics dashboard. It is also responsible for handling location data and other metadata.
*   **Mapped Tables:**
    *   `analytics_events`
    *   `location_data` (A new, denormalized table for efficient location-based queries).
    *   Potentially other aggregated/materialized views for performance.
