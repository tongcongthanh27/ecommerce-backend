package org.example.ecommerc_shop.service.specification;

import org.example.ecommerc_shop.common.UserRole;
import org.example.ecommerc_shop.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpec {
    public static Specification<User> likeUsername(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("username"), "%" + username + "%"
            );
        };
    }

    public static Specification<User> likeEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("email"), "%" + email + "%"
            );
        };
    }

    public static Specification<User> likeFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {

            if (fullName == null || fullName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("fullName"), "%" + fullName + "%"
            );
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("role"), role
            );
        };
    }
}
