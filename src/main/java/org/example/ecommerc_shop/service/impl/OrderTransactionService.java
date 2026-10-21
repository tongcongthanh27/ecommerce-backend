package org.example.ecommerc_shop.service.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.DiscountType;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentStatus;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderItemResponse;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.entity.*;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.OrderItemMapper;
import org.example.ecommerc_shop.mapper.OrderMapper;
import org.example.ecommerc_shop.repository.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderTransactionService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final TrackingLogRepository trackingLogRepository;
    private final NotificationRepository notificationRepository;

    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    ObjectOptimisticLockingFailureException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public OrderResponse createOrderTransaction(OrderCreateRequest request, String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems =
                cartItemRepository.findCartItemsWithProductVariant(cart.getId());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (cartItem.getQuantity() > productVariant.getQuantityInStock()) {
                throw new AppException(ErrorCode.INSUFFICIENT_ORDER_INVENTORY);
            }
            BigDecimal itemSubtotal = productVariant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }
        Coupon coupon = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findValidCoupon(request.getCouponCode(), LocalDateTime.now()).orElseThrow(
                    () -> new AppException(ErrorCode.INVALID_COUPON));
            if (subtotal.compareTo(coupon.getMinOrderValue()) >= 0) {
                if (DiscountType.PERCENTAGE.equals(coupon.getDiscountType())) {
                    discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                    if (coupon.getMaxDiscountAmount() != null) {
                        discount = discount.min(coupon.getMaxDiscountAmount());
                    }
                } else if (DiscountType.FIXED_AMOUNT.equals(coupon.getDiscountType())) {
                    discount = coupon.getDiscountValue();
                    if (coupon.getMaxDiscountAmount() != null) {
                        discount = discount.min(coupon.getMaxDiscountAmount());
                    }
                    discount = discount.min(subtotal);
                }
            }
        }

        BigDecimal shippingFee = BigDecimal.valueOf(500000);
        BigDecimal grandTotal = subtotal.add(shippingFee).subtract(discount);
        Order order = orderMapper.toOrder(request);
        order.setTrackingNumber(generateTrackingNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(request.getPaymentMethod() == PaymentMethod.VNPAY
                ? PaymentStatus.AWAITING_PAYMENT : PaymentStatus.UNPAID);
        order.setCoupon(coupon);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setGrandTotal(grandTotal);
        Order savedOrder = orderRepository.save(order);
        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
        }
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = orderItemMapper.toOrderItem(cartItem);
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
            ProductVariant productVariant = cartItem.getProductVariant();
            productVariant.setQuantityInStock(productVariant.getQuantityInStock() - cartItem.getQuantity());
            cartItem.setDeleted(true);
        }
        orderItemRepository.saveAll(orderItems);
        TrackingLog trackingLog = new TrackingLog();
        trackingLog.setOrder(savedOrder);
        trackingLog.setStatus(OrderStatus.PENDING);
        trackingLog.setNote("Order created");
        trackingLogRepository.save(trackingLog);
        OrderResponse response = orderMapper.toOrderResponse(savedOrder);
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItemResponse itemResponse = orderItemMapper.toOrderItemResponse(orderItem);
            itemResponses.add(itemResponse);
        }
        response.setItems(itemResponses);
        return response;
    }
    private String generateTrackingNumber() {
        String randomCode = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "ORD-" + LocalDate.now() + "-" + randomCode;
    }
}
