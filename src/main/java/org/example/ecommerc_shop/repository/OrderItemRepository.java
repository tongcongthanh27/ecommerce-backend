package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
