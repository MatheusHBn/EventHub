package com.example.eventhub.mapper;

import com.example.eventhub.domain.Event;
import com.example.eventhub.dto.event.EventCreateForm;
import com.example.eventhub.dto.event.EventCreateRequest;
import com.example.eventhub.dto.event.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    Event toEvent(EventCreateForm form);

    @Mapping(source = "organizer.id", target = "organizerId")
    EventResponse toEventResponse(Event event);

    List<EventResponse> toEventResponseList(List<Event> events);
}
