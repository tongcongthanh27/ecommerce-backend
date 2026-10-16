package org.example.ecommerc_shop.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ecommerc_shop.common.OrderStatus;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class OrderStatisticsResponse {
    private long totalOrders;
    private Map<OrderStatus, Long> ordersByStatus;
    private BigDecimal totalRevenue;
    private BigDecimal deliveredRevenue;
}
