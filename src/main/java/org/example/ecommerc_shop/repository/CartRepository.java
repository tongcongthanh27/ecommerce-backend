package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findByUserId(String userId);

    Optional<Cart> findByUserUsername(String username);
}