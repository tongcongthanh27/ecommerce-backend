package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.CategoryCreateRequest;
import org.example.ecommerc_shop.dto.response.CategoryResponse;
import org.example.ecommerc_shop.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentId", ignore = true)
    Category toCategory(CategoryCreateRequest request);

    @Mapping(source = "parentId.id", target = "parentId")
    CategoryResponse toCategoryResponse(Category category);
}
