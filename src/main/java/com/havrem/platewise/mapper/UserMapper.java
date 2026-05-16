package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.user.UserDto;
import com.havrem.platewise.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
