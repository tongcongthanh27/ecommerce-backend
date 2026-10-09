package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query("""
        SELECT oi
        FROM OrderItem oi
        JOIN FETCH oi.productVariant pv
        WHERE oi.order.id = :orderId
    """)
    List<OrderItem> findOrderItemsWithProductVariant(
            @Param("orderId") String orderId
    );
}