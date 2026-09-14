package com.example.eventhub.service;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.RegistrationUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.Registration;
import com.example.eventhub.domain.RegistrationStatus;
import com.example.eventhub.exception.*;
import com.example.eventhub.repository.RegistrationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class RegistrationServiceTest {

    @InjectMocks
    private RegistrationService service;
    @Mock
    private RegistrationRepository repository;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;

    private RegistrationUtils registrationUtils;
    private UserUtils userUtils;
    private EventUtils eventUtils;

    @BeforeEach
    void setUp() {
        registrationUtils = new RegistrationUtils();
        eventUtils = new EventUtils();
        userUtils = new UserUtils();
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully register user to event and set registration status to ACTIVE")
    void register_registerUser_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var registration = registrationUtils.createRegistration(user, event);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenReturn(event);

        when(repository.save(any(Registration.class))).thenReturn(registration);

        var result = service.registerUser(user.getId(), event.getId());

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getStatus()).isEqualByComparingTo(RegistrationStatus.ACTIVE);
        assertThat(result.getRegisteredAt()).isNotNull();

        verify(repository, times(1)).save(any(Registration.class));
    }

    @Test
    @Order(2)
    @DisplayName("Should throw NotFoundException when registering with a non-existing user ID")
    void findByUserId_throwsNotFoundException_WhenUserIdDoesNotExists() {
        var user = userUtils.createUser();
        var eventId = 1L;

        when(userService.findByIdOrThrow(user.getId())).thenThrow(new NotFoundException("User not found"));

        assertThatThrownBy(() -> service.registerUser(user.getId(), eventId)).isInstanceOf(NotFoundException.class);

        verify(userService, times(1)).findByIdOrThrow(user.getId());
        verify(eventService, never()).findByIdOrThrow(any());
        verify(repository, never()).save(any());

    }

    @Test
    @Order(3)
    @DisplayName("Should throw NotFoundException when registering for a non-existing event ID")
    void findByEventId_throwsNotFoundException_WhenEventIdDoesNotExists() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenThrow(new NotFoundException("Event not found"));

        assertThatThrownBy(() -> service.registerUser(user.getId(), event.getId())).isInstanceOf(NotFoundException.class);

        verify(userService).findByIdOrThrow(user.getId());
        verify(eventService).findByIdOrThrow(event.getId());
        verify(repository, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("Should throw AlreadyRegisteredException when user is already registered for the event")
    void existsByUserIdAndEventId_ThrowsAlreadyRegisteredException_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenReturn(event);

        when(repository.existsByUser_IdAndEvent_Id(user.getId(), event.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(user.getId(), event.getId())).isInstanceOf(AlreadyRegisteredException.class);
    }

    @Test
    @Order(5)
    @DisplayName("Should throw EventIsNotPublishedException when attempting to register for an unpublished event")
    void registerUser_ThrowsEventIsNotPublishedException_WhenEventIsNotPublished() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        event.setStatus(EventStatus.DRAFT);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenReturn(event);

        assertThatThrownBy(() -> service.registerUser(user.getId(), event.getId())).isInstanceOf(EventIsNotPublishedException.class);
    }

    @Test
    @Order(6)
    @DisplayName("Should throw EventFullException when event capacity is full")
    void registerUser_ThrowsEventFullException_WhenEventsCapacityIsFully() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        event.setCapacity(19);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenReturn(event);
        when(repository.countByEventIdAndStatus(event.getId(), RegistrationStatus.ACTIVE)).thenReturn(20L);

        assertThatThrownBy(() -> service.registerUser(user.getId(), event.getId())).isInstanceOf(EventFullException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Should save registration successfully when event has available capacity")
    void registerUser_SavesRegistration_WhenHasCapacity() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var registration = registrationUtils.createRegistration(user, event);
        event.setCapacity(200);

        when(userService.findByIdOrThrow(user.getId())).thenReturn(user);
        when(eventService.findByIdOrThrow(event.getId())).thenReturn(event);
        when(repository.existsByUser_IdAndEvent_Id(user.getId(), event.getId())).thenReturn(false);
        when(repository.countByEventIdAndStatus(event.getId(), RegistrationStatus.ACTIVE)).thenReturn(20L);

        when(repository.save(any(Registration.class))).thenReturn(registration);

        var result = service.registerUser(user.getId(), event.getId());

        assertThat(result).isNotNull();
        assertThat(result.getEvent().getCapacity()).isEqualTo(200);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getStatus()).isEqualByComparingTo(RegistrationStatus.ACTIVE);

        verify(repository, times(1)).save(any(Registration.class));
    }

    @Test
    @Order(8)
    @DisplayName("Should return all registrations associated with a specific event ID")
    void findByEventId_findAllRegistrationsByEventId_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var user2 = userUtils.createSavedUser();
        var event1 = eventUtils.createSavedEvent(user);
        var event2 = eventUtils.createSavedEvent(user2);
        var registrationList = registrationUtils.newRegistrationList(user, user2, event1, event2);

        when(repository.findByEventId(1L)).thenReturn(registrationList);

        var result = service.findByEvent(1L);

        assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    @Order(9)
    @DisplayName("Should cancel registration successfully by updating status to CANCELED")
    void cancelEvent_cancelEvent_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var registration = registrationUtils.createSavedRegistration(user, event);

        when(repository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));
        when(repository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelRegistration(user.getId(), event.getId());

        assertThat(registration.getStatus()).isEqualByComparingTo(RegistrationStatus.CANCELED);
        verify(repository, times(1)).save(registration);
    }

    @Test
    @Order(10)
    @DisplayName("Should throw NotFoundException when attempting to cancel a non-existing registration")
    void cancelEvent_throwsNotFoundException_WhenEventIsNotFound() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var registration = registrationUtils.createSavedRegistration(user, event);

        when(repository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelRegistration(user.getId(), event.getId())).isInstanceOf(NotFoundException.class);

        verify(repository, times(0)).save(registration);
    }

    @Test
    @Order(11)
    @DisplayName("Should throw AlreadyCanceledException when attempting to cancel an already canceled registration")
    void cancelEvent_throwsAlreadyCanceledException_WhenRegistrationAlreadyCanceled() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        var registration = registrationUtils.createSavedRegistration(user, event);
        registration.setStatus(RegistrationStatus.CANCELED);

        when(repository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.cancelRegistration(user.getId(), event.getId()))
                .isInstanceOf(AlreadyCanceledException.class).hasMessageContaining("This registration already canceled");

        verify(repository, times(0)).save(registration);
    }
}