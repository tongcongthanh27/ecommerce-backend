package org.example.ecommerc_shop.dto.response;

import lombok.*;
import org.example.ecommerc_shop.common.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse implements Serializable {

    private String orderId;

    private String customerName;

    private LocalDateTime createdDate;

    private BigDecimal amount;

    private OrderStatus status;

    private String province;

    private String city;

    private String addressDetail;
}