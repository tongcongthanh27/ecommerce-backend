package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.ProductCreateRequest;
import org.example.ecommerc_shop.dto.response.ProductDetailResponse;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.example.ecommerc_shop.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "thumbnailPublicId", ignore = true)
    Product toProduct(ProductCreateRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toProductResponse(Product product);

    ProductDetailResponse toProductDetailResponse(Product product);

}
