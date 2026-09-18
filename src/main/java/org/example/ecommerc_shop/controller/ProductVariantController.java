package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.ProductVariantCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductVariantUpdateRequest;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.service.ProductVariantService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class ProductVariantController {
    private final ProductVariantService productVariantService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/products/{productId}/variants")
    public ApiResponse<ProductVariantResponse> createVariant(@Valid @ModelAttribute ProductVariantCreateRequest request, @PathVariable String productId){
        return ApiResponse.<ProductVariantResponse>builder()
                .result(productVariantService.createVariant(productId,request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/products/variants/{variantId}")
    public ApiResponse<ProductVariantResponse> updateVariant(@PathVariable String variantId, @Valid @ModelAttribute ProductVariantUpdateRequest request) {
        return ApiResponse.<ProductVariantResponse>builder()
                .result(productVariantService.updateVariant(variantId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/variants/{variantId}")
    public ApiResponse<Void> deleteVariant(@PathVariable String variantId) {
        productVariantService.deleteVariant(variantId);
        return ApiResponse.<Void>builder()
                .message("Delete Variant success")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/product-variants")
    public ApiResponse<Page<ProductVariantResponse>> getAllVariant(@RequestParam(name = "page_size") Integer pageSize,
                                                                   @RequestParam(name = "page_number") Integer pageNumber){
        return ApiResponse.<Page<ProductVariantResponse>>builder()
                .result(productVariantService.getAllVariant(pageSize, pageNumber))
                .build();
    }
}
