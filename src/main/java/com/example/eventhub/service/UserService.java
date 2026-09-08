package com.example.eventhub.service;

import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User createUser(User user){
        assertEmailDoesNotExists(user.getEmail());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return repository.save(user);
    }

    public void assertEmailDoesNotExists(String email){
        if (repository.findByEmail(email).isPresent()){
            throw new IllegalArgumentException("This email already exists");
        }
    }

    public User findByIdOrThrow(Long id){
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
    }
}
