package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.entity.User;
import org.example.ecommerc_shop.repository.IAccountRepository;
import org.example.ecommerc_shop.service.IAccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class IAccountServiceImpl implements IAccountService {
    private final IAccountRepository iAccountRepository;

    @Override
    public User getUserByUsername(String username) {
        return iAccountRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = iAccountRepository.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("ko tim thay user");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
