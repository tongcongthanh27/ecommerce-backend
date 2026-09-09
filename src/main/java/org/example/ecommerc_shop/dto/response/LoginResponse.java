package org.example.ecommerc_shop.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String username;
    private String fullName;
    private String role;
}
