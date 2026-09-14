package com.example.eventhub.api;

import com.example.eventhub.dto.user.UserRegisterRequest;
import com.example.eventhub.dto.user.UserResponse;
import com.example.eventhub.exception.DefaultErrorMessage;
import com.example.eventhub.mapper.UserMapper;
import com.example.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Endpoints for user registration and management")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserMapper mapper;

    @Operation(summary = "Register user", description = "Creates a new user account in EventHub")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid event data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),
            @ApiResponse(responseCode = "409", description = "Email is already registered", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 409, \"message\": \"This email already exists\"}")))
    })
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        var user = mapper.toUser(request);

        var userSaved = service.createUser(user);

        var userResponse = mapper.toUserResponse(userSaved);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "Get authenticated user", description = "Returns data for the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully found"),
            @ApiResponse(
                    responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}")))
    })
    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        String email = authentication.getName();
        var user = service.findByEmailOrThrow(email);
        var response = mapper.toUserResponse(user);
        return ResponseEntity.ok(response);
    }
}
