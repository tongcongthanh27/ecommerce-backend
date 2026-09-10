package org.example.ecommerc_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
public class CartSummaryResponse implements Serializable {
    private Integer totalItems;

    private BigDecimal subtotal;

    private BigDecimal shippingFee;

    private BigDecimal total;
}