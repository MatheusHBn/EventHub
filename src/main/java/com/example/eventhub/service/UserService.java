package com.example.eventhub.service;

import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.exception.EmailAlreadyExistsException;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        assertEmailDoesNotExists(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        return repository.save(user);
    }

    public void assertEmailDoesNotExists(String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("This email already exists");
        }
    }

    public User findByIdOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
    }

    public User findByEmailOrThrow(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
