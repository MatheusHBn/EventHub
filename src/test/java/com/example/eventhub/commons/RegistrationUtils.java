package com.example.eventhub.commons;

import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.Registration;
import com.example.eventhub.domain.RegistrationStatus;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.registration.RegistrationResponse;
import com.example.eventhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegistrationUtils {

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private UserRepository userRepository;

    public List<Registration> newRegistrationList(User user1, User user2, Event event1, Event event2) {
        var registration = Registration.builder().id(1L).user(user1).event(event1)
                .status(RegistrationStatus.ACTIVE).registeredAt(LocalDateTime.now().plusDays(2)).build();
        var registration2 = Registration.builder().id(1L).user(user2).event(event2)
                .status(RegistrationStatus.ACTIVE).registeredAt(LocalDateTime.now().plusDays(3)).build();

        return new ArrayList<>(List.of(registration, registration2));
    }

    public Registration createRegistration(User user, Event event) {
        return Registration.builder().user(user).event(event).status(RegistrationStatus.ACTIVE)
                .registeredAt(LocalDateTime.now().plusMinutes(21)).build();
    }

    public Registration createSavedRegistration(User user, Event event) {
        return Registration.builder().id(1L).user(user).event(event).status(RegistrationStatus.ACTIVE)
                .registeredAt(LocalDateTime.now().plusMinutes(28)).build();
    }

    public RegistrationResponse createRegistrationResponse(Long userId, Long eventId) {
        return RegistrationResponse.builder().id(1L).eventId(eventId).userId(userId)
                .status(RegistrationStatus.ACTIVE).registeredAt(LocalDateTime.now().plusDays(2)).build();
    }


}
