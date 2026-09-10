package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "id", target = "cartItemId")
    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.name", target = "productName")
    @Mapping(source = "productVariant.variantName", target = "variantName")
    @Mapping(source = "productVariant.imageUrl", target = "imageUrl")
    @Mapping(source = "productVariant.price", target = "unitPrice")
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
