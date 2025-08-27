### Agent Command: Implement Survey Response Module

implement all ofbelow in backend/survery-response folder

I will generate the required Java classes for the Survey Response module. The implementation will include a REST API endpoint, data validation, and persistence logic using Spring Data JPA. I will use the established design patterns and technologies you've outlined, such as `@RestController`, `@Service`, and `JpaRepository`.

The command will create the following:

1. **Entities**: `SurveyResponseEntity`, `QuestionAnswerEntity`, and `LocationDataEntity` to map to the database tables.
2. **Repositories**: Interfaces (`SurveyResponseRepository`, `QuestionAnswerRepository`, `SurveyRepository`, `QuestionRepository`) to manage database interactions.
3. **Data Transfer Objects (DTOs)**: `SurveyResponseRequest` and `QuestionAnswerRequest` to handle the incoming JSON payload.
4. **Service Layer**: A `SurveyResponseService` class that contains the core business logic for processing and persisting a response.
5. **Controller**: A `SurveyResponseController` class that exposes the public-facing REST endpoint for survey submissions.

I'll also include a simple, asynchronous event mechanism using Spring's `ApplicationEventPublisher` to decouple the response intake from the analytics processing, as mentioned in your original HLD. This will ensure that the high-throughput response endpoint is not blocked by downstream tasks.

Here is the implementation plan:

1. **Generate `SurveyResponseEntity.java` and `QuestionAnswerEntity.java`**:
    * Create JPA `@Entity` classes with `@Id` and `@GeneratedValue` annotations.
    * Map the fields to the database columns, including `@OneToMany` and `@ManyToOne` relationships where applicable.
2. **Generate `SurveyResponseRequest.java` and `QuestionAnswerRequest.java`**:
    * Create plain POJO classes to act as DTOs for the API request body.
    * Include `@NotNull` or other validation annotations from the `jakarta.validation` package.
3. **Generate Repository Interfaces**:
    * Create interfaces that extend `JpaRepository<Entity, IdType>`.
    * I'll use `QuestionAnswerRepository` with the `saveAll()` method to handle the bulk insertion of answers.
4. **Generate `SurveyResponseService.java`**:
    * Annotate with `@Service`.
    * Use `@Autowired` for dependency injection of the repositories.
    * Implement the `processSubmission` method with `@Transactional` to ensure data integrity during the save operation.
    * Inside the service, after a successful save, use `ApplicationEventPublisher.publishEvent()` to send a `SurveyResponseSubmittedEvent`.
5. **Generate `SurveyResponseController.java`**:
    * Annotate with `@RestController` and `@RequestMapping`.
    * Implement the `POST /api/v1/surveys/{linkId}/submit` endpoint.
    * Inject the `SurveyResponseService` and call its `processSubmission` method.
    * Wrap the response in a `ResponseEntity` to return the appropriate HTTP status code.
6. **Create an Event Class and Listener (Optional but Recommended)**:
    * Create a simple POJO `SurveyResponseSubmittedEvent` to carry the necessary data (e.g., `responseId`).
    * Create a separate `@Component` or `@Service` class that has a method annotated with `@EventListener` to listen for this event. This will be a placeholder for the future Analytics module.

This structured approach ensures that the code is clean, modular, and adheres to the principles of a microservices architecture.
