package org.example.ecommerc_shop.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class UpdateCartItemRequest implements Serializable {
    private Integer quantity;
}
