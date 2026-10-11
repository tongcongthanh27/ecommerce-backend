package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperOrderResponse implements Serializable {
    private String orderId;
    private String trackingNumber;
    private String customerName;
    private BigDecimal amount;
    private OrderStatus status;
    private String province;
    private String city;
    private String addressDetail;
}
