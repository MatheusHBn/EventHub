package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.domain.Role;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EventControllerIT {

    @Autowired
    private FileUtils fileUtils;

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
    @DisplayName("POST /api/v1/events Should create an event when user is an organizer")
    void create_returns201_WhenUserIsOrganizer() throws Exception {
        String email = "organizer@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("ORGANIZER", email, password);
        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);

        var request = fileUtils.readResourceFile("event/event-request-200.json");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .body("title", equalTo("Evento-importante 2"))
                .body("status", equalTo("DRAFT"))
                .body("organizerId", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/events Should return 403 Forbidden when user is a normal user")
    void create_returns403_WhenUserIsNotOrganizer() throws Exception {
        String token = eventUtils.registerAndLogin("Normal User", "user-event@test.com", "12345678");
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/events")
                .then()
                .log().all()
                .statusCode(403);
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/events Should return 401 Unauthorized when request is unauthenticated")
    void create_returns401_WhenUnauthenticated() throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        given().contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/events Should find all events")
    void findAll_returns200_WhenEventsExist() throws Exception {
        String email = "organizer-events@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", email, password);
        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);

        var request = fileUtils.readResourceFile("event/event-request-200.json");

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201);

        given().when().get("/api/v1/events")
                .then().statusCode(200).body("$", not(empty()));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/events/{id} Should find event by ID")
    void findById_returns200_WhenEventExists() throws Exception {
        String email = "organizer-find@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", email, password);
        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        Long eventId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201).extract().jsonPath().getLong("id");

        given().when().get("/api/v1/events/" + eventId)
                .then().statusCode(200)
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo("Evento-importante 2"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/events/{id} Should return 404 Not Found when event does not exist")
    void findById_returns404_WhenEventDoesNotExist() {
        given().when().get("/api/v1/events/999999").then().statusCode(404)
                .body("message", equalTo("This id not found"));
    }

    @Test
    @Order(7)
    @DisplayName("PATCH /api/v1/events/{id}/publish Should publish own event")
    void publish_returns200_WhenUserIsOwner() throws Exception {
        String email = "organizer-publish@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", email, password);

        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);

        var request = fileUtils.readResourceFile("event/event-request-200.json");

        Long eventId = given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when().patch("/api/v1/events/" + eventId + "/publish")
                .then().statusCode(200)
                .body("id", equalTo(eventId.intValue()))
                .body("status", equalTo("PUBLISHED"));
    }

    @Test
    @Order(8)
    @DisplayName("PATCH /api/v1/events/{id}/publish Should return 403 Forbidden when trying to publish another organizer's event")
    void publish_returns403_WhenUserIsNotOwner() throws Exception {
        String emailA = "organizer-a@test.com";
        String passwordA = "12345678";

        String emailB = "organizer-b@test.com";
        String passwordB = "12345678";

        eventUtils.registerAndLogin("Organizer A", emailA, passwordA);
        eventUtils.changeRole(emailA, Role.ORGANIZER);

        String tokenA = eventUtils.login(emailA, passwordA);

        var request = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + tokenA)
                .contentType(ContentType.JSON)
                .body(request).when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");


        eventUtils.registerAndLogin("Organizer B", emailB, passwordB);
        eventUtils.changeRole(emailB, Role.ORGANIZER);

        String tokenB = eventUtils.login(emailB, passwordB);


        given().header("Authorization", "Bearer " + tokenB)
                .when().patch("/api/v1/events/" + eventId + "/publish")
                .then().statusCode(403);
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /api/v1/events/{id}/publish  Should allow admin to publish another organizer's event")
    void publish_returns200_WhenUserIsAdmin() throws Exception {

        String organizerEmail = "organizer-admin-test@test.com";
        String organizerPassword = "12345678";

        String adminEmail = "admin-test@test.com";
        String adminPassword = "12345678";

        eventUtils.registerAndLogin("Organizer", organizerEmail, organizerPassword);
        eventUtils.changeRole(organizerEmail, Role.ORGANIZER);

        String organizerToken = eventUtils.login(organizerEmail, organizerPassword);
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + organizerToken)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        eventUtils.registerAndLogin("Admin", adminEmail, adminPassword);
        eventUtils.changeRole(adminEmail, Role.ADMIN);

        String adminToken = eventUtils.login(adminEmail, adminPassword);


        given().header("Authorization", "Bearer " + adminToken)
                .when().patch("/api/v1/events/" + eventId + "/publish")
                .then().statusCode(200)
                .body("status", equalTo("PUBLISHED"));
    }

    @Test
    @Order(10)
    @DisplayName("PUT /api/v1/events/{id} Should update own event")
    void update_returns200_WhenUserIsOwner() throws Exception {
        String email = "organizer-update@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", email, password);

        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);

        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        Long eventId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        var updateRequest = fileUtils.readResourceFile("event/event-update-request-200.json");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(updateRequest)
                .when().put("/api/v1/events/" + eventId)
                .then().statusCode(200)
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo("Evento atualizado"))
                .body("location", equalTo("Rio de Janeiro"))
                .body("capacity", equalTo(200));
    }

    @Test
    @Order(11)
    @DisplayName("PUT /api/v1/events/{id} Should return 403 Forbidden when trying to update another organizer's event")
    void update_returns403_WhenUserIsNotOwner() throws Exception {
        String emailA = "organizer-update-a@test.com";
        String emailB = "organizer-update-b@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer A", emailA, password);
        eventUtils.changeRole(emailA, Role.ORGANIZER);
        String tokenA = eventUtils.login(emailA, password);

        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + tokenA)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        eventUtils.registerAndLogin("Organizer B", emailB, password);

        eventUtils.changeRole(emailB, Role.ORGANIZER);
        String tokenB = eventUtils.login(emailB, password);

        var updateRequest = fileUtils.readResourceFile("event/event-update-request-200.json");

        given().header("Authorization", "Bearer " + tokenB)
                .contentType(ContentType.JSON).body(updateRequest)
                .when().put("/api/v1/events/" + eventId)
                .then().statusCode(403);
    }

    @Test
    @Order(12)
    @DisplayName("PUT /api/v1/events/{id} Should allow admin to update another organizer's event")
    void update_returns200_WhenUserIsAdmin() throws Exception {
        String organizerEmail = "organizer-admin-update@test.com";
        String adminEmail = "admin-update@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", organizerEmail, password);

        eventUtils.changeRole(organizerEmail, Role.ORGANIZER);
        String organizerToken = eventUtils.login(organizerEmail, password);

        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + organizerToken)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");


        eventUtils.registerAndLogin("Admin", adminEmail, password);
        eventUtils.changeRole(adminEmail, Role.ADMIN);

        String adminToken = eventUtils.login(adminEmail, password);
        var updateRequest = fileUtils.readResourceFile("event/event-update-request-200.json");

        given().header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(updateRequest).when().put("/api/v1/events/" + eventId)
                .then().statusCode(200)
                .body("title", equalTo("Evento atualizado"))
                .body("location", equalTo("Rio de Janeiro"))
                .body("capacity", equalTo(200));
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /api/v1/events/{id} Should cancel own event")
    void cancel_returns204_WhenUserIsOwner() throws Exception {
        String email = "organizer-delete@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", email, password);
        eventUtils.changeRole(email, Role.ORGANIZER);

        String token = eventUtils.login(email, password);
        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given().header("Authorization", "Bearer " + token)
                .when().delete("/api/v1/events/" + eventId)
                .then().statusCode(204);
    }

    @Test
    @Order(14)
    @DisplayName("DELETE /api/v1/events/{id} Should return 403 Forbidden when trying to cancel another organizer's event")
    void cancel_returns403_WhenUserIsNotOwner() throws Exception {
        String emailA = "organizer-delete-a@test.com";
        String emailB = "organizer-delete-b@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer A", emailA, password);
        eventUtils.changeRole(emailA, Role.ORGANIZER);

        String tokenA = eventUtils.login(emailA, password);
        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + tokenA)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        eventUtils.registerAndLogin("Organizer B", emailB, password);
        eventUtils.changeRole(emailB, Role.ORGANIZER);

        String tokenB = eventUtils.login(emailB, password);

        given().header("Authorization", "Bearer " + tokenB)
                .when().delete("/api/v1/events/" + eventId)
                .then().statusCode(403);
    }

    @Test
    @Order(15)
    @DisplayName("DELETE /api/v1/events/{id} Should allow admin to cancel another organizer's event")
    void cancel_returns204_WhenUserIsAdmin() throws Exception {
        String organizerEmail = "organizer-admin-delete@test.com";
        String adminEmail = "admin-delete@test.com";
        String password = "12345678";

        eventUtils.registerAndLogin("Organizer", organizerEmail, password);
        eventUtils.changeRole(organizerEmail, Role.ORGANIZER);

        String organizerToken = eventUtils.login(organizerEmail, password);
        var createRequest = fileUtils.readResourceFile("event/event-request-200.json");

        long eventId = given().header("Authorization", "Bearer " + organizerToken)
                .contentType(ContentType.JSON).body(createRequest)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        eventUtils.registerAndLogin("Admin", adminEmail, password);
        eventUtils.changeRole(adminEmail, Role.ADMIN);

        String adminToken = eventUtils.login(adminEmail, password);

        given().header("Authorization", "Bearer " + adminToken).when()
                .delete("/api/v1/events/" + eventId)
                .then().statusCode(204);

        given().when().get("/api/v1/events/" + eventId)
                .then().statusCode(200)
                .body("status", equalTo("CANCELED"));
    }
}