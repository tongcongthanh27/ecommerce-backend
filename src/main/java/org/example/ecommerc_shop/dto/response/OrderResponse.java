package org.example.ecommerc_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse implements Serializable {

    private String id;

    private String trackingNumber;

    private String status;

    private String paymentMethod;

    private String paymentStatus;

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
