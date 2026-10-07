package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest implements Serializable {

    @NotBlank
    private String productVariantId;

    @NotNull
    @Min(1)
    private Integer quantity;
}