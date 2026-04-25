# ExGym Training Program

A comprehensive gym training management system built with Spring Boot, providing functionality for managing trainees, trainers, and training sessions.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [API Overview](#api-overview)
- [Code Quality](#code-quality)

## Features

- **User Management**: Create and manage user profiles with automatic username and password generation
- **Trainee Management**: 
  - Register new trainees with personal information
  - Update trainee profiles
  - Activate/Deactivate trainee accounts
  - Delete trainee profiles
  - Assign trainers to trainees
- **Trainer Management**:
  - Register trainers with specializations
  - Update trainer profiles
  - Activate/Deactivate trainer accounts
  - Find trainers not assigned to specific trainees
   - Trainer specialization validated against `training_type` table
- **Training Session Management**:
  - Create training sessions with specific details
  - Query trainee trainings with filters (date range, trainer, training type)
  - Query trainer trainings with filters (date range, trainee)
   - Validation for training date (must be now/future) and duration bounds
- **Training Type Management**:
   - Default training types are auto-seeded on startup when table is empty (`YOGA`, `STRENGTH`, `CARDIO`)
- **Authentication**:
   - Public registration and login endpoints
   - JWT bearer authentication for protected endpoints
   - BCrypt password hashing with logout token invalidation
- **Custom Exception Handling**: Comprehensive error handling with custom exceptions

## Technology Stack

- **Java**: 25
- **Spring Boot**: 4.0.1
- **Spring Data JPA**: Database abstraction and ORM
- **Spring Security**: Security framework
- **PostgreSQL**: Primary database (production)
- **H2 Database**: In-memory database (testing)
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for tests
- **JaCoCo**: Code coverage reporting
- **Maven**: Build and dependency management

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK)** 25 or higher
- **Maven** 3.6 or higher
- **PostgreSQL** 12 or higher (for production use)
- **Git** (optional, for cloning the repository)

## Installation

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd exgym-training-program
   ```

2. **Verify Java installation**:
   ```bash
   java -version
   ```
   Should display Java 25 or higher.

3. **Verify Maven installation**:
   ```bash
   mvn -version
   ```

## Configuration

### Database Configuration

The application uses Spring profiles for datasource selection:

- `application.properties`: shared defaults
- `application-local.properties`: local H2 configuration
- `application-dev.properties`: PostgreSQL development configuration

If you want to run against PostgreSQL, configure `src/main/resources/application-dev.properties` and start the app with the `dev` profile.

Update the following properties with your database credentials:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/exgym_db
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD

# Hibernate Configuration
spring.jpa.properties.hibernate.default_schema=exgym
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
```

**Important**: Replace the datasource values with your actual PostgreSQL credentials.

### Database Setup

1. **Create the PostgreSQL database**:
   ```sql
   CREATE DATABASE exgym_db;
   ```

2. If using a custom DB name, update `spring.datasource.url` accordingly (for example `jdbc:postgresql://localhost:5432/exgym_db`).

3. **Create the schema**:
   ```sql
   CREATE SCHEMA exgym;
   ```

4. The application will automatically create or update the necessary tables on startup based on the configured `ddl-auto` setting.

**Note**: The default `local` profile still uses H2. Use the `dev` profile when you want PostgreSQL.

### Alternative: Using H2 (In-Memory Database)

For quick testing without PostgreSQL, use the default `local` profile. It already points to H2.

## Running the Application

### Launch ActiveMQ

The training service now publishes trainer workload updates asynchronously to ActiveMQ instead of calling the workload service over REST.

The broker was launched with Docker Compose from the training-program repository root:

```bash
docker compose up -d activemq
```

This starts:

- JMS broker on `tcp://localhost:61616`
- ActiveMQ web console on `http://localhost:8161`

If you want the whole local stack instead of just the broker, use:

```bash
docker compose up -d activemq discovery-service exgymworkload exgym-training-program
```

The training service publishes to queue `trainer.workload.queue` using `spring-boot-starter-activemq` and the broker URL from `SPRING_ACTIVEMQ_BROKER_URL`.

### Using Maven

1. **Build the project**:
   ```bash
   mvn clean install
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Run with PostgreSQL dev profile**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Using Maven Wrapper (if mvnw is available)

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Using Java directly

```bash
mvn clean package
java -jar target/training-0.0.1-SNAPSHOT.jar
```

The application will start on the default port (typically 8080).

## Testing

### Run all tests:
```bash
mvn test
```

### Run all tests with the Maven wrapper:
```bash
./mvnw test
```

### Run only the Cucumber component suite:
```bash
./mvnw -Dtest=TrainingComponentCucumberTest test
```

This suite covers training-service component behaviour with a real Spring Boot context, including:

- successful trainee registration, login, and profile retrieval
- forbidden access to another trainee's profile
- negative validation flow for future birth dates

### Run only the microservice integration suite:
```bash
./mvnw -Dtest=MicroservicesIntegrationCucumberTest test
```

This suite starts the workload service as part of the test flow and verifies:

- successful trainer assignment, training creation, and workload propagation
- negative authorization flow where forbidden training creation does not produce workload data

### Run both new BDD suites together:
```bash
./mvnw -Dtest=TrainingComponentCucumberTest,MicroservicesIntegrationCucumberTest test
```

### Integration test prerequisites

- Docker must be running because the integration flow starts the workload service against a MongoDB Testcontainers instance.
- The suite launches the workload service automatically on port `18082` and the training service test context on port `18080`.

### Run tests with code coverage:
```bash
mvn clean test jacoco:report
```

### Focused messaging tests

```bash
./mvnw test -Dtest=WorkloadServiceClientTest,TrainingFacadeTest,TrainerControllerTest
```

After running tests with coverage, open the report:
```
target/site/jacoco/index.html
```

### Test Structure

- **Unit Tests**: Located in `src/test/java/com/exgym/training/`
  - `dao/`: DAO layer tests
  - `service/`: Service layer tests (TraineeServiceTest, TrainerServiceTest, TrainingServiceTest)
  - `facade/`: Facade layer tests
  - `util/`: Utility class tests
- **Cucumber Component Tests**: `src/test/java/com/exgym/training/cucumber/component/`
- **Cucumber Integration Tests**: `src/test/java/com/exgym/training/cucumber/integration/`
- **Feature Files**: `src/test/resources/features/`

## Project Structure

```
exgym-training-program/
├── src/
│   ├── main/
│   │   ├── java/com/exgym/training/
│   │   │   ├── config/            # Interceptors, exception handling, OpenAPI, seeding
│   │   │   ├── controller/        # REST controllers
│   │   │   ├── dao/               # Data Access Layer (Repositories)
│   │   │   ├── dto/               # Request/response DTOs (domain-organized)
│   │   │   ├── entity/            # JPA Entities
│   │   │   ├── exception/         # Custom Exceptions
│   │   │   ├── facade/            # Facade Pattern Implementation
│   │   │   ├── service/           # Business Logic Layer
│   │   │   ├── util/              # Utility Classes
│   │   │   └── TrainingApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data/
│   │           └── init-data.txt  # Sample data
│   └── test/
│       └── java/com/exgym/training/
│           └── [Test classes]
├── pom.xml
└── README.md
```

## API Overview

### Messaging Flow

The CRM service publishes workload updates to ActiveMQ in these flows:

- `POST /api/v1/training` publishes an `ADD` workload event after the training is created locally.
- `DELETE /api/v1/trainee/profile` publishes `DELETE` workload events for the trainee's existing trainings after the local delete succeeds.
- `PUT /api/v1/trainer/profile` replays the trainer's existing trainings as `DELETE` then `ADD` so the workload projection picks up updated trainer name and active status without changing workload totals.
- `PATCH /api/v1/trainer/{username}/status` uses the same replay strategy so the workload projection stays aligned with trainer activation changes.

All workload messages are sent to queue `trainer.workload.queue` and include the current transaction id as a JMS property when available.

### Manual Verification Flow

These are the manual flows used to verify the CRM-side messaging behavior before submission:

1. Start infrastructure:
   ```bash
   docker compose up -d activemq discovery-service exgymworkload
   ```
2. Start the CRM service:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Register a trainer and trainee, assign the trainer to the trainee, then create a training through the CRM API.
4. Query the workload service and verify the trainer's monthly summary increased.
5. Update the trainer profile or trainer active status in the CRM API.
6. Query the workload service again and verify the trainer name/status changed while the monthly duration stayed the same.
7. Delete the trainee profile in the CRM API.
8. Query the workload service again and verify the trainer's monthly summary decreased accordingly.

### Authentication Model

- Public endpoints: `POST /api/v1/trainee/register`, `POST /api/v1/trainer/register`, `POST /api/v1/user/login`
- Protected endpoints: all remaining API endpoints require `Authorization: Bearer <jwt>`
- Ownership is enforced on trainee and trainer resources. The authenticated username from the JWT must match the requested username.
- `POST /api/v1/user/logout` revokes the current bearer token.

### Common Request Flow

**Register trainee**
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

**Login**
```bash
curl -X POST "http://localhost:8080/api/v1/user/login" \
   -H "Content-Type: application/json" \
   -d '{
      "username": "John.Doe",
      "password": "generatedPassword"
   }'
