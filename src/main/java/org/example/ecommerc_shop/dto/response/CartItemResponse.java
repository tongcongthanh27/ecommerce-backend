package org.example.ecommerc_shop.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
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
