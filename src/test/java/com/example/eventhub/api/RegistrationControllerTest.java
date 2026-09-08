package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.commons.RegistrationUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.Registration;
import com.example.eventhub.domain.User;
import com.example.eventhub.mapper.RegistrationMapper;
import com.example.eventhub.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ComponentScan(basePackages = {"com.Matheus.AuthBank"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(FileUtils.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService service;

    @MockitoBean
    private RegistrationMapper mapper;

    private final UserUtils userUtils = new UserUtils();
    private final EventUtils eventUtils = new EventUtils();
    private final RegistrationUtils registrationUtils = new RegistrationUtils();

    @Test
    @Order(1)
    @DisplayName("Should return 201 Created and registration details when user registers for an event successfully")
    void register_returnsCreated_WhenSuccessful() throws Exception {
        // This test is unavailable because the security features have not yet been implemented, this will be fixed in the next update.
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);
        Registration registration = registrationUtils.createSavedRegistration(user, event);

        var response = registrationUtils.createRegistrationResponse(user.getId(), event.getId());

        when(service.registerUser(any(), any())).thenReturn(registration);
        when(mapper.toRegistrationResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/events/{eventId}/registrations", event.getId())
                        .param("userId", user.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));

        verify(service).registerUser(user.getId(), event.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should return 200 OK and list of registrations when searching by event ID")
    void findByEvent_returnsRegistrations_WhenSuccessful() throws Exception {
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);

        Registration registration = registrationUtils.createSavedRegistration(user, event);

        var registrations = List.of(registration);

        var response = registrationUtils.createRegistrationResponse(user.getId(), event.getId());

        when(service.findByEvent(event.getId())).thenReturn(registrations);
        when(mapper.toRegistrationResponseList(registrations)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/events/{eventId}/registrations", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(event.getId()));

        verify(service).findByEvent(event.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Should return 204 No Content when registration cancellation is successful")
    void cancelRegistration_returnsNoContent_WhenSuccessful() throws Exception {
        doNothing().when(service).cancelRegistration(1L, 1L);

        mockMvc.perform(delete("/api/v1/events/1/registrations")
                .param("userId", "1")).andExpect(status().isNoContent());

        verify(service).cancelRegistration(1L, 1L);
    }
}