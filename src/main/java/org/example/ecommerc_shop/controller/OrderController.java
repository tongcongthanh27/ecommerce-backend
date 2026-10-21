package org.example.ecommerc_shop.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.*;
import org.example.ecommerc_shop.service.VnPayService;
import org.example.ecommerc_shop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class OrderController {
    private final OrderService orderService;
    private final VnPayService vnPayService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/orders")
    public ApiResponse<?> createOrder(@Valid @RequestBody OrderCreateRequest request, Principal principal,
                                      HttpServletRequest httpRequest){
        OrderResponse order = orderService.createOrder(request, principal.getName());
        if (request.getPaymentMethod() == org.example.ecommerc_shop.common.PaymentMethod.VNPAY) {
            return ApiResponse.builder()
                    .result(Map.of("order", order,
                            "payment", vnPayService.createPayment(order.getId(), principal.getName(), httpRequest)))
                    .build();
        }
        return ApiResponse.<OrderResponse>builder()
                .result(order)
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders")
    public ApiResponse<Page<OrderSummaryResponse>> getMyOrders(@RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber,
                                                                Principal principal){
        return ApiResponse.<Page<OrderSummaryResponse>>builder()
                .result(orderService.getMyOrders(pageSize, pageNumber, principal.getName()))
                .build();

    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders/filter")
    public ApiResponse<Page<OrderSummaryResponse>> getMyOrdersByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber,
            Principal principal) {
        return ApiResponse.<Page<OrderSummaryResponse>>builder()
                .result(orderService.getMyOrdersByStatus(pageSize, pageNumber, principal.getName(), status))
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
    @GetMapping("/orders/{orderId}/tracking")
    public ApiResponse<List<TrackingLogResponse>> getOrderTracking(
            @PathVariable String orderId, Principal principal) {
        return ApiResponse.<List<TrackingLogResponse>>builder()
                .result(orderService.getOrderTracking(orderId, principal.getName()))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders/statistics")
    public ApiResponse<OrderStatisticsResponse> getOrderStatistics() {
        return ApiResponse.<OrderStatisticsResponse>builder()
                .result(orderService.getStatistics())
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public ApiResponse<Page<AdminOrderResponse>> getAllOrders(@RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber){
        return ApiResponse.<Page<AdminOrderResponse>>builder()
                .result(orderService.getAllOrders(pageSize, pageNumber))
                .build();

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders/filter")
    public ApiResponse<Page<AdminOrderResponse>> getAllOrdersByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber) {
        return ApiResponse.<Page<AdminOrderResponse>>builder()
                .result(orderService.getAllOrdersByStatus(pageSize, pageNumber, status))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderByIdForAdmin(
            @PathVariable String orderId) {

        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderByIdForAdmin(orderId))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/orders/{orderId}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {

        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrderStatus(orderId, status))
                .build();
    }

   @PreAuthorize("hasRole('SHIPPER')")
   @GetMapping("/shipper/orders/")
   public ApiResponse<Page<ShipperOrderResponse>> getAllOrderForShipper(@RequestParam(name = "page_size", defaultValue = "10") Integer pageSize,
                                                                        @RequestParam(name = "page_number", defaultValue = "1") Integer pageNumber,
                                                                        Principal principal){
        return ApiResponse.<Page<ShipperOrderResponse>>builder()
                .result(orderService.getAllOrderForShipper(pageSize, pageNumber, principal.getName()))
                .build();
   }

   @PreAuthorize("hasRole('SHIPPER')")
    @PatchMapping("/shipper/orders/{orderId}/status")
    public ApiResponse<ShipperOrderResponse> updateOrderStatusByShipper(@PathVariable String orderId,
                                                                        @RequestParam("status") OrderStatus status,
                                                                        Principal principal){
        return ApiResponse.<ShipperOrderResponse>builder()
                .result(orderService.updateOrderStatusByShipper(orderId, status, principal.getName()))
                .build();
   }

    @GetMapping("/admin/order/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportOrders(@RequestParam(required = false) OrderStatus status, @RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx");
        orderService.exportOrders(status, keyword, response.getOutputStream());
    }
}
