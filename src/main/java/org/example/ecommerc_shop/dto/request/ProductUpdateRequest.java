package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest implements Serializable {
    private String categoryId;

    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    private String description;

    private MultipartFile image;
}
