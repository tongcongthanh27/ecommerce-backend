package org.example.ecommerc_shop.service.specification;

import jakarta.persistence.criteria.JoinType;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.entity.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpec {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> belongsToUser(String userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Order> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static Specification<Order> fetchUser() {
        return (root, query, criteriaBuilder) -> {

            if (query.getResultType() != Long.class
                    && query.getResultType() != long.class) {

                root.fetch("user", JoinType.LEFT);
            }

            return criteriaBuilder.conjunction();
        };
    }
}