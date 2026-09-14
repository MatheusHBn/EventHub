package com.example.eventhub.service;

import com.example.eventhub.commons.EventUtils;
import com.example.eventhub.commons.UserUtils;
import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.Role;
import com.example.eventhub.dto.event.EventUpdateRequest;
import com.example.eventhub.exception.NotFoundException;
import com.example.eventhub.repository.EventRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class EventServiceTest {

    @InjectMocks
    private EventService service;
    @Mock
    private EventRepository repository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    private UserUtils userUtils;
    private EventUtils eventUtils;

    @BeforeEach
    void setUp() {
        eventUtils = new EventUtils();
        userUtils = new UserUtils();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new event with DRAFT status and set creation date when successful")
    void createEvent_CreateNewEvent_WhenSuccessful() {
        var user = userUtils.createUser();
        var event = eventUtils.createEvent(user);

        when(repository.save(event)).thenReturn(event);

        var result = service.createEvent(event);

        assertThat(result.getStatus()).isEqualByComparingTo(EventStatus.DRAFT);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(repository).save(event);
    }

    @Test
    @Order(2)
    @DisplayName("Should return a list of all events when successful")
    void findAll_findAllUsers_WhenSuccessful() {
        var user = userUtils.createUser();
        var user2 = userUtils.createUser();
        var eventList = eventUtils.newEventList(user, user2);

        when(repository.findAll()).thenReturn(eventList);

        var result = service.findAll();

        assertThat(result).hasSize(2);
        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    @Order(3)
    @DisplayName("Should return event by ID when event exists")
    void findById_FindsEventById_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        var result = service.findByIdOrThrow(event.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());

        verify(repository, times(1)).findById(event.getId());
    }

    @Test
    @Order(4)
    @DisplayName("Should throw NotFoundException when searching for a non-existing event ID")
    void findById_ThrowsNotFoundException_WhenEventsIdNotFound() {
        var eventId = 999L;

        when(repository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIdOrThrow(eventId)).isInstanceOf(NotFoundException.class);

        verify(repository, times(1)).findById(eventId);
    }

    @Test
    @Order(5)
    @DisplayName("Should update event details and set status to PUBLISHED when successful")
    void updateEvent_updatesEvent_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        user.setRole(Role.ORGANIZER);

        when(authentication.getName()).thenReturn("testeMarcos123@gmail.com");

        var request = new EventUpdateRequest("Evento novo", "Descrição nova",
                LocalDateTime.now().plusDays(20), "Campinas", 200);

        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.save(event)).thenReturn(event);

        var result = service.updateEvent(1L, request, authentication);

        assertThat(result).isEqualTo(event);
        assertThat(event.getTitle()).isEqualTo("Evento novo");
        assertThat(event.getDescription()).isEqualTo("Descrição nova");
        assertThat(event.getLocation()).isEqualTo("Campinas");
        assertThat(event.getCapacity()).isEqualTo(200);
        assertThat(event.getId()).isEqualTo(1L);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getOrganizer()).isEqualTo(user);

        verify(repository).findById(1L);
        verify(repository).save(event);
    }

    @Test
    @Order(5)
    @DisplayName("Should update event details and set status to PUBLISHED when successful")
    void updateEvent_ThrowsException_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(authentication.getName()).thenReturn("outroUsuario@gmail.com");
        when(repository.findById(1L)).thenReturn(Optional.of(event));

        var request = new EventUpdateRequest("Evento novo", "Descrição nova",
                LocalDateTime.now().plusDays(20), "Campinas", 200);

        assertThatThrownBy(() -> service.updateEvent(1L, request, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not the owner of this event");

        verify(repository, never()).save(any());
    }

    @Test
    @Order(6)
    @DisplayName("Should throw NotFoundException when updating an event that does not exist")
    void updateEvent_throwsNotFoundException_henUpdatesEventNotExists() {
        var request = new EventUpdateRequest("Evento novo", "Descrição nova",
                LocalDateTime.now().plusDays(20), "Campinas", 200);

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateEvent(1L, request, authentication)).isInstanceOf(NotFoundException.class);

        verify(repository, times(1)).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    @Order(7)
    @DisplayName("Should soft delete event by changing status to CANCELED when successful")
    void deleteEvent_deletesEvent_whenSuccessful() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);

        when(authentication.getName()).thenReturn("testeMarcos123@gmail.com");

        service.deleteEvent(event, authentication);

        assertThat(event.getStatus()).isEqualByComparingTo(EventStatus.CANCELED);

        then(repository).should().save(event);
        then(repository).should(never()).delete(event);
    }

    @Test
    @Order(8)
    void publishedEvent_publishedEventOrganizer_WhenSuccessful() {
        var user = userUtils.createSavedUser();
        user.setRole(Role.ORGANIZER);

        var event = eventUtils.createSavedEvent(user);
        event.setStatus(EventStatus.DRAFT);

        when(authentication.getName()).thenReturn(user.getEmail());

        when(repository.findById(event.getId())).thenReturn(Optional.of(event));
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = service.publishEvent(event.getId(), authentication);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);

        verify(repository).findById(event.getId());
        verify(repository).save(event);
    }


    @Test
    @Order(9)
    @DisplayName("Should throw AccessDeniedException when user attempting to publish is not the organizer")
    void publishEvent_throwsAccessDeniedException_WhenUserIsNotOrganizer() {
        var userA = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(userA);
        event.setStatus(EventStatus.DRAFT);

        when(authentication.getName()).thenReturn("usuarioB@gmail.com");
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publishEvent(event.getId(), authentication))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository).findById(event.getId());
        verify(repository, never()).save(any());
    }

    @Test
    @Order(10)
    @DisplayName("Should publish event successfully when user is ADMIN even if not the organizer")
    void publishEvent_publishesEvent_WhenUserIsAdmin() {
        var userA = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(userA);
        event.setStatus(EventStatus.DRAFT);

        var adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        doReturn(List.of(adminAuthority)).when(authentication).getAuthorities();

        when(repository.findById(event.getId())).thenReturn(Optional.of(event));
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = service.publishEvent(event.getId(), authentication);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);

        verify(repository).findById(event.getId());
        verify(repository).save(event);
    }

    @Test
    @Order(11)
    @DisplayName("Should throw IllegalArgumentException when attempting to publish an already published event")
    void publishEvent_throwsIllegalArgumentException_WhenEventIsAlreadyPublished() {
        var user = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(user);
        event.setStatus(EventStatus.PUBLISHED);

        when(authentication.getName()).thenReturn(user.getEmail());

        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publishEvent(event.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository).findById(event.getId());
        verify(repository, never()).save(any());
    }

    @Test
    @Order(12)
    @DisplayName("Should throw AccessDeniedException when user attempting to update event is not the organizer")
    void updateEvent_throwsAccessDeniedException_WhenUserIsNotOrganizer() {
        var userA = userUtils.createSavedUser();
        var event = eventUtils.createSavedEvent(userA);

        when(authentication.getName()).thenReturn("userB@gmail.com");
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        var request = new EventUpdateRequest("Evento novo", "Descrição nova",
                LocalDateTime.now().plusDays(20), "Campinas", 200);

        assertThatThrownBy(() -> service.updateEvent(event.getId(), request, authentication))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository).findById(event.getId());
        verify(repository, never()).save(any());
    }
}