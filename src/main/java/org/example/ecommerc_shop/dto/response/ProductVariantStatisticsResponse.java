package org.example.ecommerc_shop.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductVariantStatisticsResponse {
    private long totalVariants;
    private long inStockVariants;
    private long lowStockVariants;
    private long outOfStockVariants;
    private long totalQuantityInStock;
    private BigDecimal averagePrice;
}
