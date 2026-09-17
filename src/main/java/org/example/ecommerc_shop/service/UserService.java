package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.common.UserRole;
import org.example.ecommerc_shop.dto.request.UserCreateRequest;
import org.example.ecommerc_shop.dto.request.UserUpdateRequest;
import org.example.ecommerc_shop.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    Page<UserResponse> getAllUsers(Integer pageSize, Integer pageNumber);
    void deleteUser(String id);
    UserResponse getUserById(String id);
    UserResponse updateRole(String id, UserRole role);
    UserResponse updateUser(String id, UserUpdateRequest request);
}
