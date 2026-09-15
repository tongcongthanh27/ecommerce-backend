package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.common.DiscountType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse implements Serializable {
    private String id;

    private String code;

    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;


    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private CouponStatus status;
}
