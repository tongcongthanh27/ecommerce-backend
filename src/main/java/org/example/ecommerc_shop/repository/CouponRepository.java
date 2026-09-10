package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    Coupon findCouponByCode(String code);
}
