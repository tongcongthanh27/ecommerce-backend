package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.CategoryCreateRequest;
import org.example.ecommerc_shop.dto.request.CategoryUpdateRequest;
import org.example.ecommerc_shop.dto.response.CategoryResponse;
import org.example.ecommerc_shop.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @ModelAttribute CategoryCreateRequest request) {

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping("/categories")
    public ApiResponse<Page<CategoryResponse>> getAllCategories(
            @RequestParam(name = "page_size") Integer pageSize,
            @RequestParam(name = "page_number") Integer pageNumber) {

        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getAllCategory(pageSize, pageNumber))
                .build();
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> getCategoryById(
            @PathVariable String categoryId) {

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(categoryId))
                .build();
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping("/categories/tree")
    public ApiResponse<List<CategoryResponse>> getCategoryTree() {

        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getCategoryTree())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @Valid @ModelAttribute CategoryUpdateRequest request) {

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(categoryId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable String categoryId) {

        categoryService.deleteCategory(categoryId);

        return ApiResponse.<Void>builder()
                .message("Delete Category Successfully")
                .build();
    }
}
