# ExGym Training Program - REST API Implementation

## Overview
This document describes the complete REST API implementation for the ExGym Training Program application. All implemented endpoints are documented with current validation, error handling, transaction logging, and Swagger/OpenAPI details.

## Architecture Changes

### 1. **Package Structure**
```
com.exgym.training/
├── config/                  # Configuration classes
│   ├── GlobalExceptionHandler.java
│   ├── LoggingInterceptor.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   ├── TrainingTypeDataInitializer.java
│   └── WebConfig.java
├── controller/              # REST controllers
│   ├── TraineeController.java
│   ├── TrainerController.java
│   ├── TrainingController.java
│   ├── TrainingTypeController.java
│   └── UserController.java
├── dto/
│   ├── common/             # Shared DTOs
│   ├── trainee/            # Trainee request/response DTOs
│   ├── trainer/            # Trainer request/response DTOs
│   ├── training/           # Training request/response DTOs
│   └── user/               # User request/response DTOs
├── util/
│   └── TransactionContext.java  # ThreadLocal transaction ID storage
└── (existing packages: dao, entity, exception, facade, service)
```

### 2. **New Dependencies Added**
- **Spring Boot Validation**: For request validation annotations
- **SpringDoc OpenAPI**: For Swagger/OpenAPI 3 documentation (v2.8.5)

## API Endpoints

### Base URL: `/api/v1`

### 1. User Authentication (`/api/v1/user`)

#### Login
- **Endpoint**: `POST /api/v1/user/login`
- **Request**: LoginRequest { username, password }
- **Response**: LoginResponse { username, token, expiresIn }
- **Description**: Authenticate trainee or trainer and issue JWT bearer token

#### Change Password
- **Endpoint**: `PUT /api/v1/user/change-password`
- **Auth**: Bearer token required
- **Request**: ChangePasswordRequest { oldPassword, newPassword }
- **Response**: 200 OK
- **Description**: Update the authenticated user's password

#### Logout
- **Endpoint**: `POST /api/v1/user/logout`
- **Auth**: Bearer token required
- **Request**: Authorization header only
- **Response**: 200 OK
- **Description**: Revoke the current bearer token

### 2. Trainee Management (`/api/v1/trainee`)

#### Register Trainee
- **Endpoint**: `POST /api/v1/trainee/register`
- **Request**: TraineeRegistrationRequest { firstName, lastName, dateOfBirth?, address? }
- **Response**: RegistrationResponse { username, password }
- **Auth**: Public endpoint

#### Get Trainee Profile
- **Endpoint**: `GET /api/v1/trainee/profile?username={username}`
- **Auth**: Bearer token required
- **Request**: Query parameter `username`
- **Response**: TraineeProfileResponse
- **Note**: `username` must match the username in the JWT subject

#### Update Trainee Profile
- **Endpoint**: `PUT /api/v1/trainee/profile`
- **Auth**: Bearer token required
- **Request**: UpdateTraineeProfileRequest
- **Response**: UpdateTraineeProfileResponse

#### Delete Trainee Profile
- **Endpoint**: `DELETE /api/v1/trainee/profile`
- **Auth**: Bearer token required
- **Request**: GetProfileRequest { username }
- **Response**: 200 OK
- **Note**: Cascade deletes related trainings

#### Get Not Assigned Trainers
- **Endpoint**: `GET /api/v1/trainee/trainers/not-assigned?username={username}`
- **Auth**: Bearer token required
- **Request**: Query parameter `username`
- **Response**: TrainerListResponse
- **Note**: Returns only active trainers, validates that trainee is active, and requires `username` to match the JWT subject

#### Update Trainer List
- **Endpoint**: `PUT /api/v1/trainee/trainers`
- **Auth**: Bearer token required
- **Request**: UpdateTraineeTrainerListRequest
- **Response**: TrainerListResponse
- **Note**: Updates many-to-many relation through owning side to persist join-table changes

#### Get Trainee Trainings
- **Endpoint**: `GET /api/v1/trainee/trainings`
- **Auth**: Bearer token required
- **Request**: Query params: `username` (required), `periodFrom`, `periodTo`, `trainerName`, `trainingType`
- **Response**: TrainingListResponse

