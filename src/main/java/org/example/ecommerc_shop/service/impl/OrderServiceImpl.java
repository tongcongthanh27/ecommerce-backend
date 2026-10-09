package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.DiscountType;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentStatus;
import org.example.ecommerc_shop.common.UserRole;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.AdminOrderResponse;
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
import org.example.ecommerc_shop.service.specification.OrderSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public Page<OrderSummaryResponse> getMyOrders(Integer pageSize, Integer pageNumber, String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAllByUserIdAndDeletedFalse(user.getId(), pageable);
        return orderPage.map(orderMapper::toOrderSummuryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrdersByStatus(Integer pageSize, Integer pageNumber, String username, OrderStatus status) {
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createdAt").descending());
        Specification<Order> spec = Specification.where(OrderSpec.isNotDeleted())
                .and(OrderSpec.belongsToUser(user.getId()))
                .and(OrderSpec.hasStatus(status));
        return orderRepository.findAll(spec, pageable).map(orderMapper::toOrderSummuryResponse);
    }

    @Override
    @Transactional
    public OrderResponse getOrderById(String id, String username) {
        Order order = orderRepository.findByIdAndUserUsernameAndDeletedFalse(id, username).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_FOUND)
        );
        OrderResponse response = orderMapper.toOrderResponse(order);
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsWithProductVariant(id);
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
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsWithProductVariant(orderId);
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

    @Override
    @Transactional
    public Page<AdminOrderResponse> getAllOrders(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAllByDeletedFalse(pageable);
        return orderPage.map(orderMapper::toAdminOrderResponse);
    }

    @Override
    @Transactional
    public Page<AdminOrderResponse> getAllOrdersByStatus(Integer pageSize, Integer pageNumber, OrderStatus status) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createdAt").descending());
        Specification<Order> spec = Specification.where(OrderSpec.isNotDeleted())
                .and(OrderSpec.hasStatus(status))
                .and(OrderSpec.fetchUser());
        return orderRepository.findAll(spec, pageable).map(orderMapper::toAdminOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse getOrderByIdForAdmin(String orderId) {
        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        OrderResponse response = orderMapper.toOrderResponse(order);
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsWithProductVariant(orderId);
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            itemResponses.add(orderItemMapper.toOrderItemResponse(orderItem));
        }
        response.setItems(itemResponses);
        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findByIdAndDeletedFalse(orderId).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        OrderStatus currentStatus = order.getStatus();
        boolean isAllowedByAdmin = (currentStatus == OrderStatus.PENDING && orderStatus == OrderStatus.CONFIRMED)
                        || (currentStatus == OrderStatus.CONFIRMED && orderStatus == OrderStatus.PICKING);
        if (!isAllowedByAdmin || !currentStatus.canTransitionTo(orderStatus)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
        if (currentStatus == OrderStatus.PENDING && orderStatus == OrderStatus.CONFIRMED) {
            String province = order.getProvince();
            User shipper = userRepository
                    .findByRoleAndProvinceAndDeletedFalse(UserRole.SHIPPER, province).orElseThrow(
                            () -> new AppException(ErrorCode.SHIPPER_NOT_FOUND));
            order.setShipper(shipper);
        }
        order.setStatus(orderStatus);
        orderRepository.save(order);
        TrackingLog trackingLog = new TrackingLog();
        trackingLog.setOrder(order);
        trackingLog.setStatus(orderStatus);

        if (orderStatus == OrderStatus.CONFIRMED) {
            trackingLog.setNote("Order confirmed and shipper automatically assigned");
        } else if (orderStatus == OrderStatus.PICKING) {
            trackingLog.setNote("Order is being prepared for delivery");
        }
        trackingLogRepository.save(trackingLog);
        return orderMapper.toOrderResponse(order);
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
