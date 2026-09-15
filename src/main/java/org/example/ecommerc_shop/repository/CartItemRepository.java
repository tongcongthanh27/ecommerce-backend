package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByCartIdAndDeletedFalse(String cartId);

    List<CartItem> findByCart_User_UsernameAndDeletedFalse(String username);

    @Query("""
            select distinct ci
            from CartItem ci
            join fetch ci.productVariant pv
            join fetch pv.product
            join ci.cart c
            join c.user u
            where u.username = :username
              and ci.deleted = false
            """)
    List<CartItem> findDetailedByUsername(@Param("username") String username);

    @Query("""
            select ci
            from CartItem ci
            join fetch ci.productVariant pv
            join fetch pv.product
            join ci.cart c
            join c.user u
            where ci.id = :id
              and u.username = :username
              and ci.deleted = false
            """)
    Optional<CartItem> findDetailedByIdAndUsername(@Param("id") String id, @Param("username") String username);

    Optional<CartItem> findByIdAndCart_User_UsernameAndDeletedFalse(String id, String username);

    @Modifying
    @Query("""
            update CartItem ci
            set ci.deleted = true
            where ci.id = :id
              and ci.deleted = false
              and ci.cart.user.username = :username
            """)
    int softDeleteByIdAndUsername(@Param("id") String id, @Param("username") String username);
}
