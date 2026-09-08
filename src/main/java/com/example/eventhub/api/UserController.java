package com.example.eventhub.api;

import com.example.eventhub.dto.user.LoginRequest;
import com.example.eventhub.dto.user.LoginResponse;
import com.example.eventhub.dto.user.UserRegisterRequest;
import com.example.eventhub.dto.user.UserResponse;
import com.example.eventhub.mapper.UserMapper;
import com.example.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @ApiResponse(responseCode = "400", description = "Invalid data or email already registered")
    })
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        var user = mapper.toUser(request);

        var userSaved = service.createUser(user);

        var userResponse = mapper.toUserResponse(userSaved);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid login data"),
            @ApiResponse(responseCode = "401", description = "Incorrect email or password")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // TODO: implement Spring Security + JWT
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Get authenticated user", description = "Returns data for the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe() {
        // TODO: implement Spring Security + JWT
        return ResponseEntity.ok(null);
    }
}
