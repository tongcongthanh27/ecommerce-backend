package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    CartItem findByCartIdAndProductVariantIdAndDeletedFalse(String cartId, String productVariantId);
    @Query("""
    SELECT ci
    FROM CartItem ci
    JOIN FETCH ci.productVariant pv
    JOIN FETCH pv.product p
    WHERE ci.cart.id = :cartId
      AND ci.deleted = false""")
    List<CartItem> findCartItemsWithProductVariant(@Param("cartId") String cartId);
    Optional<CartItem> findByIdAndCartIdAndDeletedFalse(String cartItemId, String cartId);
}
