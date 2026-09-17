package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, String> {

    Optional<Coupon> findByCodeAndDeletedFalse(String code);

    Optional<Coupon> findByIdAndDeletedFalse(String id);

    boolean existsByCodeAndDeletedFalse(String code);

    Page<Coupon> findAllByDeletedFalse(Pageable pageable);
}