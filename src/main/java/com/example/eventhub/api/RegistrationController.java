package com.example.eventhub.api;

import com.example.eventhub.domain.Registration;
import com.example.eventhub.dto.registration.RegistrationResponse;
import com.example.eventhub.exception.DefaultErrorMessage;
import com.example.eventhub.mapper.RegistrationMapper;
import com.example.eventhub.service.RegistrationService;
import com.example.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "Endpoints for event registration management")
public class RegistrationController {

    private final RegistrationService service;
    private final RegistrationMapper mapper;
    private final UserService userService;

    @Operation(summary = "Register for an event", description = "Registers the authenticated user for a published event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successfully created"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid request data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Event is not available for registration\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}"))),
            @ApiResponse(responseCode = "409", description = "User is already registered or event is full", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 409, \"message\": \"User already registered / Event is full\"}")))
    })
    @PostMapping("/events/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(@PathVariable Long eventId, Authentication authentication) {
        String email = authentication.getName();
        var user = userService.findByEmailOrThrow(email);
        var registration = service.registerUser(user.getId(), eventId);
        var response = mapper.toRegistrationResponse(registration);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List my registrations", description = "Returns all registrations belonging to the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrations successfully found"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}")))
    })
    @GetMapping("/users/me/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistration(Authentication authentication) {
        String email = authentication.getName();
        var user = userService.findByEmailOrThrow(email);
        var registrations = service.findByUserId(user.getId());
        var response = mapper.toRegistrationResponseList(registrations);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List event registrations",
            description = "Returns all registrations made for a specific event. Requires \"ORGANIZER\" or \"ADMIN\" role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration successfully created"),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to view event registrations",
                    content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"You don't have authorization\"}"))),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}"))),

    })
    @GetMapping("/events/{eventId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationByEvent(@PathVariable Long eventId) {
        var registrations = service.findByEvent(eventId);
        var response = mapper.toRegistrationResponseList(registrations);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel registration",
            description = "Cancels a user's registration for an event. The registration is not physically removed from the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registration successfully cancelled"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid Request Data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"This registration already canceled\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "Registration not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Registration not found\"}")))
    })
    @DeleteMapping("/events/{eventId}/registrations")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId, Authentication authentication) {
        String email = authentication.getName();
        var user = userService.findByEmailOrThrow(email);
        service.cancelRegistration(user.getId(), eventId);
        return ResponseEntity.noContent().build();
    }
}
