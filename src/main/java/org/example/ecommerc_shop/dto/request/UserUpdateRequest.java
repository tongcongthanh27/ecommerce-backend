package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest implements Serializable {

    @Size(min = 3, max = 50,
            message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    private String username;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String fullName;

    @Pattern(
            regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    private String address;

    private MultipartFile avatarUrl;
}
