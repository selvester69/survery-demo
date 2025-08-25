# HLD

## Backend HLD

**Folder:** /backend

### 1. Survey & Question Management

- **SurveyService:** CRUD API for surveys. Handles creation, updates, status transitions, and settings. Stores reference to questions and links.
- **QuestionService:** Handles all question logic. Supports type definitions, constraints, conditional/branching logic.
- **SurveyLinkService:** Unique ID/URL generation, link status, QR code creation, sharing, and metrics.

### 2. Authentication & User Management

- **AuthService:** JWT/OAuth-based authentication, session control, and password resets.
- **UserService:** Admin/user profile data, permissions, role assignment.

### 3. Distribution & Access Control

- **DistributionService:** Email/SMS/QR code registration, bulk sends, link expiration, usage stats.
- **AccessControlService:** Optional password protection, rate-limiting, anonymous toggle.

### 4. Response Intake & Metadata

- **ResponseService:** Accepts survey responses, validates, records with location/device info.
- **LocationService:** Geolocation API integration (GPS, IP fallback), timezone assignment, accuracy rating.
- **DeviceInfoService:** Browser/user-agent parser, stores screen/device type.

### 5. Analytics & Reporting

- **AnalyticsService:** Aggregates responses, KPIs, computes trends, response funnel, device map, heatmaps.
- **ExportService:** Generates Excel, CSV, PDF reports, delivers via secure download or cloud drive integration.

### 6. Core Data Storage

- **Database:**  
  - surveys: id, title, settings, etc.
  - questions: id, survey_id, type, config
  - responses: id, survey_id, respondent_id, timestamp, location, device details
  - answers: id, response_id, question_id, value
  - survey_links: id, survey_id, url, status, clicks
  - analytics_events: id, survey_id, event_type, data
- **File Storage:** Cloud storage for uploaded files, exports, and QR codes.

### 7. Privacy & Compliance

- **ConsentService:** Requests and logs respondent consent.
- **GDPRService:** Data retention, deletion, and download endpoints.

***

## Architectural Patterns

- **Frontend:** Component-based, modular design leveraging React, Redux, and Tailwind for isolation and scalability.[3]
- **Backend:** Layered and microservices-inspired structure, with Secure REST API boundaries and clear service granularity.[1][3]
- **Integration:** Well-defined API contracts (OpenAPI/Swagger), webhook support for notifications.

***

This HLD provides scalable, maintainable separation for both frontend and backend, directly mapping implementation to robust business requirements.[3][1]


[1](https://www.simform.com/blog/software-architecture-patterns/)
[2](https://microservices.io/patterns/microservices.html)
[3](https://www.geeksforgeeks.org/system-design/design-patterns-architecture/)
[4](https://zerotomastery.io/blog/software-architecture-design-patterns/)
[5](https://stackoverflow.com/questions/13800695/architecture-of-a-mobile-survey-app)
[6](https://www.sciencedirect.com/science/article/pii/S187705091503183X)
[7](https://dl.acm.org/doi/10.1145/2926966)
[8](https://nexla.com/data-integration-101/data-integration-architecture/)