#### Activate/Deactivate Trainee
- **Endpoint**: `PATCH /api/v1/trainee/status`
- **Auth**: Bearer token required
- **Request**: ActivateDeactivateRequest { username, isActive }
- **Response**: 200 OK
- **Note**: Non-idempotent operation

### 3. Trainer Management (`/api/v1/trainer`)

#### Register Trainer
- **Endpoint**: `POST /api/v1/trainer/register`
- **Request**: TrainerRegistrationRequest { firstName, lastName, specialization }
- **Response**: RegistrationResponse { username, password }
- **Note**: `specialization` must exist in `training_type` table
- **Auth**: Public endpoint

#### Get Trainer Profile
- **Endpoint**: `GET /api/v1/trainer/{username}/profile`
- **Auth**: Bearer token required
- **Request**: Path variable `username`
- **Response**: TrainerProfileResponse
- **Note**: `username` must match the username in the JWT subject

#### Update Trainer Profile
- **Endpoint**: `PUT /api/v1/trainer/profile`
- **Auth**: Bearer token required
- **Request**: UpdateTrainerProfileRequest
- **Response**: UpdateTrainerProfileResponse
- **Note**: Specialization is read-only

#### Get Trainer Trainings
- **Endpoint**: `GET /api/v1/trainer/{username}/trainings`
- **Auth**: Bearer token required
- **Request**: Path variable `username`; optional query params `periodFrom`, `periodTo`, `traineeName`
- **Response**: TrainingListResponse

#### Activate/Deactivate Trainer
- **Endpoint**: `PATCH /api/v1/trainer/{username}/status?isActive={true|false}`
- **Auth**: Bearer token required
- **Request**: Path variable `username`, query parameter `isActive`
- **Response**: 200 OK
- **Note**: Non-idempotent operation

### 4. Training Management (`/api/v1/training`)

#### Add Training
- **Endpoint**: `POST /api/v1/training`
- **Auth**: Bearer token required
- **Request**: AddTrainingRequest
- **Response**: 200 OK
- **Validation**:
  - `trainingDate` must be now/future (small clock-skew tolerance)
  - `trainingDuration` must be positive and <= 480 minutes
  - `trainingTypeName` must reference existing `training_type`
  - Authenticated user must match either `traineeUsername` or `trainerUsername`

#### Get Training Types
- **Endpoint**: `GET /api/v1/training/types`
- **Auth**: Bearer token required
- **Request**: None
- **Response**: TrainingTypeResponse

### 5. Training Type Management (`/api/v1/training-types`)

#### Add Training Type
- **Endpoint**: `POST /api/v1/training-types`
- **Auth**: Bearer token required
- **Request**: AddTrainingTypeRequest { trainingTypeName }
- **Response**: TrainingTypeInfo
- **Note**: Returns 409 if type already exists

## Key Features Implemented

### 1. **Request/Response DTOs**
- All endpoints use proper DTOs instead of multiple `@RequestBody` parameters
- Jakarta validation annotations ensure data integrity
- Lombok annotations reduce boilerplate code

### 2. **Validation**
- `@Valid` annotation triggers bean validation
- `@NotBlank`, `@NotNull`, `@Positive` constraints on fields
- Validation errors return 400 with detailed messages

### 3. **Error Handling**
- Global exception handler (`GlobalExceptionHandler`)
- Custom exceptions mapped to appropriate HTTP status codes:
  - `ResourceNotFoundException` → 404
  - `InvalidCredentialsException` → 401
  - `AlreadyExistsException` → 409
  - `ValidationException` → 400
  - `MethodArgumentNotValidException` → 400
  - `HttpMessageNotReadableException` → 400
  - `MethodArgumentTypeMismatchException` → 400
  - Generic exceptions → 500
- Consistent error response format with transaction ID

