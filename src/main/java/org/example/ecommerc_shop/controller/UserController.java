package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.UserRole;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.UserCreateRequest;
import org.example.ecommerc_shop.dto.request.UserUpdateRequest;
import org.example.ecommerc_shop.dto.response.UserResponse;
import org.example.ecommerc_shop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@Validated
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @Valid @ModelAttribute UserCreateRequest request) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(name = "page_size") Integer pageSize,
            @RequestParam(name = "page_number") Integer pageNumber) {

        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getAllUsers(pageSize, pageNumber))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable String userId) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @ModelAttribute UserUpdateRequest request) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(
            @PathVariable String userId) {

        userService.deleteUser(userId);

        return ApiResponse.<Void>builder()
                .message("Delete User Successfully")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/role")
    public ApiResponse<UserResponse> updateRole(
            @PathVariable String userId,
            @RequestParam UserRole role) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.updateRole(userId, role))
                .build();
    }
}
