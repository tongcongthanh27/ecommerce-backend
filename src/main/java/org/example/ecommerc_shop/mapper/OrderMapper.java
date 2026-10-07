package org.example.ecommerc_shop.mapper;

import org.aspectj.weaver.ast.Or;
import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
import org.example.ecommerc_shop.dto.response.OrderResponse;
import org.example.ecommerc_shop.dto.response.OrderSummaryResponse;
import org.example.ecommerc_shop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "grandTotal", ignore = true)
    @Mapping(target = "coupon", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(source = "name", target = "recipientName")
    @Mapping(source = "phone", target = "recipientPhone")
    Order toOrder(OrderCreateRequest request);

    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "status", target = "orderStatus")
    @Mapping(source = "createdAt", target = "orderDate")
    OrderSummaryResponse toOrderSummuryResponse(Order order);
}