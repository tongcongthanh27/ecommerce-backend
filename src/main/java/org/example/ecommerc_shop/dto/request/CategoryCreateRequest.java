package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest implements Serializable {
    @NotBlank(message = "Category name must not be blank")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 36, message = "Parent ID must not exceed 36 characters")
    private String parentId;

    private MultipartFile image;
}
