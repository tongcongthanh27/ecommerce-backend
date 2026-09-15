package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ecommerc_shop.common.DiscountType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUpdateRequest implements Serializable {
    @NotNull
    private DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.0")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "1")
    private Integer usageLimit;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;
}
