package org.example.ecommerc_shop.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;
import org.example.ecommerc_shop.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/carts")
public class CartController {
    private final CartService cartService;

    @GetMapping("/items")
    public ApiResponse<List<CartItemResponse>> getMyCart(Principal principal){
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getMyCart(principal.getName()))
                .build();
    }

    @GetMapping("/summury")
    public ApiResponse<CartSummaryResponse> getCartSummury(Principal principal){
        return ApiResponse.<CartSummaryResponse>builder()
                .result(cartService.getCartSummury(principal.getName()))
                .build();
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartItemResponse> updateQuantity(@PathVariable String id, Principal principal, @RequestBody UpdateCartItemRequest updateCartItemRequest){
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.updateQuantity(id, principal.getName(), updateCartItemRequest))
                .build();

    }
    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteCartItems(@PathVariable String id, Principal principal){
        cartService.deleteCartItem(id, principal.getName());
        return ApiResponse.<Void>builder()
                .message("Cart item deleted successfully")
                .build();
    }
}
