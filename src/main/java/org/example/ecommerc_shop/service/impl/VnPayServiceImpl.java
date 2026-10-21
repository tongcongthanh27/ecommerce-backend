package org.example.ecommerc_shop.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.common.PaymentStatus;
import org.example.ecommerc_shop.common.PaymentTransactionStatus;
import org.example.ecommerc_shop.config.VnPayProperties;
import org.example.ecommerc_shop.dto.response.PaymentUrlResponse;
import org.example.ecommerc_shop.entity.Order;
import org.example.ecommerc_shop.entity.Payment;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.repository.OrderRepository;
import org.example.ecommerc_shop.repository.PaymentRepository;
import org.example.ecommerc_shop.service.VnPayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final VnPayProperties properties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(String orderId, String username, HttpServletRequest request) {
        requireCredentials();
        Order order = orderRepository.findByIdAndUserUsernameAndDeletedFalse(orderId, username)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getPaymentMethod() != PaymentMethod.VNPAY || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.PAYMENT_NOT_AVAILABLE);
        }

        Payment payment = paymentRepository.findFirstByTransactionId(orderId).orElseGet(Payment::new);
        if (payment.getId() == null) {
            payment.setOrder(order);
            payment.setPaymentMethod(PaymentMethod.VNPAY);
            payment.setAmount(order.getGrandTotal());
            payment.setStatus(PaymentTransactionStatus.PENDING);
            payment.setTransactionId(orderId);
            paymentRepository.save(payment);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.version());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.tmnCode());
        params.put("vnp_Amount", order.getGrandTotal().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", properties.currency());
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getTrackingNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", properties.locale());
        params.put("vnp_ReturnUrl", properties.returnUrl());
        params.put("vnp_IpAddr", clientIp(request));
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));
        String query = params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        String url = properties.url() + "?" + query + "&vnp_SecureHash=" + hmacSha512(properties.hashSecret(), query);
        return new PaymentUrlResponse(orderId, url);
    }

    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> parameters) {
        requireCredentials();
        if (!verifySignature(parameters)) return ipnResponse("97", "Invalid signature");
        String orderId = parameters.get("vnp_TxnRef");
        Order order = orderRepository.findByIdAndDeletedFalse(orderId).orElse(null);
        if (order == null) return ipnResponse("01", "Order not found");
        Payment payment = paymentRepository.findFirstByTransactionId(orderId).orElse(null);
        if (payment == null) return ipnResponse("01", "Payment not found");
        String amount = parameters.get("vnp_Amount");
        String responseCode = parameters.get("vnp_ResponseCode");
        String transactionStatus = parameters.get("vnp_TransactionStatus");
        if (amount == null || !order.getGrandTotal().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString().equals(amount)) {
            return ipnResponse("04", "Invalid amount");
        }
        if (!"00".equals(responseCode)) {
            if (payment.getStatus() == PaymentTransactionStatus.PENDING) {
                payment.setStatus(PaymentTransactionStatus.FAILED);
                payment.setProviderResponse(responseCode);
            }
            return ipnResponse("00", "Confirm success");
        }
        if (!"00".equals(transactionStatus)) return ipnResponse("00", "Confirm success");
        if (payment.getStatus() == PaymentTransactionStatus.SUCCESS) return ipnResponse("02", "Order already confirmed");
        if (payment.getStatus() != PaymentTransactionStatus.PENDING || order.getPaymentStatus() == PaymentStatus.PAID) {
            return ipnResponse("02", "Order already confirmed");
        }
        payment.setStatus(PaymentTransactionStatus.SUCCESS);
        payment.setPaymentTime(parsePaymentTime(parameters.get("vnp_PayDate")));
        payment.setProviderResponse(responseCode);
        order.setPaymentStatus(PaymentStatus.PAID);
        return ipnResponse("00", "Confirm success");
    }

    @Override
    @Transactional(readOnly = true)
    public String handleReturn(Map<String, String> parameters) {
        if (!verifySignature(parameters)) return "INVALID_SIGNATURE";
        if (!"00".equals(parameters.get("vnp_ResponseCode"))
                || !"00".equals(parameters.get("vnp_TransactionStatus"))) return "FAILED";
        return "SUCCESS";
    }

    private void requireCredentials() {
        if (properties.tmnCode() == null || properties.tmnCode().isBlank()
                || properties.hashSecret() == null || properties.hashSecret().isBlank()) {
            throw new IllegalStateException("VNPAY_TMN_CODE and VNPAY_HASH_SECRET must be configured");
        }
    }

    private boolean verifySignature(Map<String, String> parameters) {
        String provided = parameters.get("vnp_SecureHash");
        if (provided == null || properties.hashSecret() == null || properties.hashSecret().isBlank()) return false;
        Map<String, String> signed = new TreeMap<>(parameters);
        signed.remove("vnp_SecureHash");
        signed.remove("vnp_SecureHashType");
        String query = signed.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        return MessageDigest.isEqual(provided.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                hmacSha512(properties.hashSecret(), query).getBytes(StandardCharsets.US_ASCII));
    }

    private String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign VNPAY request", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }

    private LocalDateTime parsePaymentTime(String value) {
        try {
            return value == null ? LocalDateTime.now() : LocalDateTime.parse(value, DATE_FORMAT);
        } catch (Exception ignored) {
            return LocalDateTime.now();
        }
    }

    private Map<String, String> ipnResponse(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}
