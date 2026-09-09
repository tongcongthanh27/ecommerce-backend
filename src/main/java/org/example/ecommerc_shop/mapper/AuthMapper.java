package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.response.LoginResponse;
import org.example.ecommerc_shop.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    LoginResponse toLoginResponse(User user);
}
