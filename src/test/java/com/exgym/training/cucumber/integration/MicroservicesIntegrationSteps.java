package com.exgym.training.cucumber.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;
import com.exgym.training.dao.UserDao;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MicroservicesIntegrationSteps {

    private record Credentials(String username, String password) {
    }

    private static final int TRAINING_PORT = 18080;
    private static final int WORKLOAD_PORT = 18082;

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private UserDao userDao;

        @Autowired
        private JdbcTemplate jdbcTemplate;

    @Value("${app.security.interservice.jwt.secret}")
    private String sharedSecret;

    private final Map<String, Credentials> credentialsByAlias = new HashMap<>();
    private Response trainingResponse;
    private Response workloadResponse;
    private String currentUserToken;
    private String scenarioSuffix;

    @Before(order = 10)
    public void setUpScenario(Scenario scenario) {
        trainingDao.deleteAll();
                jdbcTemplate.execute("delete from exgym.trainee_trainer");
        traineeDao.deleteAll();
        trainerDao.deleteAll();
        userDao.deleteAll();

        credentialsByAlias.clear();
        trainingResponse = null;
        workloadResponse = null;
        currentUserToken = null;
        scenarioSuffix = UUID.nameUUIDFromBytes(scenario.getName().getBytes(StandardCharsets.UTF_8))
                .toString()
                .substring(0, 8);

        RestAssured.baseURI = "http://localhost";
    }

    @Given("the microservice integration state is clean")
    public void theMicroserviceIntegrationStateIsClean() {
        assertThat(userDao.count()).isZero();
    }

    @Given("I register integration trainee alias {string} with first name {string} and last name {string}")
    public void iRegisterIntegrationTraineeAliasWithFirstNameAndLastName(String alias, String firstName, String lastName) {
        String uniqueLastName = lastName + scenarioSuffix;
        trainingResponse = RestAssured.given()
                .port(TRAINING_PORT)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", uniqueLastName,
                        "dateOfBirth", toIsoInstant("1990-01-01"),
                        "address", "Test address"))
                .when()
                .post("/api/v1/trainee/register");

        credentialsByAlias.put(alias,
                new Credentials(trainingResponse.jsonPath().getString("username"), trainingResponse.jsonPath().getString("password")));
    }

    @Given("I register integration trainer alias {string} with first name {string}, last name {string}, and specialization {string}")
    public void iRegisterIntegrationTrainerAliasWithFirstNameLastNameAndSpecialization(
            String alias, String firstName, String lastName, String specialization) {
        String uniqueLastName = lastName + scenarioSuffix;
        trainingResponse = RestAssured.given()
                .port(TRAINING_PORT)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", uniqueLastName,
                        "specialization", specialization))
                .when()
                .post("/api/v1/trainer/register");

        credentialsByAlias.put(alias,
                new Credentials(trainingResponse.jsonPath().getString("username"), trainingResponse.jsonPath().getString("password")));
    }

    @When("I log in to the training service as alias {string}")
    public void iLogInToTheTrainingServiceAsAlias(String alias) {
        Credentials credentials = credentialsByAlias.get(alias);
        trainingResponse = RestAssured.given()
                .port(TRAINING_PORT)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", credentials.username(),
                        "password", credentials.password()))
                .when()
                .post("/api/v1/user/login");

        currentUserToken = trainingResponse.jsonPath().getString("token");
    }

        @When("I assign trainer alias {string} to trainee alias {string}")
        public void iAssignTrainerAliasToTraineeAlias(String trainerAlias, String traineeAlias) {
                Credentials trainee = credentialsByAlias.get(traineeAlias);
                Credentials trainer = credentialsByAlias.get(trainerAlias);

                Response assignmentResponse = RestAssured.given()
                                .port(TRAINING_PORT)
                                .header("Authorization", "Bearer " + currentUserToken)
                                .contentType(ContentType.JSON)
                                .body(Map.of(
                                                "traineeUsername", trainee.username(),
                                                "trainerUsernames", java.util.List.of(trainer.username())))
                                .when()
                                .put("/api/v1/trainee/trainers");

                assertThat(assignmentResponse.statusCode()).isEqualTo(200);
        }

    @When("I create a training named {string} for trainee alias {string} and trainer alias {string} on {string} with duration {int}")
    public void iCreateATrainingNamedForTraineeAliasAndTrainerAliasOnWithDuration(
            String trainingName, String traineeAlias, String trainerAlias, String trainingDate, Integer duration) {
        Credentials trainee = credentialsByAlias.get(traineeAlias);
        Credentials trainer = credentialsByAlias.get(trainerAlias);

        trainingResponse = RestAssured.given()
                .port(TRAINING_PORT)
                .header("Authorization", "Bearer " + currentUserToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "traineeUsername", trainee.username(),
                        "trainerUsername", trainer.username(),
                        "trainingName", trainingName,
                        "trainingTypeName", "YOGA",
                        "trainingDate", toIsoInstant(trainingDate),
                        "trainingDuration", duration))
                .when()
                .post("/api/v1/training");
    }

    @When("I query the workload summary for trainer alias {string}")
    public void iQueryTheWorkloadSummaryForTrainerAlias(String alias) {
        String serviceToken = generateServiceToken();
        String username = credentialsByAlias.get(alias).username();
        workloadResponse = RestAssured.given()
                .port(WORKLOAD_PORT)
                .header("Authorization", serviceToken)
                .when()
                .get("/api/v1/trainers/{username}/workload", username);
    }

    @Then("the integration training response status should be {int}")
    public void theIntegrationTrainingResponseStatusShouldBe(Integer statusCode) {
        assertThat(trainingResponse.statusCode()).isEqualTo(statusCode);
    }

    @Then("the integration workload response status should be {int}")
    public void theIntegrationWorkloadResponseStatusShouldBe(Integer statusCode) {
        assertThat(workloadResponse.statusCode()).isEqualTo(statusCode);
    }

    @Then("the workload summary for trainer alias {string} should contain {int} minutes for month {int} of year {int}")
    public void theWorkloadSummaryForTrainerAliasShouldContainMinutesForMonthOfYear(
            String alias, Integer duration, Integer month, Integer year) {
        assertThat(workloadResponse.jsonPath().getString("trainerUsername"))
                .isEqualTo(credentialsByAlias.get(alias).username());
        assertThat(workloadResponse.jsonPath().getInt("years[0].year")).isEqualTo(year);
        assertThat(workloadResponse.jsonPath().getInt("years[0].months[0].month")).isEqualTo(month);
        assertThat(workloadResponse.jsonPath().getInt("years[0].months[0].trainingSummaryDuration")).isEqualTo(duration);
    }

    private String toIsoInstant(String date) {
        Instant instant = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    private String generateServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(sharedSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return "Bearer " + Jwts.builder()
                .subject("training-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}