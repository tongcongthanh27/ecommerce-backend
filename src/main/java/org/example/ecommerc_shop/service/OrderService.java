package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.AdminOrderResponse;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request, String username);
    Page<OrderSummaryResponse> getMyOrders(Integer pageSize, Integer pageNumber, String username);
    Page<OrderSummaryResponse> getMyOrdersByStatus(Integer pageSize, Integer pageNumber, String username, OrderStatus status);
    OrderResponse getOrderById(String id, String username);
    void cancelOrder(String orderId, String username);
    Page<AdminOrderResponse> getAllOrders(Integer pageSize, Integer pageNumber);
    Page<AdminOrderResponse> getAllOrdersByStatus(Integer pageSize, Integer pageNumber, OrderStatus status);
    OrderResponse getOrderByIdForAdmin(String orderId);
    OrderResponse updateOrderStatus(String orderId, OrderStatus orderStatus);
}
