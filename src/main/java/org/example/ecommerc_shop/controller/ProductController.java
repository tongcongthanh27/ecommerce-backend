package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.ProductCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductUpdateRequest;
import org.example.ecommerc_shop.dto.response.ProductDetailResponse;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.example.ecommerc_shop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/products")
    public ApiResponse<ProductResponse> createProduct(
            @Valid @ModelAttribute ProductCreateRequest request) {

        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/{productId}")
    public ApiResponse<Void> deleteProduct(
            @PathVariable String productId) {

        productService.deleteProduct(productId);

        return ApiResponse.<Void>builder()
                .message("Delete Product Successfully")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/products/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @ModelAttribute ProductUpdateRequest request) {

        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @GetMapping("/products")
    public ApiResponse<Page<ProductResponse>> getAllProducts(
            @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getAllProduct(pageSize, pageNumber))
                .build();
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @GetMapping("/products/filter")
    public ApiResponse<Page<ProductResponse>> filter(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category_id", required = false) String categoryId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.filter(name, categoryId, page, size))
                .build();
    }

    @GetMapping("/products/category/{categoryId}")
    public ApiResponse<Page<ProductResponse>> getProductByCategory(@RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
                                                                   @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber,
                                                                   @PathVariable String categoryId){
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProductByCategory(categoryId, pageSize, pageNumber))
                .build();
    }
}
