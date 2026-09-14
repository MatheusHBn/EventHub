package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.config.SecurityConfig;
import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.event.EventCreateRequest;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.exception.GlobalErrorHandler;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.mapper.EventMapper;
import com.example.eventhub.security.CustomAccessDeniedHandler;
import com.example.eventhub.security.CustomAuthenticationEntryPoint;
import com.example.eventhub.security.CustomUserDetailsService;
import com.example.eventhub.security.JwtService;
import com.example.eventhub.service.EventService;
import com.example.eventhub.service.UserService;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalErrorHandler.class,
        FileUtils.class
})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService service;

    @MockitoBean
    private EventMapper mapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private FileUtils fileUtils;

    private final UserUtils userUtils = new UserUtils();
    private final EventUtils eventUtils = new EventUtils();

    @Test
    @Order(1)
    @DisplayName("Should return 201 Created and response body when event is created successfully")
    void createEvent_returnsCreated_WhenSuccessful() throws Exception {
        User user = userUtils.createSavedUser();

        when(userService.findByEmailOrThrow(user.getEmail())).thenReturn(user);

        Event event = eventUtils.createEvent(user);

        EventResponse eventResponse = eventUtils.createEventResponse();

        when(mapper.toEvent(any(EventCreateRequest.class))).thenReturn(event);
        when(service.createEvent(any())).thenReturn(event);
        when(mapper.toEventResponse(event)).thenReturn(eventResponse);

        var request = fileUtils.readResourceFile("event/event-request-200.json");
        var response = fileUtils.readResourceFile("event/event-response-200.json");

        String responseJson = mockMvc.perform(post("/api/v1/events")
                        .with(user("organizer@email.com").roles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(response, responseJson,
                new CustomComparator(JSONCompareMode.LENIENT,
                        new Customization("date", (_, _) -> true),
                        new Customization("createdAt", (_, _) -> true)
                ));

        verify(service).createEvent(any());
    }

    @Test
    @Order(2)
    @DisplayName("Should return 400 Bad Request when create request payload is invalid")
    void createEvent_returnsBadRequest_WhenRequestIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-400.json");

        mockMvc.perform(post("/api/v1/events")
                .with(user("organizer@email.com").roles("ORGANIZER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @Order(3)
    @DisplayName("Should return 200 OK and list of events when request is successful")
    void findAll_returnsEvents_WhenSuccessful() throws Exception {
        User user = userUtils.createSavedUser();
        Event event1 = eventUtils.createSavedEvent(user);
        Event event2 = eventUtils.createSavedEvent(user);

        var events = List.of(event1, event2);
        var response1 = eventUtils.createEventResponse();
        var response2 = eventUtils.createEventResponse();

        when(service.findAll()).thenReturn(events);
        when(mapper.toEventResponseList(events)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/events")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).findAll();
        verify(mapper).toEventResponseList(events);
    }

    @Test
    @Order(4)
    @DisplayName("Should return 200 OK and event details when ID exists")
    void findById_returnsEvent_WhenIdExists() throws Exception {

        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);

        var response = eventUtils.createEventResponse();

        when(service.findByIdOrThrow(event.getId())).thenReturn(event);
        when(mapper.toEventResponse(event)).thenReturn(response);

        mockMvc.perform(get("/api/v1/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()))
                .andExpect(jsonPath("$.title").value(event.getTitle()));

        verify(service).findByIdOrThrow(event.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 Not Found when event ID does not exist")
    void findById_returnsNotFound_WhenIdDoesNotExist() throws Exception {

        when(service.findByIdOrThrow(999L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/api/v1/events/999")).andExpect(status().isNotFound());

        verify(service).findByIdOrThrow(999L);
    }

    @Test
    @Order(6)
    @DisplayName("Should return 200 OK and updated event when update is successful")
    void updateEvent_returnsEvent_WhenSuccessful() throws Exception {
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);

        EventResponse updatedEventResponse = EventResponse.builder()
                .id(event.getId())
                .title("Evento atualizado")
                .description("Uma descrição autalizada")
                .date(LocalDateTime.now())
                .location("Campinas")
                .capacity(200)
                .status(EventStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .organizerId(user.getId())
                .build();

        var request = fileUtils.readResourceFile("event/event-request-200.json");
        var response = fileUtils.readResourceFile("event/event-response-update-200.json");

        when(service.updateEvent(eq(event.getId()), any(EventUpdateRequest.class), any(Authentication.class))).thenReturn(event);


        when(mapper.toEventResponse(event)).thenReturn(updatedEventResponse);

        String actualJsonResponse = mockMvc.perform(
                        put("/api/v1/events/{id}", event.getId())
                                .with(user(user.getEmail()).roles("ORGANIZER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(response, actualJsonResponse,
                new CustomComparator(JSONCompareMode.LENIENT,
                        new Customization("date", (_, _) -> true),
                        new Customization("createdAt", (_, _) -> true)
                ));

        verify(service).updateEvent(eq(event.getId()), any(EventUpdateRequest.class), any(Authentication.class));
    }

    @Test
    @Order(7)
    @DisplayName("Should return 400 Bad Request when update request payload is invalid")
    void updateEvent_returnsBadRequest_WhenRequestIsInvalid() throws Exception {

        var request = fileUtils.readResourceFile("event/event-request-400.json");

        mockMvc.perform(
                put("/api/v1/events/{id}", 1L)
                        .with(user("organizer@email.com").roles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @Order(8)
    @DisplayName("Should return 204 No Content when event deletion is successful")
    void deleteEvent_returnsNoContent_WhenSuccessful() throws Exception {
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);

        when(service.findByIdOrThrow(1L)).thenReturn(event);

        doNothing().when(service).deleteEvent(eq(event), any(Authentication.class));

        mockMvc.perform(delete("/api/v1/events/1")
                .with(user(user.getEmail()).roles("ORGANIZER"))).andExpect(status().isNoContent());

        verify(service).findByIdOrThrow(1L);

        verify(service).deleteEvent(eq(event), any(Authentication.class));
    }
}