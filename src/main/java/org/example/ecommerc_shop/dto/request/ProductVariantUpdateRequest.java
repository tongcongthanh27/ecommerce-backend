package org.example.ecommerc_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private String variantName;

    private String sku;

    private Integer weightGram;

    private BigDecimal price;

    private Integer quantityInStock;

    private MultipartFile image;
}
