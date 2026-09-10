package org.example.ecommerc_shop;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println(encoder.encode("123456"));
    }
}
