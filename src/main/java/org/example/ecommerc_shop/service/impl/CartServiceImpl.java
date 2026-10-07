package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.StockStatus;
import org.example.ecommerc_shop.dto.request.AddToCartRequest;
import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;
import org.example.ecommerc_shop.entity.Cart;
import org.example.ecommerc_shop.entity.CartItem;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.entity.User;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.CartItemMapper;
import org.example.ecommerc_shop.mapper.CartMapper;
import org.example.ecommerc_shop.repository.CartItemRepository;
import org.example.ecommerc_shop.repository.CartRepository;
import org.example.ecommerc_shop.repository.ProductVariantRepository;
import org.example.ecommerc_shop.repository.UserRepository;
import org.example.ecommerc_shop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getMyCart(String username) {
        Cart cart = cartRepository.findByUserUsername(username).orElse(null);
        if (cart == null) {
            return CartResponse.builder()
                    .totalItems(0)
                    .subtotal(BigDecimal.ZERO)
                    .items(Collections.emptyList())
                    .build();
        }
        List<CartItem> cartItems = cartItemRepository.findCartItemsWithProductVariant(cart.getId());
        List<CartItemResponse> items = new ArrayList<>();
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();
            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalItems += cartItem.getQuantity();
            subtotal = subtotal.add(itemTotal);
            StockStatus stockStatus ;
            if (variant.getQuantityInStock() > 0) {
                stockStatus = StockStatus.IN_STOCK;
            } else {
                stockStatus = StockStatus.OUT_OF_STOCK;
            }
            CartItemResponse itemResponse = cartItemMapper.toCartItemResponse(cartItem);
            itemResponse.setStockStatus(stockStatus);
            items.add(itemResponse);
        }
        return cartMapper.toCartResponse(cart, items, totalItems, subtotal);
    }

    @Override
    @Transactional
    public CartItemResponse updateQuantity(String id, String username, UpdateCartItemRequest updateCartItemRequest) {
        Cart cart = cartRepository.findByUserUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.CARTNOTFOUND)
        );
        CartItem cartItem = cartItemRepository.findByIdAndCartIdAndDeletedFalse(id, cart.getId()).orElseThrow(
                () -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND)
        );
        int quantity = updateCartItemRequest.getQuantity();
        if (quantity > cartItem.getProductVariant().getQuantityInStock()){
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        cartItem.setQuantity(quantity);
        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @Override
    @Transactional
    public void deleteCartItem(String id, String username) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.CARTNOTFOUND));
        CartItem cartItem = cartItemRepository.findByIdAndCartIdAndDeletedFalse(id, cart.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.setDeleted(true);
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(AddToCartRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNOTFOUND));
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        ProductVariant productVariant = productVariantRepository.findProductVariantByIdAndDeletedFalse(request.getProductVariantId());
        if (productVariant == null) {
            throw new AppException(ErrorCode.VARIANTNOTFOUND);
        }
        CartItem cartItem = cartItemRepository.findByCartIdAndProductVariantIdAndDeletedFalse(cart.getId(), productVariant.getId());
        if (cartItem == null) {
            if (request.getQuantity() > productVariant.getQuantityInStock()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(productVariant);
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > productVariant.getQuantityInStock()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

        return cartItemMapper.toCartItemResponse(cartItem);


    }
}
