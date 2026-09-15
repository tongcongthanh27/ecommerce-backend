package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    Optional<Coupon> findByCodeAndDeletedFalse(String code);
}
