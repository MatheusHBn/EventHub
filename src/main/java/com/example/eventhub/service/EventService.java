package com.example.eventhub.service;

import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;

    public Event createEvent(Event event){
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }

    public List<Event> findAll(){
        return repository.findAll();
    }

    public Event findByIdOrThrow(Long id){
        return repository.findById(id).orElseThrow(() -> new NotFoundException("This id not found"));
    }

    public Event updateEvent(Long id, EventUpdateRequest request) {
        Event event = findByIdOrThrow(id);

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setCapacity(request.capacity());

        return repository.save(event);
    }

    public void deleteEvent(Event event){
        findByIdOrThrow(event.getId());
        event.setStatus(EventStatus.CANCELED);
        repository.save(event);
    }
}
