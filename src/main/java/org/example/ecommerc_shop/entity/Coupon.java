package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.common.DiscountType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
public class Coupon extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status = CouponStatus.ACTIVE;
}
