package org.example.ecommerc_shop.dto.request;

import lombok.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest implements Serializable {

    private String name;

    private String description;

    private String parentId;

    private MultipartFile image;
}