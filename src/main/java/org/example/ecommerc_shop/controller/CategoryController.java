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
@RequestMapping("/category")
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @ModelAttribute CategoryCreateRequest categoryCreateRequest){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(categoryCreateRequest))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping
    public ApiResponse<Page<CategoryResponse>> getAllCategory(@RequestParam(name = "page_size") Integer pageSize,
                                                              @RequestParam(name = "page_number") Integer pageNumber){
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.getAllCategory(pageSize, pageNumber))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable String categoryId){
        categoryService.deleteCategory(categoryId);
        return ApiResponse.<Void>builder()
                .message("Delete Category Succesfully")
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable String categoryId){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(categoryId))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SHIPPER')")
    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> getCategoryTree(){
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getCategoryTree())
                .build();

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @ModelAttribute CategoryUpdateRequest request) {

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(categoryId, request))
                .build();
    }
}
