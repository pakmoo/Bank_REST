package com.example.bankcards.mapping;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Из входных данных → сущность
    User dtoToUser(UserRequest userRequest);

    // Сущность → DTO для ответа
    UserDTO userToDTO(User user);
}