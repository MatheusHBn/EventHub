package com.example.eventhub.service;

import com.example.eventhub.domain.*;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;
    private final UserService userService;
    private final EventService eventService;

    public Registration registerUser(Long userId, Long eventId) {
        User user = userService.findByIdOrThrow(userId);
        Event event = eventService.findByIdOrThrow(eventId);

        if (repository.existsByUser_IdAndEvent_Id(userId, eventId)) {
            throw new IllegalArgumentException("User already registered");
        }

        if (event.getStatus() != EventStatus.PUBLISHED){
            throw new IllegalArgumentException("Event is not available for registration");
        }

        long registrations = repository.countByEventIdAndStatus(eventId, RegistrationStatus.ACTIVE);

        if (registrations >= event.getCapacity()) {
            throw new IllegalArgumentException("Event is full");
        }

        Registration registration = Registration.builder().user(user).event(event).status(RegistrationStatus.ACTIVE).build();

        return repository.save(registration);
    }

    public List<Registration> findByEvent(Long eventId) {
        return repository.findByEventId(eventId);
    }

    public List<Registration> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public void cancelRegistration(Long userId, Long eventId) {

        var registration = repository.findByUserIdAndEventId(userId, eventId).orElseThrow(
                () -> new NotFoundException("Registration not found"));

        if (registration.getStatus() == RegistrationStatus.CANCELED){
            throw new IllegalArgumentException("This registration already canceled");
        }

        registration.setStatus(RegistrationStatus.CANCELED);
        repository.save(registration);
    }
}
