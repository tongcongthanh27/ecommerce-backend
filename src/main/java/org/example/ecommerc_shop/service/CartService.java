package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getMyCart(String username);

    CartSummaryResponse getCartSummury(String username);

    CartItemResponse updateQuantity(String id, String username, UpdateCartItemRequest updateCartItemRequest);

    void deleteCartItem(String id, String username);
}