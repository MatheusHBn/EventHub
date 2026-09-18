package com.example.eventhub.api;

import com.example.eventhub.commons.FileUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIT {

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
    @DisplayName("GET /api/v1/users/me Should return current user when authenticated")
    void me_returns200_WhenAuthenticated() throws Exception {
        var request = fileUtils.readResourceFile("user/user-request-fields-200.json");
        String email = "me@test.com";
        String password = "12345678";

        given().contentType(ContentType.JSON)
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

        given().header("Authorization", "Bearer " + token)
                .when().get("/api/v1/users/me")
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