package org.example.ecommerc_shop.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse implements Serializable {

    private String cartId;

    private Integer totalItems;

    private BigDecimal subtotal;

    private List<CartItemResponse> items;
}
