package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.example.ecommerc_shop.common.DiscountType;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentStatus;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderItemResponse;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.dto.response.OrderSummaryResponse;
import org.example.ecommerc_shop.entity.*;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.OrderItemMapper;
import org.example.ecommerc_shop.mapper.OrderMapper;
import org.example.ecommerc_shop.repository.*;
import org.example.ecommerc_shop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final TrackingLogRepository trackingLogRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.CARTNOTFOUND));
        List<CartItem> cartItems =
                cartItemRepository.findCartItemsWithProductVariant(cart.getId());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (cartItem.getQuantity() > productVariant.getQuantityInStock()) {
                throw new AppException(ErrorCode.INSUFFICIENTSTOCK);
            }
            BigDecimal itemSubtotal = productVariant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }
        Coupon coupon = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findValidCoupon(request.getCouponCode(), LocalDateTime.now())
                    .orElseThrow(() -> new AppException(ErrorCode.COUPONINVALID));
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
        order.setPaymentStatus(PaymentStatus.UNPAID);
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

    @Override
    @Transactional
    public Page<OrderSummaryResponse> getAllOrders(Integer pageSize, Integer pageNumber, String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAllByUserIdAndDeletedFalse(user.getId(), pageable);
        return orderPage.map(orderMapper::toOrderSummuryResponse);
    }

    @Override
    @Transactional
    public OrderResponse getOrderById(String id, String username) {
        Order order = orderRepository.findByIdAndUserUsernameAndDeletedFalse(id, username).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_FOUND)
        );
        OrderResponse response = orderMapper.toOrderResponse(order);
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsByOrderId(id);
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            itemResponses.add(orderItemMapper.toOrderItemResponse(orderItem));
        }
        response.setItems(itemResponses);
        return response;
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, String username) {
        Order order = orderRepository.findByIdAndUserUsernameAndDeletedFalse(orderId, username).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_FOUND)
        );
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED){
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
        order.setStatus(OrderStatus.CANCELLED);
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsByOrderId(orderId);
        for (OrderItem orderItem : orderItems){
            ProductVariant productVariant = orderItem.getProductVariant();
            productVariant.setQuantityInStock(productVariant.getQuantityInStock() + orderItem.getQuantity());
        }
        TrackingLog trackingLog = new TrackingLog();
        trackingLog.setOrder(order);
        trackingLog.setStatus(OrderStatus.CANCELLED);
        trackingLog.setNote("Order cancelled by customer");
        trackingLogRepository.save(trackingLog);
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