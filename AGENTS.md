# ExGym Training Program - Agent Reference Guide

## Project Overview
This is a Spring Boot REST API application for managing gym training programs, trainers, trainees, and training sessions. The application provides comprehensive RESTful endpoints for user registration, authentication, profile management, and training session coordination.

## Technology Stack
- **Framework**: Spring Boot 4.x
- **Build Tool**: Maven
- **Language**: Java
- **Database**: JPA/Hibernate with configurable datasource
- **API Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito
- **Security**: Spring Security with JWT bearer authentication, BCrypt password hashing, brute-force protection, and token revocation on logout

## Project Structure

### Root Directory Structure
```
exgym-training-program/
├── src/
│   ├── main/
│   │   ├── java/com/exgym/training/
│   │   └── resources/
│   └── test/
│       └── java/com/exgym/training/
├── target/                    # Build output and test reports
├── pom.xml                    # Maven configuration
├── README.md                  # Project readme
├── HELP.md                    # Additional help documentation
├── IMPLEMENTATION_SUMMARY.md  # Implementation details
├── REST_API_DOCUMENTATION.md  # API documentation
└── AGENTS.md                  # This file

```

### Source Code Structure (`src/main/java/com/exgym/training/`)

#### 1. **config/** - Configuration & Cross-cutting Concerns
- `GlobalExceptionHandler.java` - Centralized exception handling with transaction ID tracking
- `LoggingInterceptor.java` - HTTP request/response logging with transaction ID generation
- `OpenApiConfig.java` - Swagger/OpenAPI documentation configuration
- `SecurityConfig.java` - JWT-based Spring Security configuration with public and protected routes
- `WebConfig.java` - Spring MVC configuration, registers interceptors

#### 2. **controller/** - REST API Endpoints
- `TraineeController.java` - Trainee management endpoints
  - POST /api/v1/trainee/register
  - GET /api/v1/trainee/profile?username={username}
  - PUT /api/v1/trainee/profile
  - DELETE /api/v1/trainee/profile
  - GET /api/v1/trainee/trainers/not-assigned?username={username}
  - PUT /api/v1/trainee/trainers
  - GET /api/v1/trainee/trainings?username={username}
  - PATCH /api/v1/trainee/status

- `TrainerController.java` - Trainer management endpoints
  - POST /api/v1/trainer/register
  - GET /api/v1/trainer/{username}/profile
  - PUT /api/v1/trainer/profile
  - GET /api/v1/trainer/{username}/trainings
  - PATCH /api/v1/trainer/{username}/status?isActive={true|false}

- `UserController.java` - Authentication endpoints
  - POST /api/v1/user/login
  - PUT /api/v1/user/change-password
  - POST /api/v1/user/logout

- `TrainingController.java` - Training management endpoints
  - POST /api/v1/training
  - GET /api/v1/training/types

- `TrainingTypeController.java` - Training type management endpoints
  - POST /api/v1/training-types

#### 3. **dao/** - Data Access Layer (Spring Data JPA Repositories)
- `TraineeDao.java` - Trainee repository with username lookup
- `TrainerDao.java` - Trainer repository with username lookup & not-assigned query
- `TrainingDao.java` - Training repository with custom queries for filtering
- `TrainingTypeDao.java` - Training type repository

#### 4. **dto/** - Data Transfer Objects

**Request DTOs (domain-organized packages):**
- `TraineeRegistrationRequest.java`
- `TrainerRegistrationRequest.java`
- `LoginRequest.java`
- `ChangePasswordRequest.java`
- `GetProfileRequest.java`
- `UpdateTraineeProfileRequest.java`
- `UpdateTrainerProfileRequest.java`
- `UpdateTraineeTrainerListRequest.java`
- `GetTraineeTrainingsRequest.java`
- `GetTrainerTrainingsRequest.java`
- `AddTrainingRequest.java`
- `AddTrainingTypeRequest.java`
- `ActivateDeactivateRequest.java`

**Response DTOs (domain-organized packages):**
- `RegistrationResponse.java`
- `LoginResponse.java`
- `TraineeProfileResponse.java`
- `TrainerProfileResponse.java`
- `UpdateTraineeProfileResponse.java`
- `UpdateTrainerProfileResponse.java`
- `TrainerListResponse.java`
- `TrainingListResponse.java`
- `TrainingTypeResponse.java`
- `ErrorResponse.java`

