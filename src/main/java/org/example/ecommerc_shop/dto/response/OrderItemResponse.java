package org.example.ecommerc_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private String id;

    private String productVariantId;

    private String variantName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}
