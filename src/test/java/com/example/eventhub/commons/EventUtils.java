package com.example.eventhub.commons;

import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.repository.UserRepository;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


@Component
public class EventUtils {

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private UserRepository userRepository;

    public List<Event> newEventList(User user1, User user2) {
        var event1 = Event.builder().title("Evento-importante").description("Uma descrição importante").date(LocalDateTime.now().plusDays(1))
                .location("São Paulo").capacity(20).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
                organizer(user1).build();
        var event2 = Event.builder().title("Evento-importante4").description("Uma descrição importante4").date(LocalDateTime.now().plusDays(4))
                .location("São Paulo").capacity(30).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
                organizer(user2).build();

        return new ArrayList<>(List.of(event1, event2));
    }

    public Event createEvent(User user) {
        return Event.builder().title("Evento-importante").description("Uma descrição importante").date(LocalDateTime.now().plusDays(1))
                .location("São Paulo").capacity(20).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
                organizer(user).build();
    }

    public Long createEvent(String token) throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        return given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }


    public Event createSavedEvent(User user) {
        return Event.builder().id(1L).title("Evento-importante 2").description("Uma descrição importante 2 ").date(LocalDateTime.now().plusMinutes(7))
                .location("São Paulo").capacity(20).status(EventStatus.PUBLISHED).createdAt(LocalDateTime.now().plusMinutes(29)).
                organizer(user).build();
    }

    public EventResponse createEventResponse() {
        return EventResponse.builder()
                .id(1L)
                .title("Evento-importante 2")
                .description("Uma descrição importante 2")
                .date(LocalDateTime.now().plusDays(10))
                .location("São Paulo")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .createdAt(LocalDateTime.parse("2026-09-17T14:54:05.6788501"))
                .organizerId(1L)
                .build();
    }

    public Long createPublishedEvent(String token) throws Exception {

        Long eventId = createEvent(token);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/v1/events/" + eventId + "/publish")
                .then()
                .statusCode(200)
                .body("status", equalTo("PUBLISHED"));

        return eventId;
    }

    public String registerAndLogin(String name, String email, String password) throws Exception {

        var authRequest = fileUtils.readResourceFile("user/auth-request-model-200.json");

        given()
                .contentType(ContentType.JSON)
                .body(authRequest.formatted(name, email, password))
                .when().post("/api/v1/auth/register")
                .then().statusCode(201);

        var loginRequest = fileUtils.readResourceFile("user/login-request-model-200.json");

        return given()
                .contentType(ContentType.JSON)
                .body(loginRequest.formatted(email, password))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    public void changeRole(String email, Role role) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        user.setRole(role);

        userRepository.save(user);
    }

    public String login(String email, String password) throws Exception {
        var loginRequest = fileUtils.readResourceFile("user/login-request-model-200.json");

        return given()
                .contentType(ContentType.JSON)
                .body(loginRequest.formatted(email, password))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    public String createUserAndGetToken(String name, String email, String password) throws Exception {
        return registerAndLogin(name, email, password);
    }

    public String createOrganizerAndGetToken(String name, String email, String password) throws Exception {

        registerAndLogin(name, email, password);
        changeRole(email, Role.ORGANIZER);

        return login(email, password);
    }

    public String createAdminAndGetToken(String name, String email, String password) throws Exception {
        registerAndLogin(name, email, password);
        changeRole(email, Role.ADMIN);

        return login(email, password);
    }

    public Long createPublishedEvent(String token, int capacity) throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-200.json");

        request = request.replace("\"capacity\": 100", "\"capacity\": " + capacity);

        Long eventId = given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(request)
                .when().post("/api/v1/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given().header("Authorization", "Bearer " + token)
                .when().patch("/api/v1/events/" + eventId + "/publish")
                .then().statusCode(200);

        return eventId;
    }
}
