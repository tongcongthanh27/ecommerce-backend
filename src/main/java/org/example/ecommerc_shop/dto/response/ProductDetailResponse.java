package org.example.ecommerc_shop.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse implements Serializable {

    private String id;
    private String categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String thumbnailUrl;

    private List<ProductVariantResponse> variants;
}
