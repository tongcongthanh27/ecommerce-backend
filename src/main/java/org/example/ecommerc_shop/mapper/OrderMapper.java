package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.OrderCreateRequest;
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
}