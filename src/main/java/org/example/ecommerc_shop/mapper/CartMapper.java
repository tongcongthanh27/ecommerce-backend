package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartResponse;
import org.example.ecommerc_shop.entity.Cart;
import org.example.ecommerc_shop.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartId", source = "cart.id")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalItems", source = "totalItems")
    @Mapping(target = "subtotal", source = "subtotal")
    CartResponse toCartResponse(
            Cart cart,
            List<CartItemResponse> cartItems,
            Integer totalItems,
            BigDecimal subtotal
    );

    CartItemResponse toCartItemResponse(CartItem cartItem);
}