```

**Get trainee profile**
```bash
curl -X GET "http://localhost:8080/api/v1/trainee/profile?username=John.Doe" \
   -H "Authorization: Bearer <jwt-token>"
```

### Core Components

#### 1. Entities
- **User**: Base user information (firstName, lastName, userName, password, isActive)
- **Trainee**: Extends User with address and dateOfBirth
- **Trainer**: Extends User with specialization
- **Training**: Training session with trainer, trainee, type, date, and duration

#### 2. Services

**TraineeService**:
- `create(firstName, lastName, address, dateOfBirth)`: Create new trainee
- `update(trainee)`: Update trainee information
- `delete(traineeId)`: Delete trainee by ID
- `select(traineeId)`: Get trainee by ID
- `selectByUsername(userName)`: Get trainee by username
- `updateStatus(userName, isActive)`: Set trainee active/inactive status
- `updateProfile(userName, firstName, lastName, dateOfBirth, address, isActive)`: Update trainee profile
- `updateTrainersList(traineeUserName, trainers)`: Update assigned trainers (persists owning side)

**TrainerService**:
- `create(firstName, lastName, specialization)`: Create new trainer
- `update(trainer)`: Update trainer information
- `select(trainerId)`: Get trainer by ID
- `selectByUsername(userName)`: Get trainer by username
- `updateStatus(userName, isActive)`: Set trainer active/inactive status
- `updateProfile(userName, firstName, lastName, isActive)`: Update trainer profile
- `findNotAssignedToTrainee(traineeUserName)`: Find available trainers

**UserService**:
- `login(username, password)`: Validate credentials and issue JWT
- `changePassword(username, oldPassword, newPassword)`: Change password for authenticated user
- `logout(token)`: Revoke the current JWT

**TrainingService**:
- `create(trainer, trainee, trainingName, trainingType, trainingDate, trainingDuration)`: Create training session
- `select(trainingId)`: Get training by ID
- `getTraineeTrainings(request)`: Query trainee's trainings by DTO filters
- `getTrainerTrainings(request)`: Query trainer's trainings by DTO filters

#### 3. Training Types

Training types are persisted in the `training_type` table and referenced by foreign key:
- `trainer.training_type_id`
- `training.training_type_id`

On startup, default values are auto-seeded when the table is empty:
- YOGA
- STRENGTH
- CARDIO

## Code Quality

### Custom Exception Handling

The application uses custom exceptions for better error handling:

- **ResourceNotFoundException**: When a requested resource is not found
- **ValidationException**: When entity validation fails
- **InvalidCredentialsException**: When authentication fails
- **AlreadyExistsException**: When attempting to create duplicate resources

### Best Practices Implemented

- **Separation of Concerns**: Clear separation between DAO, Service, and Facade layers
- **Dependency Injection**: Using Spring's dependency injection
- **Transaction Management**: Using `@Transactional` for database operations
- **Validation**: Input validation at service layer
- **Logging**: Comprehensive logging using SLF4J
- **Builder Pattern**: Using Lombok's `@SuperBuilder` for entity creation
- **Exception Handling**: Custom exceptions with meaningful messages
- **Validation**: Bad request type mismatches handled as HTTP 400 (e.g., invalid boolean/date formats)

### Code Coverage

Run JaCoCo to check code coverage:
```bash
mvn clean test jacoco:report
```

View the HTML report at: `target/site/jacoco/index.html`

## Development Guidelines

### Adding New Features

1. Create entity in `entity/` package
2. Create repository interface in `dao/` package
3. Implement business logic in `service/` package
4. Add facade methods in `facade/` package (if needed)
5. Write unit tests in `test/` directory
6. Update this README if adding major features

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods short and focused
- Use Lombok annotations to reduce boilerplate

## Troubleshooting

### Common Issues

1. **Database Connection Error**:
   - Verify PostgreSQL is running
   - Check database credentials in the active profile properties file
   - Ensure database and schema exist

2. **Port Already in Use**:
   - Change the port in `application.properties`:
     ```properties
     server.port=8081
     ```

3. **Tests Failing**:
   - Ensure all dependencies are downloaded: `mvn clean install`
   - Check if H2 database is properly configured for tests

4. **Build Errors**:
   - Verify Java version: `java -version`
   - Clear Maven cache: `mvn clean`
   - Re-download dependencies: `mvn clean install -U`

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure all tests pass: `mvn test`
5. Create a pull request


---

**Note**: This application is designed for educational/demonstration purposes. For production use, consider:
- Rotating the JWT secret and managing it outside source control
- Replacing `ddl-auto` schema management with migrations (Flyway/Liquibase)
- Adding integration/API tests for endpoint-level contract coverage
