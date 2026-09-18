package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.ProductVariantCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductVariantUpdateRequest;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;

public interface ProductVariantService {
    ProductVariantResponse createVariant(String id, ProductVariantCreateRequest request);
    void deleteVariant(String id);
    ProductVariantResponse updateVariant(String variantId, ProductVariantUpdateRequest request);
    Page<ProductVariantResponse> getAllVariant(Integer pageSize, Integer pageNumber);
}
