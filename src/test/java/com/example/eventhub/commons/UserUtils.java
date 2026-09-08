package com.example.eventhub.commons;

import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.user.UserRegisterRequest;
import com.example.eventhub.dto.user.UserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserUtils {

    public List<User> newUserList(){
        var user1 = User.builder().id(1L).name("Matheus").email("testeMatheus123@gmail.com").createdAt(LocalDateTime.now())
                .password("matheus-teste").role(Role.USER).build();
        var user2 = User.builder().id(2L).name("Joana").email("testeJoana123@gmail.com").createdAt(LocalDateTime.now().plusSeconds(20))
                .password("joana-teste").role(Role.USER).build();
        var user3 = User.builder().id(3L).name("Vitor").email("testeVitor123@gmail.com").createdAt(LocalDateTime.now().plusMinutes(28))
                .password("vitor-teste").role(Role.USER).build();

        return new ArrayList<>(List.of(user1, user2, user3));
    }

    public User createUser() {
        return User.builder().name("Thiago").email("testeThiago123@gmail.com").createdAt(LocalDateTime.now())
                .password("thiago-teste").role(Role.USER).build();
    }

    public User createSavedUser() {
        return User.builder().id(1L).name("Marcos").email("testeMarcos123@gmail.com").createdAt(LocalDateTime.now())
                .password("marcos-teste").role(Role.USER).build();
    }

    public UserRegisterRequest createUserRequest(){
        return UserRegisterRequest.builder().name("Matheus").email("matheushbn@gmail.com").password("123456").build();
    }

    public UserResponse createUserResponse(){
        return UserResponse.builder().id(1L).name("Matheus").email("matheus@email.com").role(Role.USER).build();
    }
}
