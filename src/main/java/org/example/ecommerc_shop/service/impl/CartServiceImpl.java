package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;
import org.example.ecommerc_shop.entity.*;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.CartItemMapper;
import org.example.ecommerc_shop.repository.*;
import org.example.ecommerc_shop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public List<CartItemResponse> getMyCart(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARTNOTFOUND)
        );
        int totalItems = 0;

        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        for (CartItem cartItem : items){
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null){
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }

            Inventory inventory = inventoryRepository.findByProductVariantId(productVariant.getId()).orElseThrow(
                    () -> new AppException(ErrorCode.INVENTORYNOTFOUND)
            );

            int quantity = cartItem.getQuantity();
            int quantityInStock = inventory.getQuantityInStock();
            String stockStatus;
            if (quantityInStock <= 0 || quantityInStock < quantity) {
                stockStatus = "OUT_OF_STOCK";
            }
             else {
                stockStatus = "IN_STOCK";
            }
            totalItems += quantity;

            CartItemResponse itemResponse =
                    cartItemMapper.toCartItemResponse(cartItem);

            itemResponse.setStockStatus(stockStatus);

            cartItemResponses.add(itemResponse);
        }
        return cartItemResponses;
    }

    @Override
    public CartSummaryResponse getCartSummury(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARTNOTFOUND)
        );
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        for (CartItem cartItem : items){
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null){
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            Inventory inventory = inventoryRepository.findByProductVariantId(productVariant.getId()).orElseThrow(
                    () -> new AppException(ErrorCode.INVENTORYNOTFOUND)
            );
            int quantity = cartItem.getQuantity();
            int quantityInStock = inventory.getQuantityInStock();
            if (quantityInStock  >= quantity) {
                BigDecimal itemSubtotal =
                        productVariant.getPrice()
                                .multiply(BigDecimal.valueOf(quantity));
                subtotal = subtotal.add(itemSubtotal);
                totalItems += quantity;
            }
        }
        BigDecimal shippingfee = BigDecimal.valueOf(500000);
        BigDecimal total = subtotal.add(shippingfee);

       return CartSummaryResponse.builder()
               .totalItems(totalItems)
               .total(total)
               .shippingFee(shippingfee)
               .subtotal(subtotal)
               .build();
    }

    @Override
    public CartItemResponse updateQuantity(String id, String username, UpdateCartItemRequest updateCartItemRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USERNOTFOUND)
        );

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARTNOTFOUND)
        );

        CartItem cartItem = cartItemRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.CARTITEMNOTFOUND)
        );
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.CARTITEMNOTFOUND);
        }
        ProductVariant productVariant = cartItem.getProductVariant();
        if (productVariant == null){
            throw new AppException(ErrorCode.VARIANTNOTFOUND);
        }
        Inventory inventory = inventoryRepository
                .findByProductVariantId(productVariant.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.INVENTORYNOTFOUND)
                );
        int newQuantity = updateCartItemRequest.getQuantity();
        if (newQuantity > inventory.getQuantityInStock()){
            throw new AppException(ErrorCode.INSUFFICIENTSTOCK);
        }
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        CartItemResponse response =
                cartItemMapper.toCartItemResponse(cartItem);

        int quantityInStock = inventory.getQuantityInStock();

        String stockStatus;
        if (quantityInStock <= 0 || quantityInStock < newQuantity) {
            stockStatus = "OUT_OF_STOCK";
        }
        else {
            stockStatus = "IN_STOCK";
        }
        response.setStockStatus(stockStatus);
        return response;
    }
}
