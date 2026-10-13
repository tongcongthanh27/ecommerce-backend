package org.example.ecommerc_shop.dto.request;

import lombok.*;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest implements Serializable {

    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 36)
    private String parentId;

    private MultipartFile image;
}