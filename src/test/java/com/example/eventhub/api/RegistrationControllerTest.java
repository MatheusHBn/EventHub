package com.example.eventhub.api;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.RegistrationUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.config.SecurityConfig;
import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.Registration;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.registration.RegistrationResponse;
import com.example.eventhub.exception.GlobalErrorHandler;
import com.example.eventhub.mapper.RegistrationMapper;
import com.example.eventhub.security.CustomAccessDeniedHandler;
import com.example.eventhub.security.CustomAuthenticationEntryPoint;
import com.example.eventhub.security.CustomUserDetailsService;
import com.example.eventhub.security.JwtService;
import com.example.eventhub.service.RegistrationService;
import com.example.eventhub.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalErrorHandler.class
})
@ActiveProfiles("test")
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService service;

    @MockitoBean
    private RegistrationMapper mapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private final UserUtils userUtils = new UserUtils();
    private final EventUtils eventUtils = new EventUtils();
    private final RegistrationUtils registrationUtils = new RegistrationUtils();

    @Test
    @Order(1)
    @DisplayName("POST /events/{id}/registrations Should return 401 Unauthorized when not authenticated")
    void register_returns401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/events/1/registrations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    @Order(2)
    @DisplayName("POST /events/{id}/registrations Should return 201 Created for USER role")
    void register_returns201_WhenUserIsRoleUser() throws Exception {
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);
        Registration registration = registrationUtils.createSavedRegistration(user, event);
        var response = registrationUtils.createRegistrationResponse(user.getId(), event.getId());

        when(userService.findByEmailOrThrow(user.getEmail())).thenReturn(user);
        when(service.registerUser(user.getId(), event.getId())).thenReturn(registration);
        when(mapper.toRegistrationResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/events/{eventId}/registrations", event.getId())
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));

        verify(service).registerUser(user.getId(), event.getId());
    }

    @Test
    @Order(3)
    @DisplayName("GET /events/{id}/registrations Should return 200 OK for authenticated user")
    void findByEvent_returns200_WhenAuthenticated() throws Exception {
        List<Registration> registrations = List.of(new Registration());
        List<RegistrationResponse> responses = List.of(RegistrationResponse.builder().id(1L).userId(1L).eventId(1L).build());

        when(service.findByEvent(1L)).thenReturn(registrations);
        when(mapper.toRegistrationResponseList(registrations)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/events/1/registrations")
                .with(user("organizer@email.com").roles("ORGANIZER"))).andExpect(status().isOk());

        verify(service).findByEvent(1L);
    }

    @Test
    @Order(4)
    @DisplayName("GET /events/{id}/registrations Should return 200 OK for ORGANIZER role")
    void findByEvent_returns200_WhenUserIsOrganizer() throws Exception {
        User user = userUtils.createSavedUser();
        Event event = eventUtils.createSavedEvent(user);
        Registration registration = registrationUtils.createSavedRegistration(user, event);

        var registrations = List.of(registration);
        var response = registrationUtils.createRegistrationResponse(user.getId(), event.getId());

        when(service.findByEvent(event.getId())).thenReturn(registrations);
        when(mapper.toRegistrationResponseList(registrations)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/events/{eventId}/registrations", event.getId())
                        .with(user("organizer@email.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(event.getId()));

        verify(service).findByEvent(event.getId());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /events/{id}/registrations Should return 401 Unauthorized when not authenticated")
    void cancelRegistration_returns401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/events/1/registrations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /events/{eventId}/registrations Should return 204 No Content for authenticated user")
    void cancelRegistration_returns204_WhenUserIsRoleUser() throws Exception {
        User user = userUtils.createSavedUser();

        when(userService.findByEmailOrThrow(user.getEmail())).thenReturn(user);
        doNothing().when(service).cancelRegistration(user.getId(), 1L);

        mockMvc.perform(delete("/api/v1/events/1/registrations")
                        .with(user(user.getEmail()).authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isNoContent());

        verify(userService).findByEmailOrThrow(user.getEmail());
        verify(service).cancelRegistration(user.getId(), 1L);
    }
}