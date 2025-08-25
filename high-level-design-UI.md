# HLD

## Frontend HLD

**Folder:** /frontend

### 1. Admin Dashboard

- **AdminLayout:** Primary shell for the admin interface; contains navigation and settings.
- **Dashboard/:**
  - SurveyOverview: Card/grid of survey stats.
  - RecentActivity: Activity feed (latest actions/edits).
  - QuickStats: KPIs such as open surveys, active responses.
- **SurveyManagement/:**
  - SurveyList: List, filter, paginate surveys.
  - SurveyBuilder: Drag-and-drop, multi-step wizard for survey creation.
  - QuestionEditor: Conditional/branching logic implementation, validation UI.
  - SurveySettings: Configuration forms for settings, expiry, access, etc.
- **Analytics/:**
  - ResponseAnalytics: Data panels, filter controls, summary stats.
  - LocationMap: Map-based visualization (Leaflet.js) for geographic analytics.
  - TimeBasedCharts: Recharts/Chart.js, histogram & timeline views.
  - ExportManager: Manage and download exported reports.
- **Settings/:**
  - UserManagement: User list & role management, invitations, permissions.
  - SystemSettings: Global configuration options.
- **Common Components:**
  - Notification system (Toast/Snackbar)
  - Modal dialogs (Confirm, Edit, etc.)
  - Form logic, validation hooks

### 2. Survey Taking

- **SurveyInterface/:**
  - SurveyLoader: Loads the survey by unique URL, handles expired/invalid state.
  - QuestionRenderer: Dynamic per-question UI, input, file upload, types.
  - ProgressTracker: Visual progress bar, per-page/postal indicators.
  - LocationHandler: Permission dialog, geolocation (GPS/IP), fallback, privacy messaging.
  - SubmissionConfirmation: Success screen, optional further links.

*Integration:*  

- Typescript interfaces for all survey/question/response/location models.
- Tailwind for styling, Headless UI for accessible widgets.
- Redux Toolkit & RTK Query for API state/data caching.
- Day.js for time, timezone normalization.

***


[1](https://www.simform.com/blog/software-architecture-patterns/)
[2](https://microservices.io/patterns/microservices.html)
[3](https://www.geeksforgeeks.org/system-design/design-patterns-architecture/)
[4](https://zerotomastery.io/blog/software-architecture-design-patterns/)
[5](https://stackoverflow.com/questions/13800695/architecture-of-a-mobile-survey-app)
[6](https://www.sciencedirect.com/science/article/pii/S187705091503183X)
[7](https://dl.acm.org/doi/10.1145/2926966)
[8](https://nexla.com/data-integration-101/data-integration-architecture/)
