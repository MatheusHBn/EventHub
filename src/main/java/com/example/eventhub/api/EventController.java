package com.example.eventhub.api;

import com.example.eventhub.domain.Event;
import com.example.eventhub.dto.event.EventCreateForm;
import com.example.eventhub.dto.event.EventCreateMultipartRequest;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.exception.DefaultErrorMessage;
import com.example.eventhub.mapper.EventMapper;
import com.example.eventhub.service.EventService;
import com.example.eventhub.service.S3Service;
import com.example.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for event management")
public class EventController {

    private final UserService userService;
    private final EventService service;
    private final EventMapper mapper;
    private final S3Service s3Service;

    @Operation(summary = "Create event", description = "Creates a new event with an initial status of DRAFT.")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = EventCreateMultipartRequest.class),
                    encoding = { @Encoding(name = "event", contentType = MediaType.APPLICATION_JSON_VALUE)}))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid event data", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DefaultErrorMessage.class),
                            examples = @ExampleObject(
                                    value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultErrorMessage.class), examples = @ExampleObject(
                                    value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to create events",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DefaultErrorMessage.class),
                            examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"You don't have authorization\"}")))
    })
    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvents(@Valid @RequestPart("event") EventCreateForm request,
                                                      @RequestPart(value = "image", required = false) MultipartFile image,
                                                      Authentication authentication) throws IOException {
        var event = mapper.toEvent(request);
        String email = authentication.getName();
        var user = userService.findByEmailOrThrow(email);
        event.setOrganizer(user);

        var eventSaved = service.createEvent(event);
        String imageKey = s3Service.upload(eventSaved.getId(), image.getBytes(), image.getContentType());

        eventSaved.setImageUrl(imageKey);
        eventSaved = service.updateImageUrl(eventSaved);

        var eventResponse = mapper.toEventResponse(eventSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
    }

    @Operation(summary = "Publish event", description = "Changes an event status from DRAFT to PUBLISHED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event successfully published"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid event data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to create events", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"You don't have authorization\"}"))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}")))
    })
    @PatchMapping("/events/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id, Authentication authentication) {
        Event event = service.publishEvent(id, authentication);
        return ResponseEntity.ok(mapper.toEventResponse(event));
    }

    @Operation(summary = "List events", description = "Returns all registered events")
    @ApiResponse(responseCode = "200", description = "Events successfully found")
    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> findAllEvents() {
        var eventsResponse = service.findAllResponse();

        return ResponseEntity.ok(eventsResponse);
    }

    @Operation(summary = "Search for an event by ID", description = "Returns data for a specific event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}")))
    })

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        var event = service.findByIdOrThrow(id);
        return ResponseEntity.ok(service.toEventResponse(event));
    }

    @Operation(summary = "Update event", description = "Updates the data of an existing event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event successfully updated"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid event data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to update this event", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"You don't have authorization\"}"))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}")))
    })
    @PutMapping(value = "/events/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> updateEventsById(@PathVariable Long id,
                                                          @Valid @RequestPart("event") EventUpdateRequest request,
                                                          @RequestPart(value = "image", required = false) MultipartFile image,
                                                          Authentication authentication) throws IOException {

        Event updatedEvent = service.updateEvent(id, request, authentication);

        if (image != null && !image.isEmpty()) {
            String imageKey = s3Service.upload(updatedEvent.getId(), image.getBytes(), image.getContentType());

            updatedEvent.setImageUrl(imageKey);
            updatedEvent = service.updateImageUrl(updatedEvent);
        }

        return ResponseEntity.ok(service.toEventResponse(updatedEvent));
    }

    @Operation(summary = "Cancel event",
            description = "Cancels an event by changing its status to \"CANCELED\". The record is not physically removed from the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event successfully cancelled"),
            @ApiResponse(
                    responseCode = "400", description = "Invalid event data", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Invalid request data\"}"))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to cancel this event", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"You don't have authorization\"}"))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = DefaultErrorMessage.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Event not found\"}")))
    })
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEventsById(@PathVariable Long id, Authentication authentication) {
        var event = service.findByIdOrThrow(id);

        service.deleteEvent(event, authentication);

        return ResponseEntity.noContent().build();
    }
}
