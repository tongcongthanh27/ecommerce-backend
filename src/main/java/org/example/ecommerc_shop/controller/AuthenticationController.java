package org.example.ecommerc_shop.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.response.LoginResponse;
import org.example.ecommerc_shop.dto.request.LoginRequest;
import org.example.ecommerc_shop.dto.request.RegisterRequest;
import org.example.ecommerc_shop.entity.User;
import org.example.ecommerc_shop.mapper.AuthMapper;
import org.example.ecommerc_shop.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthMapper authMapper;
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Validated @RequestBody RegisterRequest request) {
        User user = accountService.register(request);
        LoginResponse response = authMapper.toLoginResponse(user);
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Đăng ký thành công")
                .result(response)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = accountService.getUserByUsername(authentication.getName());
        LoginResponse response = authMapper.toLoginResponse(user);
        // Current security configuration uses HTTP Basic; this value is a Base64 credential string, not a JWT.
        response.setToken(java.util.Base64.getEncoder().encodeToString(
                (request.getUsername() + ":" + request.getPassword()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Đăng nhập thành công")
                .result(response)
                .build();
    }
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(Principal principal) {
        LoginResponse loginResponse = authMapper.toLoginResponse(accountService.getUserByUsername(principal.getName()));
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Success")
                .result(loginResponse)
                .build();
    }
}
