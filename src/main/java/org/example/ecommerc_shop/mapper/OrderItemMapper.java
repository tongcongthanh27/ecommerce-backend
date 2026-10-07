package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.response.OrderItemResponse;
import org.example.ecommerc_shop.entity.CartItem;
import org.example.ecommerc_shop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "productVariant", target = "productVariant")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "productVariant.price", target = "unitPrice")
    OrderItem toOrderItem(CartItem cartItem);

    @Mapping(source = "productVariant.sku", target = "sku")
    @Mapping(source = "productVariant.variantName", target = "variantName")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(
            target = "subtotal",
            expression = "java(orderItem.getUnitPrice().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity())))"
    )
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
