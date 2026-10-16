package org.example.ecommerc_shop.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ecommerc_shop.common.OrderStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class TrackingLogResponse {
    private String id;
    private OrderStatus status;
    private String location;
    private String note;
    private LocalDateTime createdAt;
}