### 4. **Logging with Transaction IDs**
- **LoggingInterceptor**: Generates unique transaction ID per request
- Transaction ID stored in ThreadLocal (`TransactionContext`)
- Transaction ID added to MDC for log correlation
- Transaction ID included in response headers (`X-Transaction-Id`)
- Request/response logging with transaction ID
- Log pattern: `%d{yyyy-MM-dd HH:mm:ss} [%X{transactionId}] - %logger{36} - %msg%n`

### 5. **Swagger/OpenAPI Documentation**
- SpringDoc OpenAPI 3 integration
- `@Tag`, `@Operation`, `@ApiResponses` annotations on all endpoints
- Interactive API documentation at `/swagger-ui.html`
- OpenAPI JSON at `/v3/api-docs`

### 6. **Security Configuration**
- Spring Security uses stateless JWT bearer authentication
- Public endpoints are limited to trainee registration, trainer registration, login, health, swagger, and H2 console
- All remaining API endpoints require `Authorization: Bearer <token>`
- CSRF is disabled for the REST API
- Controller ownership checks return 403 when an authenticated user targets another user's resources

### 7. **Requirements Compliance**
- ✅ Username/password auto-generation during registration
- ✅ No dual trainer/trainee registration possible (separate endpoints)
- ✅ Authentication endpoints implemented (`/user/login`, `/user/change-password`, `/user/logout`)
- ✅ Required validation on all endpoints
- ✅ Username cannot be changed
- ✅ Activate/Deactivate is non-idempotent
- ✅ Delete trainee cascades to trainings
- ✅ Training duration is numeric (int)
- ✅ Dates use Java Date type
- ✅ IsActive is Boolean type
- ✅ Training types are table-backed references (`training_type` FK)
- ✅ No training update/delete endpoints
- ✅ Error handling implemented
- ✅ Transaction-level and REST call logging
- ✅ Swagger documentation

## How to Test

### 1. **Start the Application**
```bash
mvn spring-boot:run
```

### 2. **Access Swagger UI**
Open browser: `http://localhost:8080/swagger-ui.html`

### 3. **Example API Calls**

#### Register a Trainee
```bash
curl -X POST "http://localhost:8080/api/v1/trainee/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "address": "123 Main St"
  }'
```

#### Login
```bash
curl -X POST "http://localhost:8080/api/v1/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "John.Doe",
    "password": "generatedPassword"
  }'
```

#### Get Trainee Profile
```bash
curl -X GET "http://localhost:8080/api/v1/trainee/profile?username=John.Doe" \
  -H "Authorization: Bearer <jwt-token>"
```

#### Get Training Types
```bash
curl -X GET "http://localhost:8080/api/v1/training/types" \
  -H "Authorization: Bearer <jwt-token>"
```

### 4. **Transaction ID Tracking**
Check response headers for `X-Transaction-Id` and correlate with server logs:
```
2026-02-14 17:00:00 [a1b2c3d4-e5f6-7890-abcd-ef1234567890] - ...
```

## Testing

### Unit Tests
Unit tests should be updated to test:
1. Controller endpoints with MockMvc
2. Request/response DTO validation
3. Service layer business logic
4. Exception handling scenarios

### Integration Tests
Consider adding:
1. End-to-end API tests
2. Database integration tests
3. Transaction ID propagation tests

## Build and Run

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run with coverage
mvn test jacoco:report

# Package
mvn package

# Run application
mvn spring-boot:run
```

## Notes

1. **Authentication**: JWT is required for protected endpoints. Only register/login and diagnostic routes are public.
2. **Database**: Configure the profile-specific datasource properties for the environment you run.
3. **Transaction IDs**: Clients can provide `X-Transaction-Id` header; otherwise auto-generated.
4. **Date Format**: JSON dates should be in ISO-8601 format (e.g., "2026-01-15").
5. **Specialization**: Trainer specialization is a foreign-key reference to `training_type` and is read-only in trainer profile update.
6. **Default training types**: On startup, `YOGA`, `STRENGTH`, `CARDIO` are auto-seeded when `training_type` is empty.

## API Documentation Links

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console** (if using H2): http://localhost:8080/h2-console

## Support

For issues or questions, refer to the inline code documentation and Swagger UI for endpoint details.
