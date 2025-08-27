# Backend HLD - Class Diagrams and Entity Relationship Diagrams

## 1. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    ADMIN_USERS {
        uuid id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar role
        timestamp created_at
        timestamp last_login
        boolean is_active
    }
    
    SURVEYS {
        uuid id PK
        varchar title
        text description
        text welcome_message
        text thank_you_message
        jsonb settings
        uuid created_by FK
        timestamp created_at
        timestamp updated_at
        varchar status
        timestamp expires_at
        boolean collect_location
        boolean require_location
    }
    
    QUESTIONS {
        uuid id PK
        uuid survey_id FK
        varchar question_type
        text question_text
        jsonb question_config
        jsonb validation_rules
        jsonb conditional_logic
        int order_index
        boolean is_required
        timestamp created_at
    }
    
    SURVEY_LINKS {
        uuid id PK
        uuid survey_id FK
        varchar short_id UK
        varchar tiny_url
        varchar custom_domain
        timestamp created_at
        timestamp expires_at
        int clicks
        int responses
        boolean is_active
    }
    
    RESPONSES {
        uuid id PK
        uuid survey_id FK
        uuid link_id FK
        varchar respondent_id
        jsonb location_data
        jsonb device_info
        timestamp started_at
        timestamp completed_at
        varchar status
        varchar user_agent
        varchar ip_address
    }
    
    ANSWERS {
        uuid id PK
        uuid response_id FK
        uuid question_id FK
        jsonb answer_value
        text answer_text
        timestamp created_at
    }
    
    LINK_ANALYTICS {
        uuid id PK
        uuid link_id FK
        timestamp click_timestamp
        varchar referrer
        varchar user_agent
        varchar ip_address
        jsonb location_data
        varchar session_id
    }
    
    URL_CAMPAIGNS {
        uuid id PK
        uuid survey_id FK
        varchar campaign_name
        int links_generated
        int total_clicks
        float conversion_rate
        timestamp created_at
        timestamp updated_at
    }
    
    ANALYTICS_CACHE {
        uuid id PK
        uuid survey_id FK
        varchar metric_type
        jsonb metric_data
        timestamp calculated_at
        timestamp expires_at
    }
    
    PRIVACY_CONSENTS {
        uuid id PK
        uuid response_id FK
        varchar consent_type
        varchar status
        timestamp granted_at
        jsonb consent_details
    }

    %% Relationships
    ADMIN_USERS ||--o{ SURVEYS : creates
    SURVEYS ||--o{ QUESTIONS : contains
    SURVEYS ||--o{ SURVEY_LINKS : "has links"
    SURVEYS ||--o{ RESPONSES : "receives responses"
    SURVEYS ||--o{ URL_CAMPAIGNS : "organized by"
    SURVEYS ||--o{ ANALYTICS_CACHE : "has metrics"
    
    SURVEY_LINKS ||--o{ RESPONSES : "tracked by"
    SURVEY_LINKS ||--o{ LINK_ANALYTICS : "generates"
    
    RESPONSES ||--o{ ANSWERS : "contains"
    RESPONSES ||--o{ PRIVACY_CONSENTS : "has consents"
    
    QUESTIONS ||--o{ ANSWERS : "answered by"
    
    URL_CAMPAIGNS ||--o{ SURVEY_LINKS : "manages"
```

## 2. Survey Management Module - Class Diagram

```mermaid
classDiagram
    class SurveyController {
        +createSurvey(SurveyCreateDTO) ResponseEntity
        +getSurvey(UUID) ResponseEntity
        +updateSurvey(UUID, SurveyUpdateDTO) ResponseEntity
        +deleteSurvey(UUID) ResponseEntity
        +getAllSurveys(Pageable) ResponseEntity
    }
    
    class SurveyService {
        -surveyRepository SurveyRepository
        -questionService QuestionService
        -linkService LinkService
        +createSurvey(SurveyCreateDTO) SurveyEntity
        +updateSurvey(UUID, SurveyUpdateDTO) SurveyEntity
        +getSurveyById(UUID) SurveyEntity
        +deleteSurvey(UUID) void
        +validateSurveyAccess(UUID, String) boolean
        -publishSurveyCreatedEvent(SurveyEntity) void
    }
    
    class QuestionService {
        -questionRepository QuestionRepository
        +createQuestion(QuestionCreateDTO) QuestionEntity
        +updateQuestion(UUID, QuestionUpdateDTO) QuestionEntity
        +deleteQuestion(UUID) void
        +getQuestionsBySurveyId(UUID) List~QuestionEntity~
        +validateQuestionConfig(QuestionType, JsonNode) boolean
        +reorderQuestions(UUID, List~UUID~) void
    }
    
    class SurveyRepository {
        <<interface>>
        +findById(UUID) Optional~SurveyEntity~
        +findByCreatedBy(UUID) List~SurveyEntity~
        +findActiveByExpiresAtAfter(LocalDateTime) List~SurveyEntity~
        +save(SurveyEntity) SurveyEntity
        +deleteById(UUID) void
    }
    
    class QuestionRepository {
        <<interface>>
        +findBySurveyIdOrderByOrderIndex(UUID) List~QuestionEntity~
        +findById(UUID) Optional~QuestionEntity~
        +save(QuestionEntity) QuestionEntity
        +deleteById(UUID) void
        +countBySurveyId(UUID) int
    }
    
    class SurveyEntity {
        -UUID id
        -String title
        -String description
        -String welcomeMessage
        -String thankYouMessage
        -JsonNode settings
        -UUID createdBy
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -SurveyStatus status
        -LocalDateTime expiresAt
        -Boolean collectLocation
        -Boolean requireLocation
        -List~QuestionEntity~ questions
        -List~SurveyLinkEntity~ links
        -List~ResponseEntity~ responses
        +isActive() boolean
        +isExpired() boolean
    }
    
    class QuestionEntity {
        -UUID id
        -UUID surveyId
        -QuestionType questionType
        -String questionText
        -JsonNode questionConfig
        -JsonNode validationRules
        -JsonNode conditionalLogic
        -Integer orderIndex
        -Boolean isRequired
        -LocalDateTime createdAt
        -SurveyEntity survey
        -List~AnswerEntity~ answers
        +validate(JsonNode) ValidationResult
    }
    
    class SurveyCreateDTO {
        +String title
        +String description
        +String welcomeMessage
        +List~QuestionCreateDTO~ questions
        +JsonNode settings
        +LocalDateTime expiresAt
    }
    
    class QuestionCreateDTO {
        +QuestionType questionType
        +String questionText
        +JsonNode questionConfig
        +JsonNode validationRules
        +Integer orderIndex
        +Boolean isRequired
    }

    %% Relationships
    SurveyController --> SurveyService : uses
    SurveyService --> SurveyRepository : uses
    SurveyService --> QuestionService : uses
    QuestionService --> QuestionRepository : uses
    SurveyService --> SurveyEntity : creates/manages
    QuestionService --> QuestionEntity : creates/manages
    SurveyController --> SurveyCreateDTO : receives
    SurveyController --> QuestionCreateDTO : receives
    SurveyEntity "1" -- "0..*" QuestionEntity : contains
```

## 3. Authentication & User Management Module - Class Diagram

```mermaid
classDiagram
    class AuthController {
        +login(LoginRequest) ResponseEntity~LoginResponse~
        +getCurrentUser() ResponseEntity~UserProfileDTO~
        +refreshToken(RefreshTokenRequest) ResponseEntity~TokenResponse~
        +logout() ResponseEntity~Void~
    }
    
    class AuthService {
        -userRepository UserRepository
        -passwordEncoder PasswordEncoder
        -jwtService JwtService
        +authenticate(String, String) UserEntity
        +generateTokens(UserEntity) TokenResponse
        +refreshAccessToken(String) TokenResponse
        +validateToken(String) boolean
        +getCurrentUser(String) UserEntity
    }
    
    class JwtService {
        -String secretKey
        -int accessTokenExpiry
        -int refreshTokenExpiry
        +generateAccessToken(UserEntity) String
        +generateRefreshToken(UserEntity) String
        +extractUsername(String) String
        +validateToken(String, UserDetails) boolean
        +isTokenExpired(String) boolean
    }
    
    class UserService {
        -userRepository UserRepository
        -passwordEncoder PasswordEncoder
        +createUser(UserCreateDTO) UserEntity
        +updateUser(UUID, UserUpdateDTO) UserEntity
        +getUserById(UUID) UserEntity
        +deleteUser(UUID) void
        +changePassword(UUID, String, String) void
    }
    
    class UserRepository {
        <<interface>>
        +findByUsername(String) Optional~UserEntity~
        +findByEmail(String) Optional~UserEntity~
        +existsByUsername(String) boolean
        +existsByEmail(String) boolean
        +save(UserEntity) UserEntity
    }
    
    class UserEntity {
        -UUID id
        -String username
        -String email
        -String passwordHash
        -UserRole role
        -LocalDateTime createdAt
        -LocalDateTime lastLogin
        -Boolean isActive
        -List~SurveyEntity~ createdSurveys
        +isAccountNonExpired() boolean
        +isAccountNonLocked() boolean
        +isCredentialsNonExpired() boolean
        +isEnabled() boolean
    }
    
    class LoginRequest {
        +String username
        +String password
    }
    
    class LoginResponse {
        +String accessToken
        +String refreshToken
        +UserProfileDTO user
        +long expiresIn
    }
    
    class UserProfileDTO {
        +UUID id
        +String username
        +String email
        +UserRole role
        +LocalDateTime lastLogin
    }

    %% Relationships
    AuthController --> AuthService : uses
    AuthController --> UserService : uses
    AuthService --> UserRepository : uses
    AuthService --> JwtService : uses
    UserService --> UserRepository : uses
    AuthService --> UserEntity : manages
    UserService --> UserEntity : manages
    AuthController --> LoginRequest : receives
    AuthController --> LoginResponse : returns
```

## 4. Response Intake & Metadata Module - Class Diagram

```mermaid
classDiagram
    class ResponseController {
        +submitResponse(String, ResponseSubmissionDTO) ResponseEntity~SubmissionResult~
        +getSurveyByLink(String) ResponseEntity~PublicSurveyDTO~
        +validateSurveyLink(String) ResponseEntity~LinkValidationResult~
    }
    
    class ResponseService {
        -responseRepository ResponseRepository
        -answerRepository AnswerRepository
        -surveyLinkService SurveyLinkService
        -locationService LocationService
        -deviceInfoService DeviceInfoService
        -applicationEventPublisher ApplicationEventPublisher
        +submitResponse(String, ResponseSubmissionDTO) ResponseEntity
        +validateResponse(ResponseSubmissionDTO, SurveyEntity) ValidationResult
        -saveResponseWithAnswers(ResponseEntity, List~AnswerEntity~) ResponseEntity
        -publishResponseSubmittedEvent(ResponseEntity) void
    }
    
    class LocationService {
        -ipGeolocationClient IpGeolocationClient
        +resolveLocationFromIP(String) LocationData
        +validateGPSCoordinates(Double, Double) boolean
        +enrichLocationData(LocationData) LocationData
    }
    
    class DeviceInfoService {
        -userAgentParser UserAgentParser
        +parseDeviceInfo(String) DeviceInfo
        +extractBrowserInfo(String) BrowserInfo
        +detectPlatform(String) PlatformInfo
    }
    
    class ResponseEntity {
        -UUID id
        -UUID surveyId
        -UUID linkId
        -String respondentId
        -JsonNode locationData
        -JsonNode deviceInfo
        -LocalDateTime startedAt
        -LocalDateTime completedAt
        -ResponseStatus status
        -String userAgent
        -String ipAddress
        -List~AnswerEntity~ answers
        +getDurationMinutes() Long
        +isCompleted() boolean
    }
    
    class AnswerEntity {
        -UUID id
        -UUID responseId
        -UUID questionId
        -JsonNode answerValue
        -String answerText
        -LocalDateTime createdAt
        -ResponseEntity response
        -QuestionEntity question
    }
    
    class ResponseSubmissionDTO {
        +List~AnswerDTO~ answers
        +LocationDataDTO location
        +DeviceInfoDTO deviceInfo
        +String respondentId
    }

    %% Relationships
    ResponseController --> ResponseService : uses
    ResponseService --> LocationService : uses
    ResponseService --> DeviceInfoService : uses
    ResponseService --> ResponseEntity : creates
    ResponseService --> AnswerEntity : creates
```

## 5. Analytics & Reporting Module - Class Diagram

```mermaid
classDiagram
    class AnalyticsController {
        +getSurveySummary(UUID) ResponseEntity~SurveySummaryDTO~
        +getResponseAnalytics(UUID, Pageable) ResponseEntity~Page~ResponseAnalyticsDTO~~
        +getTrendChartData(UUID, String) ResponseEntity~List~TrendDataPoint~~
        +getLocationHeatMapData(UUID) ResponseEntity~List~LocationDataPoint~~
        +getQuestionAnalytics(UUID) ResponseEntity~List~QuestionAnalyticsDTO~~
        +exportSurveyData(UUID, ExportRequestDTO) ResponseEntity~ExportJobDTO~
        +getExportJobStatus(UUID) ResponseEntity~ExportJobStatusDTO~
    }
    
    class AnalyticsService {
        -analyticsRepository AnalyticsRepository
        -responseRepository ResponseRepository
        -analyticsCacheRepository AnalyticsCacheRepository
        -exportService ExportService
        -applicationEventPublisher ApplicationEventPublisher
        +calculateSurveySummary(UUID) SurveySummaryDTO
        +getResponseTrends(UUID, String) List~TrendDataPoint~
        +generateLocationHeatMap(UUID) List~LocationDataPoint~
        +analyzeQuestionResponses(UUID) List~QuestionAnalyticsDTO~
        +refreshAnalyticsCache(UUID) void
        -getCachedAnalytics(UUID, String) Optional~JsonNode~
        -setCachedAnalytics(UUID, String, JsonNode) void
    }
    
    class ExportService {
        -exportJobRepository ExportJobRepository
        -responseRepository ResponseRepository
        -excelExportProcessor ExcelExportProcessor
        -csvExportProcessor CsvExportProcessor
        -pdfExportProcessor PdfExportProcessor
        -s3Service S3Service
        +createExportJob(UUID, ExportFormat) ExportJobEntity
        +processExportJob(UUID) void
        +getExportJobStatus(UUID) ExportJobStatusDTO
        -generateExcelReport(List~ResponseEntity~) byte[]
        -generateCsvReport(List~ResponseEntity~) byte[]
        -uploadToStorage(byte[], String) String
    }
    
    class AnalyticsEventListener {
        -analyticsService AnalyticsService
        +handleSurveyResponseSubmitted(SurveyResponseSubmittedEvent) void
        +handleSurveyLinkClicked(SurveyLinkClickedEvent) void
        +updateRealTimeMetrics(UUID) void
        -invalidateAnalyticsCache(UUID) void
    }
    
    class AnalyticsRepository {
        <<interface>>
        +countResponsesBySurveyId(UUID) long
        +findCompletionRateBySurveyId(UUID) double
        +findResponseTrendsBySurveyId(UUID, LocalDateTime, LocalDateTime) List~Object[]~
        +findLocationDistributionBySurveyId(UUID) List~Object[]~
        +findAverageCompletionTimeBySurveyId(UUID) Double
        +findTopReferrersBySurveyId(UUID) List~Object[]~
    }
    
    class AnalyticsCacheRepository {
        <<interface>>
        +findBySurveyIdAndMetricType(UUID, String) Optional~AnalyticsCacheEntity~
        +save(AnalyticsCacheEntity) AnalyticsCacheEntity
        +deleteBySurveyId(UUID) void
        +deleteExpiredCache() void
    }
    
    class AnalyticsCacheEntity {
        -UUID id
        -UUID surveyId
        -String metricType
        -JsonNode metricData
        -LocalDateTime calculatedAt
        -LocalDateTime expiresAt
        +isExpired() boolean
        +refresh(JsonNode) void
    }
    
    class ExportJobEntity {
        -UUID id
        -UUID surveyId
        -UUID requestedBy
        -ExportFormat format
        -ExportStatus status
        -String fileName
        -String downloadUrl
        -String errorMessage
        -LocalDateTime createdAt
        -LocalDateTime completedAt
        -Long totalRecords
        +isCompleted() boolean
        +isFailed() boolean
    }
    
    class SurveySummaryDTO {
        +long totalResponses
        +double completionRate
        +Double averageCompletionTime
        +LocalDateTime lastResponseAt
        +Map~String, Object~ demographics
        +List~QuestionSummaryDTO~ questionSummaries
    }
    
    class TrendDataPoint {
        +LocalDate date
        +long responseCount
        +long completionCount
        +double completionRate
    }
    
    class LocationDataPoint {
        +String city
        +String state
        +String country
        +Double latitude
        +Double longitude
        +long responseCount
        +double percentage
    }

    %% Relationships
    AnalyticsController --> AnalyticsService : uses
    AnalyticsController --> ExportService : uses
    AnalyticsService --> AnalyticsRepository : uses
    AnalyticsService --> AnalyticsCacheRepository : uses
    ExportService --> ExportJobEntity : creates
    AnalyticsEventListener --> AnalyticsService : uses
    AnalyticsService --> AnalyticsCacheEntity : manages
```

## 6. Survey Link Management & Tiny URL Module - Class Diagram

```mermaid
classDiagram
    class LinkController {
        +createSurveyLink(UUID, LinkCreateDTO) ResponseEntity~SurveyLinkDTO~
        +generateBulkLinks(UUID, BulkLinkRequestDTO) ResponseEntity~List~SurveyLinkDTO~~
        +getSurveyByLink(String) ResponseEntity~PublicSurveyDTO~
        +getLinkAnalytics(UUID) ResponseEntity~LinkAnalyticsDTO~
        +generateQRCode(UUID) ResponseEntity~byte[]~
        +updateLinkSettings(UUID, LinkSettingsDTO) ResponseEntity~SurveyLinkDTO~
        +deactivateLink(UUID) ResponseEntity~Void~
    }
    
    class SurveyLinkService {
        -surveyLinkRepository SurveyLinkRepository
        -linkAnalyticsRepository LinkAnalyticsRepository
        -urlShortenerService UrlShortenerService
        -qrCodeService QRCodeService
        -applicationEventPublisher ApplicationEventPublisher
        +createSurveyLink(UUID, LinkCreateDTO) SurveyLinkEntity
        +generateBulkLinks(UUID, int, String) List~SurveyLinkEntity~
        +validateAndRedirect(String) RedirectResult
        +trackLinkClick(String, HttpServletRequest) void
        +getLinkAnalytics(UUID) LinkAnalyticsDTO
        +isLinkActive(String) boolean
        -generateUniqueShortId() String
    }
    
    class UrlShortenerService {
        -customDomainConfig Map~String, String~
        +generateTinyUrl(String, String) String
        +generateShortId() String
        +validateCustomDomain(String) boolean
        +buildFullTinyUrl(String, String) String
        -generateRandomString(int) String
    }
    
    class QRCodeService {
        +generateQRCode(String, int, int) byte[]
        +generateQRCodeWithLogo(String, byte[]) byte[]
        +validateQRCodeSize(int, int) boolean
        -createQRCodeMatrix(String, int) BitMatrix
    }
    
    class LinkAnalyticsService {
        -linkAnalyticsRepository LinkAnalyticsRepository
        -geoIpService GeoIpService
        +recordLinkClick(UUID, String, String, String) LinkAnalyticsEntity
        +getClickAnalytics(UUID, LocalDateTime, LocalDateTime) ClickAnalyticsDTO
        +getGeographicDistribution(UUID) List~GeographicClickData~
        +getReferrerAnalysis(UUID) List~ReferrerData~
        +getDeviceAnalytics(UUID) DeviceAnalyticsDTO
    }
    
    class SurveyLinkEntity {
        -UUID id
        -UUID surveyId
        -String shortId
        -String tinyUrl
        -String customDomain
        -LocalDateTime createdAt
        -LocalDateTime expiresAt
        -Integer clicks
        -Integer responses
        -Boolean isActive
        -String campaignName
        -SurveyEntity survey
        -List~LinkAnalyticsEntity~ analytics
        -List~ResponseEntity~ responses
        +isExpired() boolean
        +incrementClicks() void
        +getConversionRate() double
    }
    
    class LinkAnalyticsEntity {
        -UUID id
        -UUID linkId
        -LocalDateTime clickTimestamp
        -String referrer
        -String userAgent
        -String ipAddress
        -JsonNode locationData
        -String sessionId
        -String deviceType
        -String browserType
        -SurveyLinkEntity surveyLink
    }
    
    class URLCampaignEntity {
        -UUID id
        -UUID surveyId
        -String campaignName
        -String description
        -Integer linksGenerated
        -Integer totalClicks
        -Float conversionRate
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -List~SurveyLinkEntity~ links
        +calculateConversionRate() float
        +updateMetrics() void
    }

    %% Relationships
    LinkController --> SurveyLinkService : uses
    LinkController --> QRCodeService : uses
    SurveyLinkService --> UrlShortenerService : uses
    SurveyLinkService --> LinkAnalyticsService : uses
    LinkAnalyticsService --> LinkAnalyticsEntity : creates
    SurveyLinkService --> SurveyLinkEntity : manages
    SurveyLinkEntity "1" o-- "*" LinkAnalyticsEntity : tracks
    URLCampaignEntity "1" o-- "*" SurveyLinkEntity : organizes
```

## 7. Location & Device Services Module - Class Diagram

```mermaid
classDiagram
    class LocationService {
        -ipGeolocationClient IpGeolocationClient
        -geoLocationCache Map~String, LocationData~
        -locationValidationService LocationValidationService
        +resolveLocationFromIP(String) LocationData
        +enrichGPSLocation(Double, Double) LocationData
        +validateGPSCoordinates(Double, Double) ValidationResult
        +getCachedLocation(String) Optional~LocationData~
        +reverseGeocode(Double, Double) AddressData
        -cacheLocationData(String, LocationData) void
    }
    
    class DeviceInfoService {
        -userAgentParser UserAgentParser
        -deviceFingerprinter DeviceFingerprinter
        +parseUserAgent(String) DeviceInfo
        +extractBrowserInfo(String) BrowserInfo
        +detectOperatingSystem(String) OSInfo
        +generateDeviceFingerprint(HttpServletRequest) String
        +classifyDeviceType(String) DeviceType
        +getScreenResolution(String) ScreenInfo
    }
    
    class IpGeolocationClient {
        -restTemplate RestTemplate
        -apiKey String
        -baseUrl String
        +getLocationByIP(String) LocationApiResponse
        +validateApiResponse(LocationApiResponse) boolean
        +handleRateLimiting() void
        -buildRequestUrl(String) String
    }
    
    class GeoIpService {
        -ipGeolocationClient IpGeolocationClient
        -locationCache LocationCache
        +getLocationByIP(String) LocationData
        +isValidIPAddress(String) boolean
        +getTimezoneByLocation(Double, Double) String
        +calculateDistanceBetweenLocations(LocationData, LocationData) Double
    }
    
    class LocationData {
        -Double latitude
        -Double longitude
        -String city
        -String state
        -String country
        -String countryCode
        -String postalCode
        -String timezone
        -Integer accuracy
        -LocationSource source
        -LocalDateTime capturedAt
        +isValid() boolean
        +getFormattedAddress() String
        +toJsonNode() JsonNode
    }
    
    class DeviceInfo {
        -String deviceType
        -String browserName
        -String browserVersion
        -String operatingSystem
        -String osVersion
        -String platform
        -String userAgent
        -String screenResolution
        -String language
        -String fingerprint
        +isMobile() boolean
        +isTablet() boolean
        +isDesktop() boolean
        +toJsonNode() JsonNode
    }
    
    class BrowserInfo {
        -String name
        -String version
        -String engine
        -Boolean isMobile
        -Boolean isBot
        -List~String~ supportedFeatures
        +getDisplayName() String
        +supportsFeature(String) boolean
    }
    
    class LocationValidationService {
        +validateCoordinates(Double, Double) ValidationResult
        +validateAddress(String) ValidationResult
        +isLocationWithinBounds(LocationData, BoundingBox) boolean
        +sanitizeLocationData(LocationData) LocationData
        -isValidLatitude(Double) boolean
        -isValidLongitude(Double) boolean
    }

    %% Relationships
    LocationService --> IpGeolocationClient : uses
    LocationService --> LocationValidationService : uses
    DeviceInfoService --> UserAgentParser : uses
    GeoIpService --> IpGeolocationClient : uses
    LocationService --> LocationData : creates
    DeviceInfoService --> DeviceInfo : creates
    DeviceInfoService --> BrowserInfo : creates
```

## 8. Privacy & Compliance Module - Class Diagram

```mermaid
classDiagram
    class PrivacyController {
        +recordConsent(ConsentRequestDTO) ResponseEntity<ConsentResponseDTO>
        +updateConsent(UUID, ConsentUpdateDTO) ResponseEntity<ConsentResponseDTO>
        +getConsentHistory(UUID) ResponseEntity<List<ConsentHistoryDTO>>
        +requestDataDeletion(DataDeletionRequestDTO) ResponseEntity<DeletionJobDTO>
        +getDataDeletionStatus(UUID) ResponseEntity<DeletionStatusDTO>
        +generatePrivacyReport(UUID) ResponseEntity<PrivacyReportDTO>
    }

    class ConsentManagementService {
        -consentRepository : ConsentRepository
        -auditLogService : AuditLogService
        +recordConsent(UUID, ConsentType, ConsentStatus) ConsentEntity
        +updateConsent(UUID, ConsentStatus, String) ConsentEntity
        +getActiveConsents(UUID) List<ConsentEntity>
        +hasValidConsent(UUID, ConsentType) boolean
        +revokeAllConsents(UUID) void
        +getConsentHistory(UUID) List<ConsentEntity>
    }
    
    class DataDeletionService {
        -deletionJobRepository : DeletionJobRepository
        -responseRepository : ResponseRepository
        -analyticsRepository : AnalyticsRepository
        -auditLogService : AuditLogService
        +createDeletionRequest(DataDeletionRequestDTO) DeletionJobEntity
        +processDeletionJob(UUID) DeletionResult
        +anonymizePersonalData(UUID) void
        +hardDeleteResponse(UUID) void
        +validateDeletionRequest(UUID) ValidationResult
        -deleteRelatedData(UUID) void
    }
    
    class AuditLogService {
        -auditLogRepository : AuditLogRepository
        +logDataAccess(UUID, String, String) void
        +logConsentChange(UUID, ConsentType, ConsentStatus) void
        +logDataDeletion(UUID, String) void
        +logPrivacyViolation(String, String) void
        +generateComplianceReport(LocalDateTime, LocalDateTime) ComplianceReportDTO
    }
    
    class ConsentEntity {
        -UUID id
        -UUID responseId
        -ConsentType consentType
        -ConsentStatus status
        -String purpose
        -LocalDateTime grantedAt
        -LocalDateTime revokedAt
        -LocalDateTime expiresAt
        -JsonNode consentDetails
        -String legalBasis
        -ResponseEntity response
        +isActive() boolean
        +isExpired() boolean
        +revoke(String) void
    }
    
    class DeletionJobEntity {
        -UUID id
        -UUID responseId
        -UUID requestedBy
        -DeletionType deletionType
        -DeletionStatus status
        -String reason
        -LocalDateTime requestedAt
        -LocalDateTime processedAt
        -String processingLog
        -Boolean isCompleted
        +markCompleted() void
        +addLogEntry(String) void
    }
    
    class AuditLogEntity {
        -UUID id
        -String entityType
        -UUID entityId
        -String action
        -String userId
        -JsonNode oldValues
        -JsonNode newValues
        -LocalDateTime timestamp
        -String ipAddress
        -String userAgent
        +getChangesSummary() String
    }

    %% Represent enums as classes with stereotype
    class ConsentType {
        <<enumeration>>
        LOCATION_TRACKING
        DATA_PROCESSING
        ANALYTICS_TRACKING
        MARKETING_COMMUNICATION
        THIRD_PARTY_SHARING
    }
    
    class ConsentStatus {
        <<enumeration>>
        GRANTED
        REVOKED
        EXPIRED
        PENDING
    }
    
    class DeletionStatus {
        <<enumeration>>
        REQUESTED
        IN_PROGRESS
        COMPLETED
        FAILED
        CANCELLED
    }

    %% Relationships
    PrivacyController --> ConsentManagementService : uses
    PrivacyController --> DataDeletionService : uses
    ConsentManagementService --> ConsentEntity : manages
    DataDeletionService --> DeletionJobEntity : manages
    ConsentManagementService --> AuditLogService : uses
    DataDeletionService --> AuditLogService : uses
    ConsentEntity --> ConsentType : uses
    ConsentEntity --> ConsentStatus : uses
    DeletionJobEntity --> DeletionStatus : uses
```

## 9. Event-Driven Architecture - Class Diagram

```mermaid
classDiagram
    class ApplicationEventPublisher {
        <<interface>>
        +publishEvent(ApplicationEvent) void
    }
    
    class SurveyResponseSubmittedEvent {
        -UUID responseId
        -UUID surveyId
        -LocalDateTime timestamp
        -LocationData location
        -DeviceInfo deviceInfo
        +getResponseId() UUID
        +getSurveyId() UUID
    }
    
    class SurveyLinkClickedEvent {
        -UUID linkId
        -UUID surveyId
        -String ipAddress
        -String userAgent
        -String referrer
        -LocalDateTime timestamp
        +getLinkId() UUID
        +getSurveyId() UUID
    }
    
    class SurveyCreatedEvent {
        -UUID surveyId
        -UUID createdBy
        -LocalDateTime timestamp
        -SurveySettings settings
        +getSurveyId() UUID
        +getCreatedBy() UUID
    }
    
    class ResponseEventListener {
        -analyticsService AnalyticsService
        -notificationService NotificationService
        +handleResponseSubmitted(SurveyResponseSubmittedEvent) void
        +handleLinkClicked(SurveyLinkClickedEvent) void
        -updateRealTimeMetrics(UUID) void
        -sendNotificationIfThresholdMet(UUID) void
    }
    
    class AnalyticsEventListener {
        -analyticsService AnalyticsService
        -cacheService CacheService
        +handleResponseSubmitted(SurveyResponseSubmittedEvent) void
        +handleSurveyCreated(SurveyCreatedEvent) void
        -invalidateAnalyticsCache(UUID) void
        -initializeAnalyticsForSurvey(UUID) void
    }
    
    class NotificationEventListener {
        -emailService EmailService
        -smsService SmsService
        -userRepository UserRepository
        +handleResponseSubmitted(SurveyResponseSubmittedEvent) void
        +handleSurveyCreated(SurveyCreatedEvent) void
        -sendAdminNotification(UUID, String) void
    }

    %% Relationships
    ApplicationEventPublisher --> SurveyResponseSubmittedEvent : publishes
    ApplicationEventPublisher --> SurveyLinkClickedEvent : publishes
    ApplicationEventPublisher --> SurveyCreatedEvent : publishes
    ResponseEventListener --> AnalyticsService : uses
    AnalyticsEventListener --> AnalyticsService : uses
    NotificationEventListener --> EmailService : uses
```

## 10. Complete System Integration Overview

```mermaid
classDiagram
    class SurveyApplication {
        +main(String[]) void
    }
    
    class DatabaseConfiguration {
        +dataSource() DataSource
        +entityManagerFactory() EntityManagerFactory
        +transactionManager() PlatformTransactionManager
    }
    
    class SecurityConfiguration {
        +jwtAuthenticationEntryPoint() AuthenticationEntryPoint
        +jwtRequestFilter() JwtRequestFilter
        +passwordEncoder() PasswordEncoder
        +authenticationManager() AuthenticationManager
    }
    
    class CacheConfiguration {
        +cacheManager() CacheManager
        +redisTemplate() RedisTemplate
        +cacheResolver() CacheResolver
    }
    
    class AsyncConfiguration {
        +taskExecutor() TaskExecutor
        +applicationEventMulticaster() ApplicationEventMulticaster
    }

    %% Configuration relationships
    SurveyApplication --> DatabaseConfiguration : configures
    SurveyApplication --> SecurityConfiguration : configures
    SurveyApplication --> CacheConfiguration : configures
    SurveyApplication --> AsyncConfiguration : configures
```
