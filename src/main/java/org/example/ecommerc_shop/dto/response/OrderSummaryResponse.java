package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse implements Serializable {
    private String id;
    
    private String trackingNumber;

    private LocalDateTime orderDate;

    private PaymentMethod paymentMethod;

    private BigDecimal grandTotal;

    private OrderStatus orderStatus;
}
