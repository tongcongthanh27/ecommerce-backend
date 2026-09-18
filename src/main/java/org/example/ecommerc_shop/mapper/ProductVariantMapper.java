package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.ProductVariantCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductVariantUpdateRequest;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductVariantMapper {
    ProductVariant toVariant(ProductVariantCreateRequest request);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ProductVariantResponse toVariantResponse(ProductVariant productVariant);

    void updateVariant(
            ProductVariantUpdateRequest request,
            @MappingTarget ProductVariant productVariant
    );
}

