package org.example.ecommerc_shop.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse implements Serializable {
    private String cartItemId;

    private String productVariantId;

    private String productName;

    private String variantName;

    private String imageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private String stockStatus;
}
