package org.example.ecommerc_shop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
// cac loi bat dc thi da liet ke
// loi nao ko bat dc se hien ra 9999
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNOTFOUND(1001, "User not found", HttpStatus.BAD_REQUEST),
    CARTNOTFOUND(1002, "Cart not found", HttpStatus.BAD_REQUEST),
    VARIANTNOTFOUND(1003, "Variant not found", HttpStatus.BAD_REQUEST),
    INVENTORYNOTFOUND(1004, "Inventory not found", HttpStatus.BAD_REQUEST),
    CARTITEMNOTFOUND(1005, "Cart item not found", HttpStatus.BAD_REQUEST),
    INSUFFICIENTSTOCK(1006, "There is insufficient stock of this product.", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1007, "Cart Item not found", HttpStatus.BAD_REQUEST),
    COUPONNOTFOUND(1008, "Coupon not found", HttpStatus.BAD_REQUEST),
    COUPONINVALID(1009, "Coupon not ACTIVE", HttpStatus.BAD_REQUEST),
    COUPON_ALREADY_EXISTS(1010, "Coupon code already exists", HttpStatus.BAD_REQUEST),
            ;

    private int code = 1000;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
