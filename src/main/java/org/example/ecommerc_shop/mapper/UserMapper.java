package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.UserCreateRequest;
import org.example.ecommerc_shop.dto.request.UserUpdateRequest;
import org.example.ecommerc_shop.dto.response.UserResponse;
import org.example.ecommerc_shop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //vi MapStruct không biết cách tự chuyển từ đối tượng upload file (MultipartFile)
    // sang chuỗi URL/đường dẫn ảnh (String) trong Entity.
    @Mapping(target = "avatarUrl", ignore = true)
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
}
