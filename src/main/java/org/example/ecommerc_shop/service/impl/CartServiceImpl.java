package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.request.UpdateCartItemRequest;
import org.example.ecommerc_shop.dto.response.CartItemResponse;
import org.example.ecommerc_shop.dto.response.CartSummaryResponse;
import org.example.ecommerc_shop.entity.CartItem;
import org.example.ecommerc_shop.entity.Inventory;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.CartItemMapper;
import org.example.ecommerc_shop.repository.CartItemRepository;
import org.example.ecommerc_shop.repository.InventoryRepository;
import org.example.ecommerc_shop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final InventoryRepository inventoryRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public List<CartItemResponse> getMyCart(String username) {
        List<CartItem> items = cartItemRepository.findByCart_User_UsernameAndDeletedFalse(username);
        List<CartItemResponse> responses = new ArrayList<>();
        for (CartItem cartItem : items) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null) {
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            Inventory inventory = inventoryRepository
                    .findByProductVariantId(productVariant.getId())
                    .orElseThrow(() ->
                            new AppException(ErrorCode.INVENTORYNOTFOUND)
                    );
            int quantity = cartItem.getQuantity();
            int quantityInStock = inventory.getQuantityInStock();
            String stockStatus;
            if (quantityInStock <= 0 || quantityInStock < quantity) {
                stockStatus = "OUT_OF_STOCK";
            } else {
                stockStatus = "IN_STOCK";
            }
            CartItemResponse response =
                    cartItemMapper.toCartItemResponse(cartItem);
            response.setStockStatus(stockStatus);
            responses.add(response);
        }
        return responses;
    }
    @Override
    @Transactional
    public CartSummaryResponse getCartSummury(String username) {
        List<CartItem> items = cartItemRepository.findByCart_User_UsernameAndDeletedFalse(username);
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : items) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null) {
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            Inventory inventory = inventoryRepository
                    .findByProductVariantId(productVariant.getId())
                    .orElseThrow(() ->
                            new AppException(ErrorCode.INVENTORYNOTFOUND)
                    );
            int quantity = cartItem.getQuantity();
            int quantityInStock = inventory.getQuantityInStock();
            if (quantityInStock >= quantity) {
                BigDecimal itemSubtotal =
                        productVariant.getPrice()
                                .multiply(BigDecimal.valueOf(quantity));
                subtotal = subtotal.add(itemSubtotal);
                totalItems += quantity;
            }
        }
        BigDecimal shippingFee = BigDecimal.valueOf(500000);
        BigDecimal total = subtotal.add(shippingFee);
        return CartSummaryResponse.builder()
                .totalItems(totalItems)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .total(total)
                .build();
    }
    @Override
    @Transactional
    public CartItemResponse updateQuantity(String id, String username, UpdateCartItemRequest updateCartItemRequest) {

        CartItem cartItem = cartItemRepository
                .findByIdAndCart_User_UsernameAndDeletedFalse(id, username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.CARTITEMNOTFOUND)
                );
        ProductVariant productVariant = cartItem.getProductVariant();
        if (productVariant == null) {
            throw new AppException(ErrorCode.VARIANTNOTFOUND);
        }
        Inventory inventory = inventoryRepository
                .findByProductVariantId(productVariant.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.INVENTORYNOTFOUND)
                );
        int newQuantity = updateCartItemRequest.getQuantity();
        if (newQuantity > inventory.getQuantityInStock()) {
            throw new AppException(ErrorCode.INSUFFICIENTSTOCK);
        }
        cartItem.setQuantity(newQuantity);
        CartItemResponse response = cartItemMapper.toCartItemResponse(cartItem);
        String stockStatus;
        if (inventory.getQuantityInStock() <= 0 || inventory.getQuantityInStock() < newQuantity) {
            stockStatus = "OUT_OF_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }
        response.setStockStatus(stockStatus);
        return response;
    }

    @Override
    @Transactional
    public void deleteCartItem(String id, String username) {

        CartItem cartItem = cartItemRepository
                .findByIdAndCart_User_UsernameAndDeletedFalse(id, username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.CARTITEMNOTFOUND)
                );
        cartItem.setDeleted(true);
    }
}