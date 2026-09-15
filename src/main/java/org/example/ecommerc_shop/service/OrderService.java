package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request, String username);
}
