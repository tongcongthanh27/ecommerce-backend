package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByCartIdAndDeletedFalse(String cartId);

    List<CartItem> findByCart_User_UsernameAndDeletedFalse(String username);

    Optional<CartItem> findByIdAndCart_User_UsernameAndDeletedFalse(String id, String username);
}