#### 5. **entity/** - JPA Entities
- `User.java` - Base user entity (one-to-one with Trainee/Trainer)
- `Trainee.java` - Trainee entity with date of birth, address
- `Trainer.java` - Trainer entity with specialization
- `Training.java` - Training session entity
- `TrainingTypeEntity.java` - Training type reference table

**Entity Relationships:**
- User ↔ Trainee: One-to-One (cascade all, orphan removal)
- User ↔ Trainer: One-to-One (cascade all, orphan removal)
- Trainee ↔ Trainer: Many-to-Many
- Training → Trainee: Many-to-One
- Training → Trainer: Many-to-One
- Training → TrainingTypeEntity: Many-to-One

#### 6. **exception/** - Custom Exceptions
- `ResourceNotFoundException.java` - 404 errors
- `InvalidCredentialsException.java` - 401 errors
- `AccountLockedException.java` - repeated failed login handling
- `AlreadyExistsException.java` - 409 errors
- `ValidationException.java` - 400 errors

#### 7. **facade/** - Business Logic Coordination
- `TrainingFacade.java` - Coordinates training creation across services

#### 8. **service/** - Business Logic Layer
- `TraineeService.java` - Trainee business logic
- `TrainerService.java` - Trainer business logic
- `TrainingService.java` - Training business logic
- `UserService.java` - Login, password change, logout, and authentication workflows
- `BruteForceProtectionService.java` - failed login tracking and lockout windows
- `TokenBlacklistService.java` - revoked token tracking
- `GeneratedCredentials.java` - generated username/password return type for registration

#### 9. **util/** - Utility Classes
- `CredentialsGenerator.java` - Username/password generation
- `TransactionContext.java` - Thread-local transaction ID storage

#### 10. **TrainingApplication.java** - Spring Boot main class

### Test Structure (`src/test/java/com/exgym/training/`)

