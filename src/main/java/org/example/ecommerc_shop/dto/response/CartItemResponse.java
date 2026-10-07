package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.StockStatus;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse implements Serializable {
    private String cartItemId;

    private String sku;

    private String productName;

    private String variantName;

    private String imageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private StockStatus stockStatus;
}
