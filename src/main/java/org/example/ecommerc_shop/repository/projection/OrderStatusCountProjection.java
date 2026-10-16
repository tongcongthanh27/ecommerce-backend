package org.example.ecommerc_shop.repository.projection;

import org.example.ecommerc_shop.common.OrderStatus;

public interface OrderStatusCountProjection {
    OrderStatus getStatus();
    Long getTotal();
}
