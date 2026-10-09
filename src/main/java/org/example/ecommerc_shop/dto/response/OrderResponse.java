package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.common.PaymentStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse implements Serializable {

    private String id;

    private String trackingNumber;

    private OrderStatus status;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private List<OrderItemResponse> items;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal shippingFee;

    private BigDecimal grandTotal;

    private String couponCode;

    private String province;

    private String city;

    private String addressDetail;

    private String recipientName;

    private String recipientPhone;
}
