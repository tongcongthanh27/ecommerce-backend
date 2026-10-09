package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String>, JpaSpecificationExecutor<Coupon> {

    Optional<Coupon> findByCodeAndDeletedFalse(String code);

    Optional<Coupon> findByIdAndDeletedFalse(String id);

    boolean existsByCodeAndDeletedFalse(String code);

    Page<Coupon> findAllByDeletedFalse(Pageable pageable);

    @Query("""
        SELECT c FROM Coupon c
        WHERE c.code = :code
          AND c.deleted = false
          AND c.status = 'ACTIVE'
          AND :now BETWEEN c.startDate AND c.endDate
          AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)
    """)
    Optional<Coupon> findValidCoupon(
            @Param("code") String code,
            @Param("now") LocalDateTime now
    );
}
