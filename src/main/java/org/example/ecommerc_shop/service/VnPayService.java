package org.example.ecommerc_shop.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.ecommerc_shop.dto.response.PaymentUrlResponse;

import java.util.Map;

public interface VnPayService {
    PaymentUrlResponse createPayment(String orderId, String username, HttpServletRequest request);
    Map<String, String> handleIpn(Map<String, String> parameters);
    String handleReturn(Map<String, String> parameters);
}
