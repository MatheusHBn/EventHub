package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static java.util.Optional.empty;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegistrationControllerIT {

    @Autowired
    private EventUtils eventUtils;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/events/{id}/registrations Should register user in published event")
    void create_returns201_WhenEventIsPublished() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-registration@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken("Normal User", "user-registration@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201)
                .body("eventId", equalTo(eventId.intValue()))
                .body("status", equalTo("ACTIVE"))
                .body("userId", notNullValue())
                .body("registeredAt", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/events/{id}/registrations Should return 401 Unauthorized when request is unauthenticated")
    void create_returns401_WhenUnauthenticated() {
        given().when().post("/api/v1/events/1/registrations").then().statusCode(401);
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/events/{id}/registrations Should return 404 Not Found when event does not exist")
    void create_returns404_WhenEventDoesNotExist() throws Exception {
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-nonexistent-event@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/999999/registrations")
                .then().statusCode(404);
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/events/{id}/registrations Should return 400 Bad Request when event is in DRAFT status")
    void create_returns400_WhenEventIsInDraftStatus() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-draft@test.com", "12345678");

        Long eventId = eventUtils.createEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-draft@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(400);
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/events/{id}/registrations Should return 409 Conflict when user is already registered")
    void create_returns409_WhenUserIsAlreadyRegistered() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-duplicate@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-duplicate@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(409);
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/events/{id}/registrations Should return 409 Conflict when event is full")
    void create_returns409_WhenEventIsFull() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-full@test.com", "12345678");
        Long eventId = eventUtils.createPublishedEvent(organizerToken, 1);

        String userTokenA = eventUtils.createUserAndGetToken
                ("User A", "user-full-a@test.com", "12345678");
        String userTokenB = eventUtils.createUserAndGetToken
                ("User B", "user-full-b@test.com", "12345678");

        given().header("Authorization", "Bearer " + userTokenA)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + userTokenB)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(409);
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/users/me/registrations Should find user's own registrations")
    void findMyRegistrations_returns200_WhenUserIsAuthenticated() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-my-registration@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-my-registration@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + userToken)
                .when().get("/api/v1/users/me/registrations")
                .then().statusCode(200)
                .body("$", not(empty()))
                .body("[0].eventId", equalTo(eventId.intValue()))
                .body("[0].status", equalTo("ACTIVE"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/users/me/registrations Should return 401 Unauthorized when request is unauthenticated")
    void findMyRegistrations_returns401_WhenUnauthenticated() {
        given().when().get("/api/v1/users/me/registrations").then().statusCode(401);
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/events/{id}/registrations Should find event registrations when user is event organizer")
    void findByEvent_returns200_WhenUserIsOrganizer() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-list-registration@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-list-registration@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + organizerToken)
                .when().get("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(200)
                .body("$", not(empty()))
                .body("[0].eventId", equalTo(eventId.intValue()));
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/events/{id}/registrations Should return 403 Forbidden when user is not event organizer")
    void findByEvent_returns403_WhenUserIsNotOrganizer() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-forbidden-registration@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-forbidden-registration@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().get("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(403);
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/v1/events/{id}/registrations Should find event registrations when user is admin")
    void findByEvent_returns200_WhenUserIsAdmin() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-admin-list@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String adminToken = eventUtils.createAdminAndGetToken
                ("Admin", "admin-list@test.com", "12345678");

        given().header("Authorization", "Bearer " + adminToken)
                .when().get("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(200);
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /api/v1/events/{id}/registrations Should cancel own registration")
    void cancel_returns204_WhenUserIsOwner() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-cancel@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-cancel@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + userToken)
                .when().delete("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(204);

        given().header("Authorization", "Bearer " + userToken)
                .when().get("/api/v1/users/me/registrations")
                .then().statusCode(200)
                .body("[0].status", equalTo("CANCELED"));
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /api/v1/events/{id}/registrations Should return 404 Not Found when registration does not exist")
    void cancel_returns404_WhenRegistrationDoesNotExist() throws Exception {
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-no-registration@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().delete("/api/v1/events/999999/registrations")
                .then().statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("DELETE /api/v1/events/{id}/registrations Should return 400 Bad Request when registration is already canceled")
    void cancel_returns400_WhenRegistrationIsAlreadyCanceled() throws Exception {
        String organizerToken = eventUtils.createOrganizerAndGetToken
                ("Organizer", "organizer-double-cancel@test.com", "12345678");

        Long eventId = eventUtils.createPublishedEvent(organizerToken);
        String userToken = eventUtils.createUserAndGetToken
                ("Normal User", "user-double-cancel@test.com", "12345678");

        given().header("Authorization", "Bearer " + userToken)
                .when().post("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + userToken)
                .when().delete("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(204);

        given().header("Authorization", "Bearer " + userToken)
                .when().delete("/api/v1/events/" + eventId + "/registrations")
                .then().statusCode(400);
    }
}