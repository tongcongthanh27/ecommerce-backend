package org.example.ecommerc_shop.repository.projection;

import java.math.BigDecimal;

public interface ProductVariantStatisticsProjection {
    Long getTotalVariants();
    Long getInStockVariants();
    Long getLowStockVariants();
    Long getOutOfStockVariants();
    Long getTotalQuantityInStock();
    BigDecimal getAveragePrice();
}
