package com.example.eventhub.api;

import com.example.eventhub.dto.user.LoginRequest;
import com.example.eventhub.dto.user.LoginResponse;
import com.example.eventhub.exception.DefaultErrorMessage;
import com.example.eventhub.security.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints responsible for user authentication")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user using email and password and returns a JWT token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = "{\"token\": \"eywdwkjdwjkdwdkjbwdbwibi232323kbdkhbdqwhdbqwhdb\" }"))),
            @ApiResponse(
                    responseCode = "400", description = "Invalid login data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),

            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}