package com.example.eventhub.service;

import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.dto.event.EventResponse;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.mapper.EventMapper;
import com.example.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final S3Service s3Service;
    private final EventMapper mapper;

    public Event createEvent(Event event) {
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }

    public Event publishEvent(Long id, Authentication authentication) {
        Event event = findByIdOrThrow(id);

        validateOwnership(event, authentication);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);
        return repository.save(event);
    }

    public List<Event> findAll() {
        return repository.findAll();
    }

    public List<EventResponse> findAllResponse() {
        return repository.findAll().stream().map(this::toEventResponse).toList();
    }

    public Event findByIdOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("This id not found"));
    }

    public Event updateEvent(Long id, EventUpdateRequest request, Authentication authentication) {
        Event event = findByIdOrThrow(id);

        validateOwnership(event, authentication);

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setCapacity(request.capacity());

        return repository.save(event);
    }

    public void deleteEvent(Event event, Authentication authentication) {
        validateOwnership(event, authentication);
        event.setStatus(EventStatus.CANCELED);

        repository.save(event);
    }

    private void validateOwnership(Event event, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> Objects.requireNonNull(authority.getAuthority()).equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String email = authentication.getName();

        if (!event.getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this event");
        }
    }

    public Event updateImageUrl(Event event) {
        return repository.save(event);
    }

    public EventResponse toEventResponse(Event event) {
        var response = mapper.toEventResponse(event);

        if (event.getImageUrl() == null) {
            return response;
        }

        return EventResponse.builder()
                .id(response.id())
                .title(response.title())
                .description(response.description())
                .date(response.date())
                .location(response.location())
                .capacity(response.capacity())
                .status(response.status())
                .createdAt(response.createdAt())
                .organizerId(response.organizerId())
                .imageUrl(s3Service.generatePresignedUrl(event.getImageUrl()))
                .build();
    }
}
