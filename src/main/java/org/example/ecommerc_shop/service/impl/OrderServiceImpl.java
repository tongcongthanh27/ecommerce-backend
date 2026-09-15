package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.common.PaymentStatus;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderItemResponse;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.entity.*;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.OrderMapper;
import org.example.ecommerc_shop.repository.*;
import org.example.ecommerc_shop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USERNOTFOUND)
                );

        // 2. Lấy cart item của user
        List<CartItem> cartItems =
                cartItemRepository
                        .findByCart_User_UsernameAndDeletedFalse(username);

        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CARTNOTFOUND);
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            ProductVariant variant = cartItem.getProductVariant();

            Inventory inventory = inventoryRepository
                    .findByProductVariantId(variant.getId())
                    .orElseThrow(() ->
                            new AppException(ErrorCode.INVENTORYNOTFOUND)
                    );

            if (inventory.getQuantityInStock() < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENTSTOCK);
            }

            BigDecimal itemSubtotal = variant.getPrice()
                    .multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    );

            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;

        if (request.getCouponCode() != null
                && !request.getCouponCode().isBlank()) {

            coupon = couponRepository
                    .findByCodeAndDeletedFalse(request.getCouponCode())
                    .orElseThrow(() ->
                            new AppException(ErrorCode.COUPONNOTFOUND)
                    );

            if (!coupon.getStatus().name().equals("ACTIVE")) {
                throw new AppException(ErrorCode.COUPONINVALID);
            }

            LocalDateTime now = LocalDateTime.now();

            if (coupon.getStartDate() != null
                    && now.isBefore(coupon.getStartDate())) {
                throw new AppException(ErrorCode.COUPONINVALID);
            }

            if (coupon.getEndDate() != null
                    && now.isAfter(coupon.getEndDate())) {
                throw new AppException(ErrorCode.COUPONINVALID);
            }

            if (coupon.getDiscountType().name().equals("PERCENTAGE")) {

                discount = subtotal
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));

                // max discount
                if (coupon.getMaxDiscountAmount() != null
                        && discount.compareTo(
                        coupon.getMaxDiscountAmount()) > 0) {

                    discount = coupon.getMaxDiscountAmount();
                }

            } else if (coupon.getDiscountType().name().equals("FIXED_AMOUNT")) {

                discount = coupon.getDiscountValue();

                if (discount.compareTo(subtotal) > 0) {
                    discount = subtotal;
                }
            }
        }

        BigDecimal shippingFee = BigDecimal.valueOf(50000);

        BigDecimal grandTotal = subtotal
                .subtract(discount)
                .add(shippingFee);

        Order order = orderMapper.toOrder(request);

        order.setUser(user);
        order.setTrackingNumber(generateTrackingNumber());

        order.setPaymentMethod(PaymentMethod.COD);

        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setStatus(OrderStatus.PENDING);

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setGrandTotal(grandTotal);

        order.setCoupon(coupon);

        Order savedOrder = orderRepository.save(order);

        List<OrderItemResponse> orderItemResponses = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            ProductVariant variant = cartItem.getProductVariant();

            Inventory inventory = inventoryRepository
                    .findByProductVariantId(variant.getId())
                    .orElseThrow(() ->
                            new AppException(ErrorCode.INVENTORYNOTFOUND)
                    );

            inventory.setQuantityInStock(inventory.getQuantityInStock() - cartItem.getQuantity()
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(savedOrder);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());

            orderItem.setUnitPrice(variant.getPrice());

            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            cartItem.setDeleted(true);

            BigDecimal itemSubtotal = variant.getPrice()
                    .multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    );

            orderItemResponses.add(
                    OrderItemResponse.builder()
                            .id(savedOrderItem.getId())
                            .productVariantId(variant.getId())
                            .variantName(variant.getVariantName())
                            .unitPrice(variant.getPrice())
                            .quantity(cartItem.getQuantity())
                            .subtotal(itemSubtotal)
                            .build()
            );
        }

        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
        }

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .trackingNumber(savedOrder.getTrackingNumber())
                .status(savedOrder.getStatus().name())
                .paymentMethod(savedOrder.getPaymentMethod().name())
                .paymentStatus(savedOrder.getPaymentStatus().name())
                .items(orderItemResponses)
                .subtotal(subtotal)
                .discount(discount)
                .shippingFee(shippingFee)
                .grandTotal(grandTotal)
                .couponCode(
                        coupon != null
                                ? coupon.getCode()
                                : null
                )
                .province(savedOrder.getProvince())
                .city(savedOrder.getCity())
                .addressDetail(savedOrder.getAddressDetail())
                .recipientName(savedOrder.getRecipientName())
                .recipientPhone(savedOrder.getRecipientPhone())
                .build();
    }

    private String generateTrackingNumber() {
        return "ORD-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}