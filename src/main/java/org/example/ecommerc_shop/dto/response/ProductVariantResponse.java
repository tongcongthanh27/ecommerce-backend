package org.example.ecommerc_shop.dto.response;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse implements Serializable{

    private String id;

    private String productId;

    private String productName;

    private String sku;

    private String variantName;

    private BigDecimal price;

    private Integer quantityInStock;

    private String stockStatus;

    private String imageUrl;
}
