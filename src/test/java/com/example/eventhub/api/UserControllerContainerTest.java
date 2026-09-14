package com.example.eventhub.api;

import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.config.PostgresContainerConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("container")
@Import(PostgresContainerConfig.class)
class UserControllerContainerTest {

    @Autowired
    private FileUtils fileUtils;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/users/me Should return current user when authenticated")
    void me_returns200_WhenAuthenticated() throws Exception {
        var request = fileUtils.readResourceFile("user/user-request-fields-200.json");
        String email = "me@test.com";
        String password = "12345678";

        given()
                .contentType(ContentType.JSON)
                .body(request.formatted(email, password)).when()
                .post("/api/v1/auth/register")
                .then().statusCode(201);

        var loginRequest = fileUtils.readResourceFile("user/login-request-model-200.json").formatted(email, password);

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest.formatted(email, password))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("token");

        given()
                .header("Authorization", "Bearer " + token).when()
                .get("/api/v1/users/me")
                .then().statusCode(200)
                .body("name", equalTo("Matheus"))
                .body("email", equalTo(email))
                .body("role", equalTo("USER"))
                .body("id", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/users/me Should return 401 Unauthorized when request is unauthenticated")
    void me_returns401_WhenUnauthenticated() {
        given().when().get("/api/v1/users/me").then().statusCode(401);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/users/me Should return 401 Unauthorized when token is invalid")
    void me_returns401_WhenTokenIsInvalid() {
        given().header("Authorization", "Bearer invalid-token")
                .when().get("/api/v1/users/me")
                .then().statusCode(401);
    }
}