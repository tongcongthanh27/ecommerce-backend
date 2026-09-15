package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest orderCreateRequest, Principal principal){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(orderCreateRequest, principal.getName()))
                .build();
    }
}
