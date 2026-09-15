package org.example.ecommerc_shop.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ecommerc_shop.common.PaymentMethod;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest implements Serializable {
    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String couponCode;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address detail is required")
    private String addressDetail;

    @NotBlank(message = "Recipient name is required")
    private String name;

    @NotBlank(message = "Recipient phone is required")
    private String phone;
}
