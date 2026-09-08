package com.example.eventhub.mapper;

import com.example.eventhub.domain.User;
import com.example.eventhub.dto.user.UserRegisterRequest;
import com.example.eventhub.dto.user.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserRegisterRequest userRegisterRequest);

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}
