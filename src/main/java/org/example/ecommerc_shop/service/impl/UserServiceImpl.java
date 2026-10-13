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
import org.example.ecommerc_shop.service.specification.UserSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        request.setUsername(request.getUsername().trim());
        request.setEmail(request.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        request.setPhoneNumber(request.getPhoneNumber().trim());
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhoneNumberAndDeletedFalse(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
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
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setDeleted(true);
    }

    @Override
    @Transactional
    public UserResponse getUserById(String id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(String id, UserRole role) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setRole(role);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getUsername() != null) {
            if (request.getUsername().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            String username = request.getUsername().trim();
            if (userRepository.existsByUsernameAndDeletedFalseAndIdNot(username, id)) {
                throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(username);
        }
        if (request.getEmail() != null) {
            if (request.getEmail().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            String email = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
            if (userRepository.existsByEmailAndDeletedFalseAndIdNot(email, id)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(email);
        }
        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null) {
            if (request.getPhoneNumber().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            String phoneNumber = request.getPhoneNumber().trim();
            if (userRepository.existsByPhoneNumberAndDeletedFalseAndIdNot(phoneNumber, id)) {
                throw new AppException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
            }
            user.setPhoneNumber(phoneNumber);
        }
        if (request.getProvince() != null) {
            if (request.getProvince().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            user.setProvince(request.getProvince().trim());
        }
        if (request.getCity() != null) {
            if (request.getCity().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            user.setCity(request.getCity().trim());
        }
        if (request.getAddressDetail() != null) {
            if (request.getAddressDetail().isBlank()) throw new AppException(ErrorCode.INVALID_USER_PROFILE);
            user.setAddressDetail(request.getAddressDetail().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            String oldPublicId = user.getAvatarPublicId();
            CloudinaryUploadResponse response = cloudinaryService.uploadImage(request.getAvatarUrl());
            user.setAvatarPublicId(response.getPublicId());
            user.setAvatarUrl(response.getUrl());
            // Keep the previous image available if the replacement upload fails.
            if (oldPublicId != null && !oldPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldPublicId);
            }
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> filter(String username, String email, String fullName, UserRole userRole, int page, int size) {
        Specification<User> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
        if (username != null && !username.isEmpty()) {
            specification = specification.and(UserSpec.likeUsername(username)
            );
        }

        if (email != null && !email.isEmpty()) {
            specification = specification.and(UserSpec.likeEmail(email)
            );
        }

        if (fullName != null && !fullName.isEmpty()) {
            specification = specification.and(UserSpec.likeFullName(fullName)
            );
        }

        if (userRole != null) {
            specification = specification.and(UserSpec.hasRole(userRole));
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return userRepository.findAll(specification, pageable).map(userMapper::toUserResponse);
    }
}
