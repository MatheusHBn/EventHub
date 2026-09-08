package com.example.eventhub.commons;

import com.example.eventhub.domain.Event;
import com.example.eventhub.domain.EventStatus;
import com.example.eventhub.domain.User;
import com.example.eventhub.dto.event.EventResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventUtils {

    public List<Event> newEventList(User user1, User user2){
       var event1 = Event.builder().title("Evento-importante").description("Uma descrição importante").date(LocalDateTime.now().plusDays(1))
                .location("São Paulo").capacity(20).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
                organizer(user1).build();
       var event2 = Event.builder().title("Evento-importante4").description("Uma descrição importante4").date(LocalDateTime.now().plusDays(4))
               .location("São Paulo").capacity(30).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
               organizer(user2).build();

       return new ArrayList<>(List.of(event1, event2));
    }

    public Event createEvent(User user){
        return Event.builder().title("Evento-importante").description("Uma descrição importante").date(LocalDateTime.now().plusDays(1))
                .location("São Paulo").capacity(20).status(EventStatus.DRAFT).createdAt(LocalDateTime.now()).
                organizer(user).build();
    }

    public Event createSavedEvent(User user){
        return Event.builder().id(1L).title("Evento-importante 2").description("Uma descrição importante 2 ").date(LocalDateTime.now().plusMinutes(7))
                .location("São Paulo").capacity(20).status(EventStatus.PUBLISHED).createdAt(LocalDateTime.now().plusMinutes(29)).
                organizer(user).build();
    }

    public EventResponse createEventResponse(){
        return EventResponse.builder()
                .id(1L)
                .title("Evento-importante 2")
                .description("Uma descrição importante 2")
                .date(LocalDateTime.now().plusDays(10))
                .location("São Paulo")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .createdAt(LocalDateTime.parse("2026-09-17T14:54:05.6788501"))
                .organizerId(1L)
                .build();
    }
}
