package com.exgym.training.cucumber.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;
import com.exgym.training.dao.UserDao;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class TrainingComponentSteps {

    private record Credentials(String username, String password) {
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private UserDao userDao;

    private final Map<String, Credentials> credentialsByAlias = new HashMap<>();

    private Response response;
    private String bearerToken;
    private String requestedProfileUsername;

    @Before
    public void setUp() {
        trainingDao.deleteAll();
        traineeDao.deleteAll();
        trainerDao.deleteAll();
        userDao.deleteAll();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        credentialsByAlias.clear();
        response = null;
        bearerToken = null;
        requestedProfileUsername = null;
    }

    @Given("a clean training component state")
    public void aCleanTrainingComponentState() {
        assertThat(userDao.count()).isZero();
    }

    @Given("I register trainee alias {string} with first name {string}, last name {string}, birth date {string}, and address {string}")
    public void iRegisterTraineeAliasWithFirstNameLastNameBirthDateAndAddress(
            String alias, String firstName, String lastName, String birthDate, String address) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "dateOfBirth", toIsoInstant(birthDate),
                        "address", address))
                .when()
                .post("/api/v1/trainee/register");

        if (response.statusCode() == 200) {
            credentialsByAlias.put(alias,
                    new Credentials(response.jsonPath().getString("username"), response.jsonPath().getString("password")));
        }
    }

    @When("I attempt to register a trainee with first name {string}, last name {string}, birth date {string}, and address {string}")
    public void iAttemptToRegisterATraineeWithFirstNameLastNameBirthDateAndAddress(
            String firstName, String lastName, String birthDate, String address) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "dateOfBirth", toIsoInstant(birthDate),
                        "address", address))
                .when()
                .post("/api/v1/trainee/register");
    }

    @When("I log in as trainee alias {string}")
    public void iLogInAsTraineeAlias(String alias) {
        Credentials credentials = credentialsByAlias.get(alias);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", credentials.username(),
                        "password", credentials.password()))
                .when()
                .post("/api/v1/user/login");

        bearerToken = response.jsonPath().getString("token");
    }

    @When("I request the authenticated trainee profile for alias {string}")
    public void iRequestTheAuthenticatedTraineeProfileForAlias(String alias) {
        requestedProfileUsername = credentialsByAlias.get(alias).username();
        response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get("/api/v1/trainee/profile?username={username}", requestedProfileUsername);
    }

    @When("I request the trainee profile for alias {string} using the current token")
    public void iRequestTheTraineeProfileForAliasUsingTheCurrentToken(String alias) {
        requestedProfileUsername = credentialsByAlias.get(alias).username();
        response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get("/api/v1/trainee/profile?username={username}", requestedProfileUsername);
    }

    @Then("the training response status should be {int}")
    public void theTrainingResponseStatusShouldBe(Integer statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Then("the training login response should contain a bearer token")
    public void theTrainingLoginResponseShouldContainABearerToken() {
        assertThat(response.jsonPath().getString("token")).isNotBlank();
        assertThat(response.jsonPath().getString("username")).isEqualTo(credentialsByAlias.values().iterator().next().username());
    }

    @Then("the trainee profile response should contain first name {string}")
    public void theTraineeProfileResponseShouldContainFirstName(String firstName) {
        assertThat(response.jsonPath().getString("firstName")).isEqualTo(firstName);
        assertThat(response.jsonPath().getString("trainers.size()")).isNotNull();
    }

    @Then("the training error message should contain {string}")
    public void theTrainingErrorMessageShouldContain(String messagePart) {
        assertThat(response.jsonPath().getString("message")).contains(messagePart);
    }

    private String toIsoInstant(String date) {
        Instant instant = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}