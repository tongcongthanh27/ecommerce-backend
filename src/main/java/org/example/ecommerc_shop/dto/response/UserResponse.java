package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UserRole role;

    private String username;

    private String email;

    private String fullName;

    private String phoneNumber;

    private String address;

    private String avatarUrl;
}
