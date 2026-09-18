package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.config.SecurityConfig;
import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.event.EventCreateForm;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.exception.GlobalErrorHandler;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.mapper.EventMapper;
import com.example.eventhub.security.CustomAccessDeniedHandler;
import com.example.eventhub.security.CustomAuthenticationEntryPoint;
import com.example.eventhub.security.CustomUserDetailsService;
import com.example.eventhub.security.JwtService;
import com.example.eventhub.service.EventService;
import com.example.eventhub.service.S3Service;
import com.example.eventhub.service.UserService;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
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

    @MockitoBean
    S3Service s3Service;

    @Autowired
    private FileUtils fileUtils;

    private final UserUtils userUtils = new UserUtils();
    private final EventUtils eventUtils = new EventUtils();

    @Test
    @Order(1)
    @DisplayName("Should return 201 Created and response body when event is created successfully")
    void createEvent_returnsCreated_WhenSuccessful() throws Exception {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createEvent(user);
        var eventResponse = eventUtils.createEventResponse();

        when(userService.findByEmailOrThrow(user.getEmail())).thenReturn(user);
        when(mapper.toEvent(any(EventCreateForm.class))).thenReturn(event);
        when(service.createEvent(any())).thenReturn(event);
        when(service.updateImageUrl(any())).thenReturn(event);
        when(s3Service.upload(any(), any(), any())).thenReturn("link-falso-do-s3");
        when(mapper.toEventResponse(event)).thenReturn(eventResponse);

        var request = fileUtils.readResourceFile("event/event-request-200.json").formatted(LocalDateTime.now().plusDays(1));
        var response = fileUtils.readResourceFile("event/event-response-200.json");

        MockMultipartFile eventPart = new MockMultipartFile(
                "event", "", "application/json", request.getBytes());

        MockMultipartFile imagePart = new MockMultipartFile(
                "image", "image.png", "image/png", "dados-da-imagem".getBytes());

        String responseJson = mockMvc.perform(multipart("/api/v1/events")
                        .file(eventPart).file(imagePart).with(user("organizer@email.com").roles("ORGANIZER")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(response, responseJson, new CustomComparator(JSONCompareMode.LENIENT,
                new Customization("date", (_, _) -> true),
                new Customization("createdAt", (_, _) -> true)));

        verify(service).createEvent(any());
        verify(s3Service).upload(any(), any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("Should return 400 Bad Request when create request payload is invalid")
    void createEvent_returnsBadRequest_WhenRequestIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-400.json");

        MockMultipartFile eventPart = new MockMultipartFile(
                "event", "", "application/json", request.getBytes());

        mockMvc.perform(multipart("/api/v1/events")
                        .file(eventPart).with(user("organizer@email.com").roles("ORGANIZER")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @Order(3)
    @DisplayName("Should return 200 OK and list of events when request is successful")
    void findAll_returnsEvents_WhenSuccessful() throws Exception {
        var response1 = eventUtils.createEventResponse();
        var response2 = eventUtils.createEventResponse();

        when(service.findAllResponse()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/events")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).findAllResponse();
    }

    @Test
    @Order(4)
    @DisplayName("Should return 200 OK and event details when ID exists")
    void findById_returnsEvent_WhenIdExists() throws Exception {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var response = eventUtils.createEventResponse();

        when(service.findByIdOrThrow(event.getId())).thenReturn(event);
        when(service.toEventResponse(event)).thenReturn(response);

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
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        EventResponse updatedEventResponse = EventResponse.builder()
                .id(event.getId())
                .title("Evento atualizado")
                .description("Uma descrição autalizada")
                .date(LocalDateTime.now())
                .location("Campinas").capacity(200)
                .status(EventStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .organizerId(user.getId()).build();

        var request = fileUtils.readResourceFile("event/event-request-200.json").formatted(LocalDateTime.now().plusDays(1));
        request = request.formatted(LocalDateTime.now().plusDays(1));
        var response = fileUtils.readResourceFile("event/event-response-update-200.json");

        when(service.updateEvent(anyLong(), any(), any())).thenReturn(event);
        when(service.updateImageUrl(any())).thenReturn(event);
        when(s3Service.upload(any(), any(), any())).thenReturn("link-s3-falso");
        when(service.toEventResponse(any())).thenReturn(updatedEventResponse);

        MockMultipartFile eventPart = new MockMultipartFile(
                "event", "", "application/json", request.getBytes());

        MockMultipartFile imagePart = new MockMultipartFile(
                "image", "image.png", "image/png", "dados-fake".getBytes());

        String actualJsonResponse = mockMvc.perform(multipart("/api/v1/events/{id}", event.getId())
                                .file(eventPart)
                                .file(imagePart)
                                .with(request1 -> {
                                    request1.setMethod("PUT");
                                    return request1;
                                })
                                .with(user(user.getEmail()).roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(response, actualJsonResponse, new CustomComparator(JSONCompareMode.LENIENT,
                new Customization("date", (_, _) -> true),
                new Customization("createdAt", (_, _) -> true)));
    }

    @Test
    @Order(7)
    @DisplayName("Should return 400 Bad Request when update request payload is invalid")
    void updateEvent_returnsBadRequest_WhenRequestIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("event/event-request-400.json");

        MockMultipartFile eventPart = new MockMultipartFile(
                "event", "", "application/json", request.getBytes());

        mockMvc.perform(multipart("/api/v1/events/{id}", 1L)
                        .file(eventPart)
                        .with(request1 -> {
                            request1.setMethod("PUT");
                            return request1;
                        })
                        .with(user("organizer@email.com").roles("ORGANIZER")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @Order(8)
    @DisplayName("Should return 204 No Content when event deletion is successful")
    void deleteEvent_returnsNoContent_WhenSuccessful() throws Exception {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(service.findByIdOrThrow(1L)).thenReturn(event);

        doNothing().when(service).deleteEvent(eq(event), any(Authentication.class));

        mockMvc.perform(delete("/api/v1/events/1")
                .with(user(user.getEmail())
                .roles("ORGANIZER"))).andExpect(status().isNoContent());

        verify(service).findByIdOrThrow(1L);
        verify(service).deleteEvent(eq(event), any(Authentication.class));
    }
}