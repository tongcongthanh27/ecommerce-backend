package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.ProductCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductUpdateRequest;
import org.example.ecommerc_shop.dto.response.ProductDetailResponse;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    void deleteProduct(String id);
    Page<ProductResponse> getAllProduct(Integer pageSize, Integer pageNumber);
    ProductDetailResponse getProductById(String id);
    ProductResponse updateProduct(String id, ProductUpdateRequest request);
}
