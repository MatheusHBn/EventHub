package com.example.eventhub.service;

import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.domain.Role;
import com.example.eventhub.domain.User;
import com.example.eventhub.exception.EmailAlreadyExistsException;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class UserServiceTest {

    @InjectMocks
    private UserService service;
    @Mock
    private UserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private UserUtils userUtils;

    @BeforeEach
    void setUp() {
        userUtils = new UserUtils();
    }

    @Test
    @Order(1)
    @DisplayName("Should create user successfully with default USER role and set creation date")
    void createUser_createsUser_WhenSuccessful() {
        var user = User.builder().name("Matheus").email("testeMatheus123@gmail.com")
                .password(passwordEncoder.encode("teste-matheus")).build();

        when(repository.save(user)).thenReturn(user);

        var result = service.createUser(user);

        assertThat(result).isEqualTo(user);
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getCreatedAt()).isNotNull();

        verify(repository).save(user);
    }

    @Test
    @Order(2)
    @DisplayName("Should throw NotFoundException when user ID does not exist")
    void findById_throwsNotFoundException_whenUserDoesNotExists() {
        var user = userUtils.createSavedUser();

        when(repository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIdOrThrow(user.getId())).isInstanceOf(NotFoundException.class);

        then(repository).should().findById(user.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Should not throw exception and save user when email is not registered")
    void findByEmail_notThrowsException_whenEmailDoesNotExists() {
        var user = userUtils.createUser();

        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(user);

        assertThatNoException().isThrownBy(() -> service.createUser(user));
        then(repository).should().save(user);
    }

    @Test
    @Order(4)
    @DisplayName("Should throw IllegalArgumentException when email is already registered")
    void assertEmailDoesNotExists_throwsException_WhenEmailAlreadyExists() {
        var user = userUtils.createUser();

        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.assertEmailDoesNotExists(user.getEmail()))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(repository).findByEmail(user.getEmail());
    }

}