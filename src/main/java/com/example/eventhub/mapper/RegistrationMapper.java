package com.example.eventhub.mapper;

import com.example.eventhub.domain.Registration;
import com.example.eventhub.dto.registration.RegistrationResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {

    RegistrationResponse toRegistrationResponse(Registration registration);

    List<RegistrationResponse> toRegistrationResponseList(List<Registration> registration);
}
