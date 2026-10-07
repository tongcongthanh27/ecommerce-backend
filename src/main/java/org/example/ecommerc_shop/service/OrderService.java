package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request, String username);
    Page<OrderSummaryResponse> getAllOrders(Integer pageSize, Integer pageNumber, String username);
    OrderResponse getOrderById(String id, String username);
    void cancelOrder(String orderId, String username);
}
