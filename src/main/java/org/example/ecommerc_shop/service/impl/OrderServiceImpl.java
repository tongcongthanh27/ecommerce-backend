package org.example.ecommerc_shop.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.*;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.*;
import org.example.ecommerc_shop.entity.*;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.OrderItemMapper;
import org.example.ecommerc_shop.mapper.OrderMapper;
import org.example.ecommerc_shop.mapper.TracklingLogMapper;
import org.example.ecommerc_shop.repository.*;
import org.example.ecommerc_shop.service.OrderService;
import org.example.ecommerc_shop.service.specification.OrderSpec;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final int BATCH_SIZE = 500;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;
    private final TracklingLogMapper tracklingLogMapper;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final TrackingLogRepository trackingLogRepository;
    private final NotificationRepository notificationRepository;
    private final OrderTransactionService orderTransactionService;

    @Override
    public OrderResponse createOrder(OrderCreateRequest request, String username) {
        return orderTransactionService.createOrderTransaction(request, username);
    }

    @Override
    @Transactional
    public Page<OrderSummaryResponse> getMyOrders(Integer pageSize, Integer pageNumber, String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAllByUserIdAndDeletedFalse(user.getId(), pageable);
        return orderPage.map(orderMapper::toOrderSummuryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrdersByStatus(Integer pageSize, Integer pageNumber, String username, OrderStatus status) {
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
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
    @Transactional(readOnly = true)
    public List<TrackingLogResponse> getOrderTracking(String orderId, String username) {
        Order order = orderRepository.findByIdAndUserUsernameAndDeletedFalse(orderId, username)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != OrderStatus.CONFIRMED
                && order.getStatus() != OrderStatus.PICKING
                && order.getStatus() != OrderStatus.SHIPPING
                && order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.FAILED
                && order.getStatus() != OrderStatus.REATTEMPT
                && order.getStatus() != OrderStatus.RETURNING) {
            throw new AppException(ErrorCode.ORDER_TRACKING_NOT_AVAILABLE);
        }
        return trackingLogRepository.findByOrderIdAndDeletedFalseOrderByCreatedAtAsc(orderId).stream()
                .map(tracklingLogMapper::toTrackingLogResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsResponse getStatistics() {
        java.util.Map<OrderStatus, Long> byStatus = new java.util.EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) byStatus.put(status, 0L);
        orderRepository.countOrdersByStatus().forEach(row -> byStatus.put(row.getStatus(), row.getTotal()));
        return OrderStatisticsResponse.builder()
                .totalOrders(orderRepository.countByDeletedFalse())
                .ordersByStatus(byStatus)
                .totalRevenue(orderRepository.sumPaidOrderTotals())
                .deliveredRevenue(orderRepository.sumDeliveredOrderTotals())
                .build();
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

    @Override
    @Transactional
    public Page<ShipperOrderResponse> getAllOrderForShipper(Integer pageSize, Integer pageNumber, String shipperName) {
        User shipper = userRepository.findByUsernameAndDeletedFalse(shipperName).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAllByShipperIdAndDeletedFalse(shipper.getId(), pageable);
        return orderPage.map(orderMapper::toShipperOrderResponse);
    }

    @Override
    @Transactional
    public ShipperOrderResponse updateOrderStatusByShipper(String orderId, OrderStatus orderStatus, String shipperName) {
        Order order = orderRepository.findByIdAndDeletedFalse(orderId).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getShipper() == null || !order.getShipper().getUsername().equals(shipperName)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        boolean allowedByShipper = (order.getStatus() == OrderStatus.PICKING && orderStatus == OrderStatus.SHIPPING)
                        || (order.getStatus() == OrderStatus.SHIPPING && (orderStatus == OrderStatus.DELIVERED || orderStatus == OrderStatus.FAILED));
        if (!allowedByShipper || !order.getStatus().canTransitionTo(orderStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_ORDER_STATUS_TRANSITION
            );
        }
        order.setStatus(orderStatus);
        createOrderNotification(order, orderStatus);
        TrackingLog trackingLog = new TrackingLog();
        trackingLog.setOrder(order);
        trackingLog.setStatus(orderStatus);
        switch (orderStatus) {
            case SHIPPING:
                trackingLog.setNote("Shipper is delivering the order");
                break;
            case DELIVERED:
                trackingLog.setNote("Order delivered successfully");
                break;
            case FAILED:
                trackingLog.setNote("Delivery failed");
                break;
            default:
                trackingLog.setNote("Order status updated by shipper");
        }
        trackingLogRepository.save(trackingLog);
        return orderMapper.toShipperOrderResponse(order);
    }

    private void createOrderNotification(Order order, OrderStatus status) {
        String title;
        String content;
        switch (status) {
            case SHIPPING -> {
                title = "Đơn hàng đang được giao";
                content = "Đơn hàng " + order.getTrackingNumber()
                        + " đang được shipper giao đến bạn.";
            }
            case DELIVERED -> {
                title = "Giao hàng thành công";
                content = "Đơn hàng " + order.getTrackingNumber()
                        + " đã được giao thành công.";
            }
            default -> {
                return;
            }
        }
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setOrder(order);
        notification.setType(NotificationType.EMAIL);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.PENDING);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportOrders(OrderStatus status, String keyword, OutputStream outputStream) {
        try (ExcelWriter writer = EasyExcel.write(outputStream, OrderExcelExport.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("Orders").build();
            int pageNo = 0;
            while (true) {
                Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                );
                Slice<Order> slice = orderRepository.findForExport(status, keyword, pageable);
                if (slice.isEmpty()) {
                    break;
                }
                List<OrderExcelExport> batch = slice.getContent().stream().map(orderMapper::toExcelExport).toList();
                writer.write(batch, sheet);
                if (!slice.hasNext()) {
                    break;
                }
                pageNo++;
            }
        }
    }
}
