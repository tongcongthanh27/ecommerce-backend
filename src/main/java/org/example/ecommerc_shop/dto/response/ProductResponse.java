package org.example.ecommerc_shop.dto.response;

import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerc_shop.entity.Category;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {
    private String id;

    private String categoryId;

    private String categoryName;

    private String name;

    private String description;

    private String thumbnailUrl;
}
