package org.example.ecommerc_shop.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.response.PaymentUrlResponse;
import org.example.ecommerc_shop.service.VnPayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/vnpay")
public class PaymentController {
    private final VnPayService vnPayService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/orders/{orderId}")
    public ApiResponse<PaymentUrlResponse> createPayment(@PathVariable String orderId,
                                                          Principal principal,
                                                          HttpServletRequest request) {
        return ApiResponse.<PaymentUrlResponse>builder()
                .result(vnPayService.createPayment(orderId, principal.getName(), request))
                .build();
    }

    @GetMapping("/ipn")
    public Map<String, String> ipn(@RequestParam Map<String, String> parameters) {
        return vnPayService.handleIpn(parameters);
    }

    @GetMapping("/return")
    public ApiResponse<String> paymentReturn(@RequestParam Map<String, String> parameters) {
        return ApiResponse.<String>builder().result(vnPayService.handleReturn(parameters)).build();
    }
}
