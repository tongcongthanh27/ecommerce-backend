package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.example.ecommerc_shop.repository.projection.OrderStatusCountProjection;

import java.util.Optional;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    Page<Order> findAllByUserIdAndDeletedFalse(String id, Pageable pageable);

    Optional<Order> findByIdAndDeletedFalse(String id);

    Optional<Order> findByIdAndUserUsernameAndDeletedFalse(String id, String username);

    @Query("""
            SELECT o.status AS status, COUNT(o) AS total
            FROM Order o
            WHERE o.deleted = false
            GROUP BY o.status
            """)
    List<OrderStatusCountProjection> countOrdersByStatus();

    @Query("""
            SELECT COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            WHERE o.deleted = false AND o.paymentStatus = org.example.ecommerc_shop.common.PaymentStatus.PAID
            """)
    java.math.BigDecimal sumPaidOrderTotals();

    @Query("""
            SELECT COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            WHERE o.deleted = false AND o.status = org.example.ecommerc_shop.common.OrderStatus.DELIVERED
            """)
    java.math.BigDecimal sumDeliveredOrderTotals();

    long countByDeletedFalse();

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByShipperIdAndDeletedFalse(String id, Pageable pageable);

    @Query("""
    SELECT o
    FROM Order o
    WHERE (:status IS NULL OR o.status = :status)
      AND (
          :keyword IS NULL
          OR LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(o.recipientName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(o.recipientPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Slice<Order> findForExport(
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
