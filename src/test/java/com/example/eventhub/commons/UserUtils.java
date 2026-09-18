package com.example.eventhub.commons;

import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.user.UserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserUtils {
    public User createUser() {
        return User.builder().name("Thiago").email("testeThiago123@gmail.com").createdAt(LocalDateTime.now())
                .password("thiago-teste").role(Role.USER).build();
    }

    public User createSavedUser() {
        return User.builder().id(1L).name("Marcos").email("testeMarcos123@gmail.com").createdAt(LocalDateTime.now())
                .password("marcos-teste").role(Role.USER).build();
    }

    public UserResponse createUserResponse() {
        return UserResponse.builder().id(1L).name("Matheus").email("matheus@email.com").role(Role.USER).build();
    }
}
