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
public class OrderExcelExport implements Serializable {

    @ExcelProperty("Tracking Number")
    private String trackingNumber;

    @ExcelProperty("Recipient Name")
    private String recipientName;

    @ExcelProperty("Recipient Phone")
    private String recipientPhone;

    @ExcelProperty("Status")
    private String status;

    @ExcelProperty("Payment Method")
    private String paymentMethod;

    @ExcelProperty("Payment Status")
    private String paymentStatus;

    @ExcelProperty("Subtotal")
    private BigDecimal subtotal;

    @ExcelProperty("Discount")
    private BigDecimal discount;

    @ExcelProperty("Shipping Fee")
    private BigDecimal shippingFee;

    @ExcelProperty("Grand Total")
    private BigDecimal grandTotal;

    @ExcelProperty("Province")
    private String province;

    @ExcelProperty("City")
    private String city;

    @ExcelProperty("Address")
    private String addressDetail;

    @ExcelProperty("Created At")
    private LocalDateTime createdAt;
}
