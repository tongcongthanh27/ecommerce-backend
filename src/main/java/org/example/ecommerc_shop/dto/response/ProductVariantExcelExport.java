package org.example.ecommerc_shop.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantExcelExport implements Serializable {

    @ExcelProperty("Product ID")
    private String productId;

    @ExcelProperty("Product Name")
    private String productName;

    @ExcelProperty("Variant Name")
    private String variantName;

    @ExcelProperty("SKU")
    private String sku;

    @ExcelProperty("Price")
    private BigDecimal price;

    @ExcelProperty("Weight (gram)")
    private Integer weightGram;

    @ExcelProperty("Quantity In Stock")
    private Integer quantityInStock;

    @ExcelProperty("Image URL")
    private String imageUrl;

    @ExcelProperty("Created At")
    private LocalDateTime createdAt;
}
