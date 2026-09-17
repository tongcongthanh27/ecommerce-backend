package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.CategoryCreateRequest;
import org.example.ecommerc_shop.dto.request.CategoryUpdateRequest;
import org.example.ecommerc_shop.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest request);
    Page<CategoryResponse> getAllCategory(Integer pageSize, Integer pageNumber);
    void deleteCategory(String id);
    CategoryResponse getCategoryById(String id);
    List<CategoryResponse> getCategoryTree();
    CategoryResponse updateCategory(String id, CategoryUpdateRequest request);
}
