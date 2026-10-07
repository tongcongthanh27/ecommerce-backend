package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.dto.response.OrderSummaryResponse;
import org.example.ecommerc_shop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request, Principal principal){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request, principal.getName()))
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders")
    public ApiResponse<Page<OrderSummaryResponse>> getAllOrders(@RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber,
                                                                Principal principal){
        return ApiResponse.<Page<OrderSummaryResponse>>builder()
                .result(orderService.getAllOrders(pageSize, pageNumber, principal.getName()))
                .build();

    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId, Principal principal){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId, principal.getName()))
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/orders/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderId, Principal principal){
        orderService.cancelOrder(orderId, principal.getName());
        return ApiResponse.<Void>builder()
                .message("Huy don hang thanh cong")
                .build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/orders")
//    public ApiResponse<>
}