- **dao/** - DAO layer tests (mocked)
  - `TraineeDaoTest.java`
  - `TrainerDaoTest.java`
  - `TrainingDaoTest.java`

- **service/** - Service layer tests
  - `TraineeServiceTest.java`
  - `TrainerServiceTest.java`
  - `TrainingServiceTest.java`

- **facade/** - Facade layer tests
  - `TrainingFacadeTest.java`

- **util/** - Utility tests
  - `CredentialsGeneratorTest.java`
  - `TestDataLoader.java` - Test data loading utility

- **TrainingApplicationTests.java** - Spring context test

### Resources (`src/main/resources/`)
- `application.properties` - Application configuration
- `data/init-data.txt` - Initial data file (not currently loaded automatically)

## Key Implementation Details

### 1. Transaction Logging
The application implements **two-level logging** as required:

1. **Transaction Level**: Each request gets a unique transaction ID via `LoggingInterceptor`
   - Generated as UUID if not provided
   - Stored in `TransactionContext` (ThreadLocal)
   - Added to MDC for log correlation
   - Included in error responses

2. **REST Call Details**: Each endpoint logs:
   - Request: method, URI, parameters
   - Response: status code, result
   - Errors: exception details with stack trace

### 2. Authentication & Authorization
- `POST /api/v1/user/login` validates credentials and returns a JWT bearer token
- Passwords are stored with BCrypt and checked through Spring Security user details
- All endpoints except register/login, health, swagger, and H2 console require authentication
- Controllers enforce ownership checks so users can only access their own trainee/trainer resources
- `POST /api/v1/user/logout` revokes the current token through a blacklist service
- Repeated failed logins trigger temporary account lockout

### 3. Username Generation
- Format: `FirstName.LastName`
- If username exists, appends incrementing number: `FirstName.LastName1`, `FirstName.LastName2`, etc.
- Implemented in `CredentialsGenerator`

### 4. Password Generation
- Random 10-character alphanumeric string
- Uses `SecureRandom` for cryptographic strength
- Implemented in `CredentialsGenerator`

### 4a. Password Storage
- Generated passwords are returned once during registration
- Persisted passwords are BCrypt-hashed before storage

### 5. Validation
- Uses Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, etc.)
- Handled by `GlobalExceptionHandler` which returns structured error responses
- All DTO request objects have validation constraints

### 6. Error Handling
Comprehensive error handling via `@RestControllerAdvice`:
- 400 Bad Request - Validation errors
- 401 Unauthorized - Invalid credentials
- 404 Not Found - Resource not found
- 409 Conflict - Already exists
- 500 Internal Server Error - Unexpected errors

All errors include:
- Timestamp
- Status code
- Error type
- Message
- Request path
- Transaction ID

### 7. Cascade Operations
- **Delete Trainee**: Hard delete with cascade to trainings (CascadeType.ALL)
- **Delete Trainer**: No cascade to trainings (trainings remain)
- **Training**: No update/delete endpoints available

### 8. Idempotency
- Activate/Deactivate operations are **non-idempotent** by design
- Controllers log warnings when state matches requested state but still process the request

### 9. API Documentation
- Uses **OpenAPI 3** (Swagger) annotations
- Each endpoint documented with:
  - `@Operation` - Summary and description
  - `@ApiResponses` - Response codes and meanings
- Accessible at: `/swagger-ui.html`

### 10. Profiles and Datasource Selection
- `application.properties` contains shared defaults and uses `local` as the default profile
- `application-local.properties` uses H2 for local development
- `application-dev.properties` is intended for PostgreSQL configuration
- If PostgreSQL settings are added to `application.properties` but the app still runs on H2, the active profile is probably still `local`

## Database Schema

### Schema: `exgym`

**Tables:**
1. **user** - Base user information
   - id (PK, auto-increment)
   - firstName (required)
   - lastName (required)
   - userName (required, unique)
   - password (required)
   - isActive (required, Boolean)

2. **trainee** - Trainee-specific data
   - id (PK, auto-increment)
   - address (optional)
   - dateOfBirth (optional, Date)
   - user_id (FK to user, one-to-one)

3. **trainer** - Trainer-specific data
   - id (PK, auto-increment)
  - training_type_id (required, FK to training_type)
   - user_id (FK to user, one-to-one)

4. **training** - Training sessions
   - id (PK, auto-increment)
   - trainingName (required)
   - trainingDate (required, Date)
   - trainingDuration (required, int)
   - trainee_id (FK to trainee)
   - trainer_id (FK to trainer)
   - training_type_id (FK to training_type)

5. **training_type** - Training type reference
   - id (PK, auto-increment)
   - trainingTypeName (required, unique)

6. **trainee_trainer** (join table) - Many-to-many relationship
   - trainee_id (FK to trainee)
   - trainer_id (FK to trainer)

## Running the Application

### Build
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Generate Test Coverage Report
```bash
./mvnw test jacoco:report
```
Coverage report location: `target/site/jacoco/index.html`

## API Testing

### Swagger UI
Access at: `http://localhost:8080/swagger-ui.html`

### Example Requests

**Register Trainee:**
```bash
curl -X POST http://localhost:8080/api/v1/trainee/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "address": "123 Main St"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "John.Doe",
    "password": "abc123xyz"
  }'
```

**Get Trainee Profile:**
```bash
curl -X GET "http://localhost:8080/api/v1/trainee/profile?username=John.Doe" \
  -H "Authorization: Bearer <jwt-token>"
```

## API Endpoints Summary

### Implemented REST Endpoints (19 total)

**Trainee Management:**
1. POST /api/v1/trainee/register - Register new trainee
2. GET /api/v1/trainee/profile - Get trainee profile
3. PUT /api/v1/trainee/profile - Update trainee profile
4. DELETE /api/v1/trainee/profile - Delete trainee profile
5. GET /api/v1/trainee/trainers/not-assigned - Get available trainers
6. PUT /api/v1/trainee/trainers - Update trainer assignments
7. GET /api/v1/trainee/trainings - Get trainee's training list
8. PATCH /api/v1/trainee/status - Activate/deactivate trainee

**Trainer Management:**
9. POST /api/v1/trainer/register - Register new trainer
10. GET /api/v1/trainer/profile - Get trainer profile
11. PUT /api/v1/trainer/profile - Update trainer profile
12. GET /api/v1/trainer/trainings - Get trainer's training list
13. PATCH /api/v1/trainer/status - Activate/deactivate trainer

**Authentication:**
14. POST /api/v1/user/login - User login
15. PUT /api/v1/user/change-password - Change password
16. POST /api/v1/user/logout - Logout current token

**Training Management:**
17. POST /api/v1/training - Add new training
18. GET /api/v1/training/types - Get training types

**Training Type Management:**
19. POST /api/v1/training-types - Add training type

## Key Features

- **Auto-generated Credentials**: Usernames and passwords generated automatically during registration
- **Separate User Types**: Trainee and Trainer are distinct entities (cannot be both)
- **Validation**: Comprehensive validation on all endpoints using Jakarta Bean Validation
- **One-to-One Relations**: User table has one-to-one relationship with Trainee/Trainer tables
- **Immutable Usernames**: Usernames cannot be changed after creation
- **Many-to-Many**: Trainee ↔ Trainer relationship supports multiple assignments
- **Non-idempotent Operations**: Activate/deactivate operations process even if state unchanged
- **Cascade Delete**: Deleting a trainee cascades to their trainings
- **Type Safety**: Proper data types (int for duration, Date for dates, Boolean for status)
- **JWT-Protected APIs**: Protected endpoints require bearer tokens and enforce username ownership
- **Error Handling**: Comprehensive error handling with proper HTTP status codes
- **Unit Testing**: Full test coverage across layers
- **Transaction Logging**: Two-level logging with transaction IDs
- **API Documentation**: Full OpenAPI/Swagger documentation

## Architecture Notes

### Design Decisions
1. **No Training Updates**: Training sessions cannot be modified or deleted after creation
2. **Trainer Specialization**: Stored as foreign key reference to `TrainingTypeEntity`
3. **Authentication Pattern**: JWT-based stateless authentication with controller-level ownership checks
4. **Hard Delete**: Trainee deletion permanently removes data and cascades to trainings
5. **Trainer Delete**: Trainer deletion preserves associated trainings

### Coding Standards

#### Import Guidelines
- **No Inline Fully-Qualified Names**: Always use proper import statements at the top of the file - NO EXCEPTIONS
- **Forbidden Pattern**: `java.util.Map<Long, User> existingUsers` or `new java.util.ArrayList<>()`
- **Correct Pattern**: Add `import java.util.Map;` at the top, then use `Map<Long, User> existingUsers`
- **Rationale**: Inline fully-qualified class names reduce code readability and violate Java conventions
- **For Name Conflicts**: When classes have identical simple names (e.g., `Date`), import the most frequently used one and use a class alias or refactor to avoid ambiguity. Never use inline fully-qualified names in the code body.

#### General Style
- Use proper imports for all classes from `java.util.*`, `java.time.*`, etc.
- Keep imports organized and clean (remove unused imports)
- Follow standard Java naming conventions

## Common Tasks for Agents

### Adding a New Endpoint
1. Create/update request DTO in `dto/request/`
2. Create/update response DTO in `dto/response/`
3. Add endpoint method in appropriate controller
4. Add `@Operation` and `@ApiResponses` annotations
5. Implement business logic in service layer
6. Add DAO method if needed
7. Write unit tests

### Adding a New Entity
1. Create entity class in `entity/` with JPA annotations
2. Define relationships with existing entities
3. Create DAO interface in `dao/` extending `JpaRepository`
4. Update relevant services
5. Add migration or update `ddl-auto` setting

### Debugging Request Flow
1. Check `LoggingInterceptor` for incoming request logs
2. Check controller method execution
3. Check service layer logic
4. Check DAO queries
5. Check `GlobalExceptionHandler` for error handling
6. All logs include transaction ID for correlation

### Running Specific Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TraineeServiceTest

# Run with coverage
./mvnw clean test jacoco:report
```

## Important Notes
- **Schema**: All tables are in schema `exgym`
- **Logging Pattern**: Includes transaction ID via MDC: `%X{transactionId}`
- **Date Format**: Uses `java.util.Date` throughout
- **Validation**: Uses Jakarta Bean Validation (javax.validation)
- **API Prefix**: All endpoints start with `/api/v1/` (versioned)
- **API Versioning**: Version configured in `application.properties` as `api.version=/api/v1`
- **Security**: Currently configured to permit all requests

## Contact & Support
- API Documentation: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

**Last Updated**: February 27, 2026
**Version**: 1.0
