package org.example.ecommerc_shop.service.specification;

import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.common.DiscountType;
import org.example.ecommerc_shop.entity.Coupon;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CouponSpec {
    public static Specification<Coupon> likeCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (code == null || code.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("code"), "%" + code + "%");
        };
    }

    public static Specification<Coupon> hasStatus(CouponStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Coupon> hasDiscountType(DiscountType discountType) {
        return (root, query, criteriaBuilder) -> {
            if (discountType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("discountType"), discountType);
        };
    }

    public static Specification<Coupon> startDateGreaterThanOrEqual(LocalDateTime startDateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (startDateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDateFrom);
        };
    }

    public static Specification<Coupon> startDateLessThanOrEqual(LocalDateTime startDateTo) {
        return (root, query, criteriaBuilder) -> {
            if (startDateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), startDateTo);
        };
    }

    public static Specification<Coupon> endDateGreaterThanOrEqual(LocalDateTime endDateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (endDateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), endDateFrom);
        };
    }

    public static Specification<Coupon> endDateLessThanOrEqual(LocalDateTime endDateTo) {
        return (root, query, criteriaBuilder) -> {
            if (endDateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDateTo);
        };
    }

    public static Specification<Coupon> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
    }
}
