package org.example.ecommerc_shop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(403, "You are not allowed to access this order", HttpStatus.FORBIDDEN),

    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(1002, "Cart not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND(1003, "Product variant not found", HttpStatus.NOT_FOUND),
    INVENTORY_NOT_FOUND(1004, "Inventory not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(1005, "Cart item not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_CART_STOCK(1006, "There is insufficient stock of this product", HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOUND(1008, "Coupon not found", HttpStatus.NOT_FOUND),
    INVALID_COUPON(1009, "Coupon is not active or valid", HttpStatus.BAD_REQUEST),
    COUPON_ALREADY_EXISTS(1010, "Coupon code already exists", HttpStatus.CONFLICT),
    CATEGORY_NAME_ALREADY_EXISTS(1011, "Category name already exists", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(1012, "Category not found", HttpStatus.NOT_FOUND),
    INVALID_PARENT_CATEGORY(1013, "Category cannot be its own ancestor", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1014, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1015, "Email already exists", HttpStatus.CONFLICT),
    PHONE_NUMBER_ALREADY_EXISTS(1016, "Phone number already exists", HttpStatus.CONFLICT),
    PRODUCT_NAME_ALREADY_EXISTS(1017, "Product name already exists", HttpStatus.CONFLICT),
    PRODUCT_NOT_FOUND(1018, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NAME_ALREADY_EXISTS(1019, "Product variant name already exists", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_SKU_ALREADY_EXISTS(1020, "Product variant SKU already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_ORDER_INVENTORY(1021, "Insufficient product inventory", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1022, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_TRACKING_NOT_AVAILABLE(1035, "Order tracking is available after confirmation", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(1023, "Product is out of stock", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_CANCELLED(1024, "Order cannot be cancelled at its current status", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_TRANSITION(1025, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    SHIPPER_NOT_FOUND(1026, "No shipper found for this order", HttpStatus.NOT_FOUND),
    INVALID_COUPON_DATES(1027, "Coupon end date must be after start date", HttpStatus.BAD_REQUEST),
    INVALID_COUPON_DISCOUNT(1028, "Percentage discount cannot exceed 100", HttpStatus.BAD_REQUEST),
    INVALID_PAGINATION(1029, "Page number and page size must be positive", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT(1030, "Variant name and SKU must not be blank", HttpStatus.BAD_REQUEST),
    INVALID_PRICE_RANGE(1031, "Minimum price cannot exceed maximum price", HttpStatus.BAD_REQUEST),
    INVALID_COUPON_VALUES(1032, "Coupon limits and status are invalid", HttpStatus.BAD_REQUEST),
    INVALID_USER_ROLE(1033, "User role is required", HttpStatus.BAD_REQUEST),
    INVALID_USER_PROFILE(1034, "User profile fields cannot be blank", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_AVAILABLE(1036, "VNPAY payment is not available for this order", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
