package org.example.ecommerc_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUrlResponse {
    private String orderId;
    private String paymentUrl;
}
