package com.example.eventhub.api;

import com.example.eventhub.domain.Registration;
import com.example.eventhub.dto.registration.RegistrationResponse;
import com.example.eventhub.mapper.RegistrationMapper;
import com.example.eventhub.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "Endpoints for event registration management")
public class RegistrationController {

    private final RegistrationService service;
    private final RegistrationMapper mapper;

    @Operation(summary = "Register", description = "Registers a user for a published event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Event unavailable, duplicate registration, or event full"),
            @ApiResponse(responseCode = "404", description = "User or event not found")
    })
    @PostMapping("/events/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(@PathVariable Long eventId) {
        // TODO: implement Spring Security + JWT
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @Operation(summary = "List registrations", description = "Returns all registrations for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrations found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/users/me/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistration() {
        // TODO: implement Spring Security + JWT
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "List event registrations",
            description = "Returns all registrations made for a specific event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrations found"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have permission to view the registrations")
    })
    @GetMapping("/events/{eventId}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationByEvent(@PathVariable Long eventId) {
        List<Registration> registrations = service.findByEvent(eventId);

        List<RegistrationResponse> response = mapper.toRegistrationResponseList(registrations);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel registration",
            description = "Cancels a user's registration for an event. The registration is not physically removed from the database")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registration successfully cancelled"),
            @ApiResponse(responseCode = "400", description = "Registration is already cancelled"),
            @ApiResponse(responseCode = "404", description = "Registration not found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @DeleteMapping("/events/{eventId}/registrations")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId, @RequestParam Long userId) {
        service.cancelRegistration(userId, eventId);

        return ResponseEntity.noContent().build();
    }

}
