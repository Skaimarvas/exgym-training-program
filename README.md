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
- **Training Session Management**:
  - Create training sessions with specific details
  - Query trainee trainings with filters (date range, trainer, training type)
  - Query trainer trainings with filters (date range, trainee)
- **Authentication**: Password-based authentication system
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

The application uses PostgreSQL for production. You need to configure your database connection in `src/main/resources/application.properties`:

1. Open `application.properties`
2. Update the following properties with your database credentials:

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

**Important**: Replace `YOUR_DATABASE_USERNAME` and `YOUR_DATABASE_PASSWORD` with your actual PostgreSQL credentials.

### Database Setup

1. **Create the PostgreSQL database**:
   ```sql
   CREATE DATABASE exgym_db;
   ```

2. **Create the schema**:
   ```sql
   CREATE SCHEMA exgym;
   ```

3. The application will automatically create the necessary tables on startup using Hibernate's `ddl-auto=update` setting.

### Alternative: Using H2 (In-Memory Database)

For quick testing without PostgreSQL, you can use H2 database:

1. Comment out PostgreSQL configuration in `application.properties`
2. Add H2 configuration:
   ```properties
   spring.datasource.url=jdbc:h2:mem:exgym_db
   spring.datasource.driverClassName=org.h2.Driver
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.h2.console.enabled=true
   ```

## Running the Application

### Using Maven

1. **Build the project**:
   ```bash
   mvn clean install
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
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

### Run tests with code coverage:
```bash
mvn clean test jacoco:report
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

## Project Structure

```
exgym-training-program/
├── src/
│   ├── main/
│   │   ├── java/com/exgym/training/
│   │   │   ├── dao/               # Data Access Layer (Repositories)
│   │   │   ├── entity/            # JPA Entities
│   │   │   ├── enums/             # Enumerations (TrainingType)
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
- `authenticate(userName, password)`: Authenticate trainee
- `changePassword(userName, oldPassword, newPassword)`: Change password
- `activate(userName)`: Activate trainee account
- `deactivate(userName)`: Deactivate trainee account
- `updateTrainersList(traineeUserName, trainerIds)`: Update assigned trainers

**TrainerService**:
- `create(firstName, lastName, specialization)`: Create new trainer
- `update(trainer)`: Update trainer information
- `select(trainerId)`: Get trainer by ID
- `selectByUsername(userName)`: Get trainer by username
- `authenticate(userName, password)`: Authenticate trainer
- `changePassword(userName, oldPassword, newPassword)`: Change password
- `activate(userName)`: Activate trainer account
- `deactivate(userName)`: Deactivate trainer account
- `findNotAssignedToTrainee(traineeUserName)`: Find available trainers

**TrainingService**:
- `create(trainer, trainee, trainingName, trainingType, trainingDate, trainingDuration)`: Create training session
- `select(trainingId)`: Get training by ID
- `getTraineeTrainings(traineeUserName, fromDate, toDate, trainerName, trainingType)`: Query trainee's trainings
- `getTrainerTrainings(trainerUserName, fromDate, toDate, traineeName)`: Query trainer's trainings

#### 3. Training Types

Available training types (defined in `TrainingType` enum):
- CARDIO
- STRENGTH
- FLEXIBILITY
- BALANCE
- BULKING
- CUTTING
- MAINTENANCE
- HIIT
- ENDURANCE
- FUNCTIONAL
- CIRCUIT_TRAINING
- CROSS_FIT
- YOGA
- PILATES
- SPORTS_SPECIFIC

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
   - Check database credentials in `application.properties`
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

**Note**: This application is designed for educational/demonstration purposes. For production use, consider adding:
- REST API endpoints with proper controllers
- Authentication and authorization with JWT
- Password encryption (BCrypt)
- Input validation with Bean Validation
- API documentation with Swagger/OpenAPI
- Actuator endpoints for monitoring
- Centralized exception handling with @ControllerAdvice
- DTOs for API requests/responses
