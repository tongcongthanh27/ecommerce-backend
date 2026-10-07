package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.AddToCartRequest;
import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;
import org.example.ecommerc_shop.service.CartService;
import org.example.ecommerc_shop.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/carts")
@Validated
public class CartController {
    private final CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addToCart(@Valid @RequestBody AddToCartRequest request, Principal principal){

        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.addToCart(request, principal.getName()))
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/items")
    public ApiResponse<CartResponse> getMyCart(Principal principal){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getMyCart(principal.getName()))
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> deleteCartItem(@PathVariable String cartItemId, Principal principal){
        cartService.deleteCartItem(cartItemId, principal.getName());
        return ApiResponse.<Void>builder()
                .message("Xoa san pham thanh cong")
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateQuantity(@PathVariable String cartItemId, Principal principal, @Valid @RequestBody UpdateCartItemRequest request){
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.updateQuantity(cartItemId, principal.getName(), request))
                .build();
    }
}
