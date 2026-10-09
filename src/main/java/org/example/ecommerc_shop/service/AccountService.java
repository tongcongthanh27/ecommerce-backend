package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.entity.User;
import org.example.ecommerc_shop.dto.request.RegisterRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {
    public User getUserByUsername(String username);
    User register(RegisterRequest request);
}
