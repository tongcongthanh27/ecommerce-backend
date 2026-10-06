package org.example.ecommerc_shop.service.specification;

import org.example.ecommerc_shop.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpec {
    public static Specification<Category> likeName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("name"), "%" + name + "%");
        };
    }

    public static Specification<Category> hasParentId(String parentId) {
        return (root, query, criteriaBuilder) -> {
            if (parentId == null || parentId.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("parentId").get("id"), parentId);
        };
    }

    public static Specification<Category> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
    }
}
