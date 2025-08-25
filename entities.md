# Entity

## diagram

### **1. Microservices Architecture Overview**

In this new design, the application is broken down into four core services:

1. **Auth Service**: Manages user authentication and administration.
2. **Survey Service**: Handles the creation and management of surveys and questions.
3. **Analytics Service**: Processes and visualizes survey responses and data.
4. **Response Service**: The public-facing service that handles high-volume survey submissions.

These services will communicate with each other via RESTful APIs and, where beneficial for decoupling and resilience, an asynchronous message bus (like RabbitMQ or Kafka).

---

### **2. Entity Relationship Diagram (ERD)**

Since each service is now decoupled, a central, monolithic database is no longer the best approach. Instead, each service will manage its own data, minimizing dependencies between services.

- **`Auth Service` Database:**
  - **`Admin_Users`** Table: Stores user authentication details.
- **`Survey Service` Database:**
  - **`Surveys`** Table
  - **`Questions`** Table
  - **`Survey_Links`** Table
- **`Response Service` Database:**
  - **`Responses`** Table: Stores the core response data.
  - **`Answers`** Table: Links to the responses and contains the submitted answers.
- **`Analytics Service` Database:**
  - **`Analytics_Cache`** Table: Stores pre-aggregated, denormalized data for dashboards. It receives data from the `Response Service` via a message queue.

**Explanation of Relationships:**

The relationships now exist *between* services rather than within a single database. For example, the `Response` table will no longer have a direct foreign key to the `Surveys` table. Instead, it will store the `survey_id` as a simple column. The `Analytics Service` will consume responses from a message queue to build its cache.

---

### **3. Class Diagram**

The class diagram is now broken down into separate diagrams for each microservice, highlighting the classes and their interactions within that service boundary.

#### **3.1. Auth Service**

- **Controller:** `AuthController` handles login and user management endpoints.
- **Service:** `AuthService` contains the business logic for authentication.
- **Repository:** `UserRepository` manages the `UserEntity`.
- **Entity:** `UserEntity` maps to the `admin_users` table.

#### **3.2. Survey Service**

- **Controller:** `SurveyController` handles survey and question CRUD operations.
- **Service:** `SurveyService` contains the logic for managing surveys.
- **Repository:** `SurveyRepository`, `QuestionRepository`, and `SurveyLinkRepository`.
- **Entities:** `SurveyEntity`, `QuestionEntity`, and `SurveyLinkEntity`.

#### **3.3. Response Service**

- **Controller:** `ResponseController` is the public-facing endpoint for survey submissions.
- **Service:** `ResponseService` validates and persists responses. It also publishes an event to the message bus upon a successful submission.
- **Repository:** `ResponseRepository` and `AnswerRepository`.
- **Entities:** `ResponseEntity` and `AnswerEntity`.

#### **3.4. Analytics Service**

- **Controller:** `AnalyticsController` exposes endpoints for dashboard data.
- **Service:** `AnalyticsService` contains the data aggregation logic. It listens for `SurveyResponseSubmittedEvent` from the message bus to update its cache.
- **Repository:** `AnalyticsCacheRepository`.
- **Entity:** `AnalyticsCacheEntity`.

**Note:** In this microservices model, the services are loosely coupled. Changes to one service's internal classes or database schema will not directly impact the others, as long as the public API contracts remain stable. This approach also allows for independent scaling of each service based on its specific load.
