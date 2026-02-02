based on below plan analyse and wait for instructions Based on the comprehensive documentation provided, here's a structured agent plan to create the UI for the Survey Application:

Phase 1: Project Foundation & Setup
Agent 1: Infrastructure Setup
Initialize monorepo structure with Lerna/Nx workspace
Configure Vite build system for both applications
Set up TypeScript configuration with strict settings
Implement ESLint, Prettier, and Husky pre-commit hooks
Configure Tailwind CSS with custom design tokens
Set up testing infrastructure (Vitest, React Testing Library, Playwright)
Agent 2: Shared Package Development
Create @survey-app/ui-components package with base components (Button, Input, Modal, etc.)
Develop @survey-app/shared-types with TypeScript interfaces
Build @survey-app/api-client with RTK Query integration
Implement @survey-app/design-tokens with Tailwind configuration
Create @survey-app/shared-utils with common helpers and validators

Phase 2: Admin Dashboard Development
Agent 3: Authentication & Layout
Implement authentication components (LoginForm, ProtectedRoute)
Create AdminLayout with header, sidebar navigation, and breadcrumbs
Build user profile management interface
Implement JWT token handling and refresh logic
Design responsive navigation with mobile drawer
Agent 4: Survey Management Core
Develop SurveyList with filtering, sorting, and pagination
Create SurveyCard components with status indicators
Build survey creation wizard with step navigation
Implement survey settings and configuration panels
Add survey duplication and template functionality
Agent 5: Survey Builder Interface
Design drag-and-drop question palette
Create QuestionEditor with type-specific configurations
Implement conditional logic builder interface
Build real-time survey preview functionality
Add question reordering with drag-and-drop
Create question validation and error handling
Agent 6: Question Type Components
TextQuestion with validation rules
MultipleChoiceQuestion with dynamic options
RatingQuestion with customizable scales
EmailQuestion with format validation
NumberQuestion with range constraints
DateQuestion with calendar picker
FileUploadQuestion component
Agent 7: Link Management System
Build LinkManager with URL generation interface
Create QRCodeGenerator with styling options
Implement BulkLinkGenerator for campaign management
Design LinkAnalytics dashboard with click tracking
Add custom domain configuration interface
Create link expiration and access controls

Phase 3: Analytics & Reporting
Agent 8: Analytics Dashboard
Develop real-time metrics display with StatCard components
Create ResponseAnalytics with trend visualization
Build geographic data visualization with heat maps
Implement time-based charts with date range selection
Add response filtering and segmentation tools
Agent 9: Data Visualization
Integrate Recharts for survey response analytics
Create LocationHeatmap using Leaflet.js
Build demographic breakdown charts
Implement response trend analysis graphs
Design export functionality with multiple formats
Agent 10: Reporting System
Create ResponseDataTable with advanced filtering
Build custom report builder interface
Implement scheduled report generation
Add data export in Excel, CSV, and PDF formats
Create report templates and saved configurations

Phase 4: Survey Taking Interface
Agent 11: Survey Runtime Engine
Build lightweight SurveyContainer for respondents
Create QuestionRenderer with dynamic question loading
Implement ProgressTracker with completion indicators
Add NavigationControls with conditional logic support
Build auto-save functionality with local storage
Agent 12: Response Collection
Develop form validation with real-time feedback
Create response submission with error handling
Implement offline capability with sync functionality
Build completion flow with thank you messages
Add survey expiration and access control checks
Agent 13: Location & Device Tracking
Create LocationConsent component with privacy messaging
Implement geolocation capture with fallback to IP
Build device information collection
Add timezone and browser detection
Create privacy controls and opt-out mechanisms

Phase 5: Advanced Features & Polish
Agent 14: Mobile Optimization
Implement responsive design across all components
Optimize touch interactions for mobile devices
Add progressive web app functionality
Create mobile-specific navigation patterns
Test and optimize for various screen sizes
Agent 15: Accessibility Implementation
Add ARIA labels and semantic HTML throughout
Implement keyboard navigation support
Create screen reader optimized components
Add high contrast mode support
Conduct accessibility testing and remediation
Agent 16: Performance Optimization
Implement code splitting and lazy loading
Add image optimization and compression
Create efficient caching strategies
Optimize bundle sizes and loading performance
Add performance monitoring and metrics

Phase 6: Integration & Testing
Agent 17: API Integration
Connect all components to backend APIs
Implement error handling and loading states
Add retry logic and offline handling
Create API response caching strategies
Test API integration thoroughly
Agent 18: End-to-End Testing
Write comprehensive Playwright test suites
Create user journey testing scenarios
Implement visual regression testing
Add performance testing automation
Create accessibility testing automation
Agent 19: Documentation & Deployment
Create component documentation with Storybook
Write user guides and admin documentation
Set up CI/CD pipelines for both applications
Configure environment-specific deployments
Create monitoring and error tracking setup

Phase 7: Launch Preparation
Agent 20: Quality Assurance
Conduct comprehensive cross-browser testing
Perform security testing and vulnerability assessment
Execute load testing for high-traffic scenarios
Review and optimize SEO considerations
Complete final accessibility audit
This agent plan ensures systematic development of both applications while maintaining code quality, performance, and user experience standards. Each agent focuses on specific functionality areas, allowing for parallel development and clear responsibility boundaries.
