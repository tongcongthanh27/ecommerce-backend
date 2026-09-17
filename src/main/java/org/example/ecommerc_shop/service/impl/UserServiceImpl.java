package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.UserRole;
import org.example.ecommerc_shop.dto.request.UserCreateRequest;
import org.example.ecommerc_shop.dto.request.UserUpdateRequest;
import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.example.ecommerc_shop.dto.response.UserResponse;
import org.example.ecommerc_shop.entity.User;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.UserMapper;
import org.example.ecommerc_shop.repository.UserRepository;
import org.example.ecommerc_shop.service.CloudinaryService;
import org.example.ecommerc_shop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXIST);
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        if (userRepository.existsByPhoneNumberAndDeletedFalse(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXIST);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()){
            CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(request.getAvatarUrl());
            user.setAvatarUrl(uploadResponse.getUrl());
            user.setAvatarPublicId(uploadResponse.getPublicId());
        }
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public Page<UserResponse> getAllUsers(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber -1, pageSize);
        Page<User> userPage = userRepository.findAllByDeletedFalse(pageable);
        return userPage.map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        user.setDeleted(true);
    }

    @Override
    @Transactional
    public UserResponse getUserById(String id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(String id, UserRole role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        user.setRole(role);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        if (request.getUsername() != null) {
            if (userRepository.existsByUsernameAndDeletedFalseAndIdNot(request.getUsername(), id)) {
                throw new AppException(ErrorCode.USERNAME_ALREADY_EXIST);
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailAndDeletedFalseAndIdNot(request.getEmail(), id)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST);
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumberAndDeletedFalseAndIdNot(request.getPhoneNumber(), id)) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXIST);
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            String oldPublicId = user.getAvatarPublicId();
            CloudinaryUploadResponse response = cloudinaryService.uploadImage(request.getAvatarUrl());
            user.setAvatarPublicId(response.getPublicId());
            user.setAvatarUrl(response.getUrl());
            if (oldPublicId != null && !oldPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldPublicId);
            }
        }
        return userMapper.toUserResponse(user);
    }
}
