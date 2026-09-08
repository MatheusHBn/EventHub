package com.example.eventhub.repository;

import com.example.eventhub.domain.Registration;
import com.example.eventhub.domain.RegistrationStatus;
import com.example.eventhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    Long user(User user);

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByUserId(Long userId);

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
}
