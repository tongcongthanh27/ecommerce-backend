package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.request.CategoryCreateRequest;
import org.example.ecommerc_shop.dto.request.CategoryUpdateRequest;
import org.example.ecommerc_shop.dto.response.CategoryResponse;
import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.example.ecommerc_shop.entity.Category;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.CategoryMapper;
import org.example.ecommerc_shop.repository.CategoryRepository;
import org.example.ecommerc_shop.service.CategoryService;
import org.example.ecommerc_shop.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }
        Category parentCategory = null;
        if (request.getParentId() != null) {
            parentCategory = categoryRepository
                    .findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }
        Category category = categoryMapper.toCategory(request);
        category.setParentId(parentCategory);
        if (request.getImage() != null
                && !request.getImage().isEmpty()) {

            CloudinaryUploadResponse uploadResult =
                    cloudinaryService.uploadImage(request.getImage());

            category.setImageUrl(uploadResult.getUrl());
            category.setImagePublicId(uploadResult.getPublicId());
        }
        categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public Page<CategoryResponse> getAllCategory(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Category> categoryPage = categoryRepository.findAllByDeletedFalse(pageable);
        return categoryPage.map(categoryMapper::toCategoryResponse);
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setDeleted(true);
    }

    @Override
    @Transactional
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {

        List<Category> categories = categoryRepository.findAllByDeletedFalse();
        Map<String, CategoryResponse> categoryMap = new HashMap<>();
        for (Category category : categories) {
            CategoryResponse response = categoryMapper.toCategoryResponse(category);
            categoryMap.put(category.getId(), response);
        }
        List<CategoryResponse> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryResponse current = categoryMap.get(category.getId());
            if (category.getParentId() == null) {
                roots.add(current);
            } else {
                CategoryResponse parent = categoryMap.get(category.getParentId().getId());
                if (parent != null) {
                    parent.getChildren().add(current);
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryUpdateRequest request) {
        Category category = categoryRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            category.setDescription(request.getDescription());
        }
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String oldPublicId = category.getImagePublicId();
            CloudinaryUploadResponse uploadResult = cloudinaryService.uploadImage(request.getImage());
            category.setImageUrl(uploadResult.getUrl());
            category.setImagePublicId(uploadResult.getPublicId());
            if (oldPublicId != null) {
                cloudinaryService.deleteImage(oldPublicId);
            }
        }
        if (request.getParentId() != null && !request.getParentId().isBlank()) {
            if (id.equals(request.getParentId())) {
                throw new AppException(ErrorCode.INVALID_PARENT_CATEGORY);
            }
            Category parentCategory = categoryRepository
                    .findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParentId(parentCategory);
        }
        return categoryMapper.toCategoryResponse(category);
    }


}
