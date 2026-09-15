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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final InventoryRepository inventoryRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getMyCart(String username) {
        List<CartItem> items = cartItemRepository.findDetailedByUsername(username);
        Map<String, Integer> quantityInStockByVariantId = getQuantityInStockByVariantId(items);
        List<CartItemResponse> responses = new ArrayList<>();
        for (CartItem cartItem : items) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null) {
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            int quantity = cartItem.getQuantity();
            Integer quantityInStock = quantityInStockByVariantId.get(productVariant.getId());
            if (quantityInStock == null) {
                throw new AppException(ErrorCode.INVENTORYNOTFOUND);
            }
            CartItemResponse response =
                    cartItemMapper.toCartItemResponse(cartItem);
            response.setStockStatus(resolveStockStatus(quantityInStock, quantity));
            responses.add(response);
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public CartSummaryResponse getCartSummury(String username) {
        List<CartItem> items = cartItemRepository.findDetailedByUsername(username);
        Map<String, Integer> quantityInStockByVariantId = getQuantityInStockByVariantId(items);
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : items) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null) {
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            int quantity = cartItem.getQuantity();
            Integer quantityInStock = quantityInStockByVariantId.get(productVariant.getId());
            if (quantityInStock == null) {
                throw new AppException(ErrorCode.INVENTORYNOTFOUND);
            }
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
                .findDetailedByIdAndUsername(id, username)
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
        response.setStockStatus(resolveStockStatus(inventory.getQuantityInStock(), newQuantity));
        return response;
    }

    @Override
    @Transactional
    public void deleteCartItem(String id, String username) {
        int updatedRows = cartItemRepository.softDeleteByIdAndUsername(id, username);
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.CARTITEMNOTFOUND);
        }
    }

    private Map<String, Integer> getQuantityInStockByVariantId(List<CartItem> items) {
        Set<String> variantIds = new HashSet<>();
        for (CartItem cartItem : items) {
            ProductVariant productVariant = cartItem.getProductVariant();
            if (productVariant == null) {
                throw new AppException(ErrorCode.VARIANTNOTFOUND);
            }
            variantIds.add(productVariant.getId());
        }

        Map<String, Integer> quantityInStockByVariantId = new HashMap<>();
        if (variantIds.isEmpty()) {
            return quantityInStockByVariantId;
        }

        List<Inventory> inventories = inventoryRepository.findByProductVariantIdIn(new ArrayList<>(variantIds));
        for (Inventory inventory : inventories) {
            quantityInStockByVariantId.put(inventory.getProductVariant().getId(), inventory.getQuantityInStock());
        }
        return quantityInStockByVariantId;
    }

    private String resolveStockStatus(int quantityInStock, int requestedQuantity) {
        if (quantityInStock <= 0 || quantityInStock < requestedQuantity) {
            return "OUT_OF_STOCK";
        }
        return "IN_STOCK";
    }
}
