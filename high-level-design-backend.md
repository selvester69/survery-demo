# HLD

## Backend HLD

The backend for the Client Survey Application will be a **Modular Monolith** built on the **Java platform**, specifically using the **Spring Boot framework**. This architecture provides a robust, scalable, and maintainable foundation, leveraging well-established design patterns and industry-standard technologies.

---

### **1. Core Backend Technologies**

* **Framework:** **Spring Boot** with **Spring Data JPA** for database interaction.
* **Language:** Java.
* **Database:** **PostgreSQL** for all data storage, leveraging its advanced `JSONB` data type for dynamic schemas.
* **Build Tool:** Maven or Gradle.
* **Message Broker:** A lightweight, internal message queue (e.g., Spring's `ApplicationEventPublisher` or a simple in-memory queue) for asynchronous event handling.

---

### **2. Backend Service Modules**

Each service is implemented as a cohesive Spring module with its own controllers, services, repositories, and DTOs (Data Transfer Objects).

#### **2.1. Survey & Question Management (`SurveyService.java`)**

* **Purpose:** Manages the entire lifecycle of a survey from creation to archival.
* **API Endpoints (`SurveyController.java`):**
  * `POST /api/v1/surveys`: Creates a new survey and its initial settings.
  * `GET /api/v1/surveys/{id}`: Retrieves a specific survey and its questions.
  * `PUT /api/v1/surveys/{id}`: Updates survey metadata and settings.
  * `DELETE /api/v1/surveys/{id}`: Archives or soft-deletes a survey.
* **Database Interactions:** Uses a `SurveyRepository.java` interface (extending `JpaRepository`) to perform CRUD operations on the `surveys` and `questions` tables.
* **`QuestionModule` Integration:** The `SurveyService` will internally call the `QuestionService` to handle the specific logic for adding or updating questions.

---

#### **2.2. Authentication & User Management (`AuthService.java`)**

* **Purpose:** Handles all admin authentication and access control.
* **API Endpoints (`AuthController.java`):**
  * `POST /api/v1/auth/login`: Authenticates an admin user and issues a **JWT (JSON Web Token)**.
  * `GET /api/v1/users/me`: Retrieves the profile of the currently authenticated user.
* **Security:** Implements **Spring Security** for JWT validation, role-based access control, and password hashing (using BCrypt).
* **Database:** Manages the `admin_users` table.

---

#### **2.3. Response Intake & Metadata (`ResponseService.java`)**

* **Purpose:** The public-facing entry point for survey submissions. This module is designed for high-throughput, as it's hit by every respondent.
* **API Endpoint (`ResponseController.java`):**
  * `POST /api/v1/surveys/{linkId}/submit`: The endpoint that accepts the completed survey response.
* **Detailed Flow:**
    1. **Input DTO:** A custom DTO will be used to deserialize the incoming JSON payload.
    2. **Validation:** The service validates the submitted answers against the `question_config` stored in the `questions` table.
    3. **Transaction:** A single database transaction is started.
    4. **Database Insertion:** The service saves a new row to the `responses` table and multiple rows to the `answers` table. The raw `location_data` and `device_info` JSON will be stored directly in `JSONB` columns.
    5. **Event Emission:** On a successful transaction commit, it uses `ApplicationEventPublisher` to emit a `SurveyResponseSubmittedEvent` asynchronously.

---

#### **2.4. Analytics & Reporting (`AnalyticsService.java`)**

* **Purpose:** Processes and aggregates response data for the Admin Dashboard.
* **Event-Driven Processing:** This module has a listener (`@EventListener`) that subscribes to `SurveyResponseSubmittedEvent`.
* **API Endpoints (`AnalyticsController.java`):**
  * `GET /api/v1/analytics/surveys/{id}/summary`: Returns key performance indicators (KPIs) like total responses.
  * `GET /api/v1/analytics/surveys/{id}/charts/location_map`: Provides data for geographic visualization.
* **Optimization:** This module will maintain an `analytics_cache` table to store pre-calculated metrics, ensuring that API requests for dashboard data are fast.

---

#### **2.5. Location & Device Modules (`LocationService.java`, `DeviceInfoService.java`)**

* **Purpose:** Helper services that encapsulate external API calls and data parsing.
* **`LocationService`:**
  * Makes a REST call to a third-party IP geolocation API.
  * **Fallback Logic:** If GPS coordinates are not provided by the frontend, this service will be called to resolve the respondent's IP address.
* **`DeviceInfoService`:**
  * Uses a library (e.g., `user-agent-string`) to parse the User-Agent header and extract browser, device, and OS information.

---

### **3. Data Storage Details**

* **Repository Pattern:** Each module will have a dedicated repository interface that provides methods for data access.
* **Entities:**
  * `SurveyEntity.java`, `QuestionEntity.java`, `ResponseEntity.java`, `AnswerEntity.java`, `SurveyLinkEntity.java`, `UserEntity.java`.
* **PostgreSQL with JSONB:**
  * The `settings` field in `surveys`, `question_config` in `questions`, and the `location_data`, `device_info`, and `answer_value` fields will all be mapped to **PostgreSQL's `JSONB` columns**. This allows for a flexible schema without a separate NoSQL database, simplifying the infrastructure.

---

### **4. System Integration & Performance**

* **Transaction Management:** Use Spring's `@Transactional` annotation to ensure data integrity during write operations (e.g., when a response is saved).
* **Asynchronous Processing:** The use of `ApplicationEventPublisher` or a similar mechanism decouples the response submission from the analytics aggregation, improving the user experience by reducing response time.
* **Caching:** A **Redis** instance could be added in a future phase to cache frequently accessed data, such as survey configurations, further improving performance.
* **Security:** All API endpoints will be protected by Spring Security filters. Rate-limiting can be implemented at the API Gateway or using a dedicated library like `Bucket4j`.

## API Details

### **1. Admin & User Management APIs**

These endpoints are protected and require a valid JWT token for authentication.

| API Endpoint | HTTP Method | Description | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/auth/login` | `POST` | Authenticates an admin user. | `{"username": "string", "password": "string"}` | `{"token": "JWT_token", "role": "admin"}` | `401 Unauthorized` |
| `/api/v1/users/me` | `GET` | Retrieves the profile of the authenticated user. | `(None)` | `{"id": "uuid", "username": "string", "email": "string", "role": "admin"}` | `401 Unauthorized` |
| `/api/v1/users` | `POST` | Creates a new admin user (requires super-admin role). | `{"username": "string", "email": "string", "role": "string"}` | `201 Created` | `403 Forbidden`, `409 Conflict` |
| `/api/v1/users/{id}` | `PUT` | Updates an existing admin user's details. | `{"username": "string", "email": "string", "role": "string"}` | `200 OK` | `403 Forbidden`, `404 Not Found` |

---

### **2. Survey & Question Management APIs**

These endpoints are used by the Admin Dashboard to manage survey content.

| API Endpoint | HTTP Method | Description | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/surveys` | `POST` | Creates a new survey with initial settings and questions. | `{"title": "string", "description": "string", "questions": ["QuestionObject"], "settings": "JSON"}` | `{"id": "uuid", "title": "string", ...}` | `400 Bad Request` |
| `/api/v1/surveys/{id}` | `GET` | Retrieves a full survey object, including all questions. | `(None)` | `{"id": "uuid", "title": "string", "questions": ["QuestionObject"], ...}` | `404 Not Found` |
| `/api/v1/surveys/{id}` | `PUT` | Updates a survey's settings or questions. | `{"title": "string", "settings": "JSON", "questions": ["QuestionObject"]}` | `200 OK` | `400 Bad Request`, `404 Not Found` |
| `/api/v1/surveys/{id}` | `DELETE` | Archives/deletes a survey. | `(None)` | `204 No Content` | `404 Not Found` |

---

### **3. Survey Distribution & Public APIs**

These endpoints are used by the public-facing survey interface and are open to all users.

| API Endpoint | HTTP Method | Description | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/links/{linkId}` | `GET` | **Validates a survey link** and returns the survey's public configuration. | `(None)` | `{"surveyId": "uuid", "title": "string", "welcomeMessage": "string", "questions": ["QuestionObject"]}` | `404 Not Found`, `410 Gone` |
| `/api/v1/surveys/{linkId}/submit` | `POST` | **Submits a completed survey response.** | `{"answers": ["AnswerObject"], "location": "JSON", "deviceInfo": "JSON"}` | `{"message": "Response submitted successfully."}` | `400 Bad Request`, `410 Gone` |
| `/api/v1/links/{linkId}/qr-code` | `GET` | Generates and returns a QR code image for the given link. | `(None)` | `200 OK` (Image/PNG) | `404 Not Found` |

---

### **4. Analytics & Export APIs**

These endpoints provide data for the admin analytics dashboard.

| API Endpoint | HTTP Method | Description | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/analytics/surveys/{id}/summary` | `GET` | Retrieves key summary statistics for a survey. | `(None)` | `{"totalResponses": 123, "completionRate": 0.85, ...}` | `404 Not Found` |
| `/api/v1/analytics/surveys/{id}/responses` | `GET` | Fetches a paginated list of all responses for a survey. | `(None)` | `{"responses": ["ResponseObject"], "pagination": {...}}` | `404 Not Found` |
| `/api/v1/analytics/surveys/{id}/charts/trends` | `GET` | Returns time-series data for trend charts. | `(None)` | `[{"date": "YYYY-MM-DD", "count": "number"}, ...]` | `404 Not Found` |
| `/api/v1/analytics/surveys/{id}/charts/location` | `GET` | Provides aggregated location data for the map visualization. | `(None)` | `[{"city": "string", "count": "number"}, ...]` | `404 Not Found` |
| `/api/v1/analytics/surveys/{id}/export` | `POST` | Triggers a background job to export survey data. | `{"format": "csv"}` | `{"jobId": "uuid", "status": "queued"}` | `400 Bad Request` |
| `/api/v1/analytics/exports/{jobId}` | `GET` | Checks the status of an export job and provides a download URL. | `(None)` | `{"status": "completed", "url": "download_url"}` | `202 Accepted` (if in progress), `404 Not Found` |

---

### **5. Privacy & Compliance APIs**

| API Endpoint | HTTP Method | Description | Request Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/privacy/consent` | `POST` | Records user consent for data collection. | `{"surveyId": "uuid", "consentType": "location", "status": "granted"}` | `200 OK` | `400 Bad Request` |
| `/api/v1/privacy/data-deletion`| `POST` | Initiates a data deletion request for a respondent. | `{"responseId": "uuid"}` | `202 Accepted` | `404 Not Found` |
