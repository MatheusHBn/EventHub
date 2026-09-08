package com.example.eventhub.api;

import com.example.eventhub.domain.Event;
import com.example.eventhub.dto.event.EventCreateRequest;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.mapper.EventMapper;
import com.example.eventhub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for event management")
public class EventController {

    private final EventService service;
    private final EventMapper mapper;


    @Operation(summary = "Create event", description = "Creates a new event with an initial status of DRAFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvents(@Valid @RequestBody EventCreateRequest request) {

        // OrganizerId appears as null, this will be resolved using security + JWT.
        var event = mapper.toEvent(request);
        var eventSaved = service.createEvent(event);
        var eventResponse = mapper.toEventResponse(eventSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
    }

    @Operation(summary = "List events", description = "Returns all registered events")
    @ApiResponse(responseCode = "200", description = "Events successfully found")
    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> findAllEvents() {
        var events = service.findAll();
        var eventsResponse = mapper.toEventResponseList(events);

        return ResponseEntity.ok(eventsResponse);
    }

    @Operation(summary = "Search for an event by ID", description = "Returns data for a specific event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found"
            )})

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEventsById(@PathVariable Long id) {
        var event = service.findByIdOrThrow(id);
        var eventResponse = mapper.toEventResponse(event);

        return ResponseEntity.ok(eventResponse);
    }

    @Operation(summary = "Update event", description = "Updates the data of an existing event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEventsById(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        Event updatedEvent = service.updateEvent(id, request);

        return ResponseEntity.ok(mapper.toEventResponse(updatedEvent));
    }

    @Operation(summary = "Cancel event",
            description = "Cancels an event by changing its status to CANCELED. The record is not physically removed from the database")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event successfully cancelled"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEventsById(@PathVariable Long id) {
        var event = service.findByIdOrThrow(id);
        service.deleteEvent(event);

        return ResponseEntity.noContent().build();
    }

}
