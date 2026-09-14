package com.example.eventhub.mapper;

import com.example.eventhub.domain.Registration;
import com.example.eventhub.dto.registration.RegistrationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.id", target = "userId")
    RegistrationResponse toRegistrationResponse(Registration registration);

    List<RegistrationResponse> toRegistrationResponseList(List<Registration> registrations);
}
