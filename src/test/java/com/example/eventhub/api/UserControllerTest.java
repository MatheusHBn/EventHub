package com.example.eventhub.api;

import com.example.eventhub.commons.FileUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.config.SecurityConfig;
import com.example.eventhub.dto.user.UserRegisterRequest;
import com.example.eventhub.exception.GlobalErrorHandler;
import com.example.eventhub.mapper.UserMapper;
import com.example.eventhub.security.CustomAccessDeniedHandler;
import com.example.eventhub.security.CustomAuthenticationEntryPoint;
import com.example.eventhub.security.CustomUserDetailsService;
import com.example.eventhub.security.JwtService;
import com.example.eventhub.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({
        FileUtils.class,
        UserUtils.class,
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalErrorHandler.class
})
@ActiveProfiles("test")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private UserMapper mapper;

    @Autowired
    private UserUtils userUtils;
    @Autowired
    private FileUtils fileUtils;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @Order(1)
    @DisplayName("Should return 201 Created and registered user details when registration payload is valid")
    void register_returnsCreated_WhenSuccessful() throws Exception {
        var user = userUtils.createSavedUser();
        var response = userUtils.createUserResponse();
        var jsonRequest = fileUtils.readResourceFile("user/user-request-200.json");

        when(mapper.toUser(any(UserRegisterRequest.class))).thenReturn(user);
        when(service.createUser(user)).thenReturn(user);
        when(mapper.toUserResponse(user)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Matheus"))
                .andExpect(jsonPath("$.email").value("matheus@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(service).createUser(user);
    }

    @Test
    @Order(2)
    @DisplayName("Should return 400 Bad Request when registration payload is invalid")
    void register_returnsBadRequest_WhenRequestIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("user/user-request-400.json");

        mockMvc.perform(post("/api/v1/auth/register").contentType("application/json").content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}