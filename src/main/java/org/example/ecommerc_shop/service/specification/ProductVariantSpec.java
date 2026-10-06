package org.example.ecommerc_shop.service.specification;

import org.example.ecommerc_shop.entity.ProductVariant;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductVariantSpec {
    public static Specification<ProductVariant> likeVariantName(String variantName) {
        return (root, query, criteriaBuilder) -> {
            if (variantName == null || variantName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("variantName"), "%" + variantName + "%");
        };
    }

    public static Specification<ProductVariant> likeSku(String sku) {
        return (root, query, criteriaBuilder) -> {
            if (sku == null || sku.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("sku"), "%" + sku + "%");
        };
    }

    public static Specification<ProductVariant> hasProductId(String productId) {
        return (root, query, criteriaBuilder) -> {
            if (productId == null || productId.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<ProductVariant> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<ProductVariant> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<ProductVariant> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
    }
}
