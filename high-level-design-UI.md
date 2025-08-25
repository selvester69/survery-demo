

# **Frontend High-Level Design (UI HLD)**

The frontend is comprised of two distinct **React.js applications**, each optimized for its specific user base. Both applications will share common patterns, libraries, and design principles to ensure a consistent and maintainable codebase.

##

---

### **1. Core Frontend Architecture**

* **Technology Stack:**
  * **Framework:** React.js (v18+) with TypeScript for enhanced type safety and developer productivity.
  * **Styling:** **Tailwind CSS** for utility-first, rapid UI development.
  * **Component Library:** **Headless UI** for accessible, unstyled components (e.g., dropdowns, dialogs), allowing for full design customization with Tailwind.
  * **State Management:** **Redux Toolkit** combined with **RTK Query** will serve as the single source of truth for API data. RTK Query will handle data fetching, caching, and state updates, drastically reducing boilerplate code.
  * **Routing:** **React Router** for declarative navigation within each application.

* **Data Models:** All data models (e.g., `Survey`, `Question`, `Response`, `LocationData`) will be defined using TypeScript interfaces to ensure consistency across the application and API calls.

---

### **2. Admin Dashboard (`/admin`)**

This application is a secure, authenticated, and feature-rich portal for managing surveys and viewing analytics.

* **Structure & Routing:**
  * **`AdminLayout` Component:** This is the root component for the authenticated user. It contains the persistent header, sidebar navigation, and a main content area for rendering different views based on the current route.
  * **Views & Routes:**
    * `/admin/dashboard` ➡️ `Dashboard/SurveyOverview`: The landing page with quick stats and recent activity.
    * `/admin/surveys` ➡️ `SurveyManagement/SurveyList`: A table-based view with filtering, sorting, and pagination.
    * `/admin/surveys/new` ➡️ `SurveyManagement/SurveyBuilder`: A multi-step wizard component.
    * `/admin/analytics/:surveyId` ➡️ `Analytics/ResponseAnalytics`: The main analytics view for a specific survey.
    * `/admin/users` ➡️ `Settings/UserManagement`: A view for user management and permissions.

* **Key Components & Logic:**
  * **`SurveyBuilder`:** A complex wizard component. Each step will be a separate sub-component (e.g., `QuestionEditor`, `SurveySettingsForm`). It will use a local state (`useState` or `useReducer`) to manage the unsaved survey configuration and submit the final payload to the backend via RTK Query's mutations.
  * **`QuestionEditor`:** Renders different UI forms based on the selected question type (e.g., text input, checkboxes, rating scale). This component will also contain a conditional logic builder, which will use a simple form to configure and update the `question_config` JSONB field.
  * **`Analytics` Components:**
    * `ResponseAnalytics`: Orchestrates the fetching of different data sets (e.g., summary stats, trends) using multiple RTK Query hooks.
    * `LocationMap`: Renders a map using **Leaflet.js** and places markers or a heat map layer based on a geoJSON data object fetched from the backend.
    * `TimeBasedCharts`: Renders charts using **Recharts** based on data provided by the parent component or fetched via RTK Query.

---

### **3. Survey Taking Interface (`/s/:linkId`)**

This application is lightweight, unauthenticated, and focused on a single task: rendering and submitting a survey.

* **Structure & Logic:**
  * **`SurveyLoader` Component:** This is the entry point. It takes the `linkId` from the URL and uses RTK Query to fetch the survey configuration from the backend. It handles different states, such as `loading`, `invalidLink`, `expired`, or `passwordProtected`.
  * **`QuestionRenderer` Component:** A dynamic component that takes a `question` object as a prop and renders the appropriate UI for each question type (e.g., a `<input>` for text, a series of `<button>`s for multiple choice). It will be responsible for handling user input and updating the local state.
  * **`ProgressTracker`:** A simple component that visualizes the user's progress through the survey, based on the total number of questions and the current question index.
  * **`LocationHandler`:** A crucial component for location tracking.
    * It will check if the browser supports geolocation.
    * It will display a privacy-focused permission message to the user before making the request.
    * If the user grants permission, it calls the browser's `navigator.geolocation.getCurrentPosition()`.
    * If permission is denied or geolocation fails, it will send the user's IP address to the backend for a fallback IP-based location lookup.

* **State Management:** Due to its linear nature, this application can rely on a simpler state management approach, such as React's `useReducer` to manage the survey's progress and the accumulated responses.

---

### **4. Key Integrations & Dependencies**

* **RTK Query:** All API calls, for both applications, will be managed by RTK Query. This includes fetching survey lists (`useGetSurveysQuery`), creating a new survey (`useCreateSurveyMutation`), and submitting responses (`useSubmitResponseMutation`). It provides automatic data caching and background re-fetching, which is essential for a good user experience on the admin dashboard.
* **Day.js:** Used for all date and time-based operations, including timezone normalization for responses, handling expiry dates, and formatting timestamps for display on the analytics dashboard.
* **Form Libraries:** A library like **React Hook Form** can be used with schema validation (e.g., **Zod**) to handle form state and validation efficiently, especially within the complex `SurveyBuilder` and `QuestionEditor` components.
