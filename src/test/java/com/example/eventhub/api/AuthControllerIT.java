package com.example.eventhub.api;

import com.example.eventhub.commons.FileUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private FileUtils fileUtils;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/auth/register Should register a user successfully")
    void register_returns201_WhenDataIsValid() throws Exception {
        var request = fileUtils.readResourceFile("auth/auth-request-201.json");

        given().contentType(ContentType.JSON)
                .body(request).when().post("/api/v1/auth/register")
                .then().statusCode(201)
                .body("name", equalTo("Matheus"))
                .body("email", equalTo("matheus@email.com"))
                .body("role", equalTo("USER"))
                .body("id", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/auth/login Should authenticate user and return token")
    void login_returns200_WhenCredentialsAreValid() throws Exception {
        var request = fileUtils.readResourceFile("auth/auth-request-201.json");

        given().contentType(ContentType.JSON)
                .body(request).when()
                .post("/api/v1/auth/register");

        var loginRequest = fileUtils.readResourceFile("auth/login-request-200.json");

        given().contentType(ContentType.JSON)
                .body(loginRequest).when()
                .post("/api/v1/auth/login")
                .then().statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/auth/login Should return 401 Unauthorized when password is wrong")
    void login_returns401_WhenPasswordIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("auth/auth-request-password-invalid-400.json");

        given().contentType(ContentType.JSON)
                .body(request).when()
                .post("/api/v1/auth/register");

        var invalidLogin = fileUtils.readResourceFile("auth/login-request-400.json");

        given().contentType(ContentType.JSON)
                .body(invalidLogin).when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/auth/register Should return 409 Conflict when email already exists")
    void register_returns409_WhenEmailAlreadyExists() throws Exception {
        var request = fileUtils.readResourceFile("auth/auth-request-duplicated-email-400.json");

        given().contentType(ContentType.JSON)
                .body(request).when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(201);

        given().contentType(ContentType.JSON)
                .body(request).when()
                .post("/api/v1/auth/register")
                .then().statusCode(409);
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/auth/register Should return 400 Bad Request when request fields are invalid")
    void register_returns400_WhenDataIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("auth/auth-request-invalid-fields-400.json");

        given().contentType(ContentType.JSON)
                .body(request).when()
                .post("/api/v1/auth/register")
                .then().statusCode(400);
    }
}