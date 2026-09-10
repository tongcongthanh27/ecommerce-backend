package org.example.ecommerc_shop.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.response.LoginResponse;
import org.example.ecommerc_shop.mapper.AuthMapper;
import org.example.ecommerc_shop.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthMapper authMapper;
    private final AccountService accountService;
    @GetMapping
    public ApiResponse<LoginResponse> login(Principal principal) {
        LoginResponse loginResponse = authMapper.toLoginResponse(accountService.getUserByUsername(principal.getName()));
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Success")
                .result(loginResponse)
                .build();
    }
}