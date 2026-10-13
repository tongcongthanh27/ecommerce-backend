package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantUpdateRequest implements Serializable {

    @Size(min = 1, max = 255)
    private String variantName;

    @Size(min = 1, max = 100)
    private String sku;

    @Min(0)
    private Integer weightGram;

    @DecimalMin(value = "0.0")
    private BigDecimal price;

    @Min(0)
    private Integer quantityInStock;

    private MultipartFile image;
}
