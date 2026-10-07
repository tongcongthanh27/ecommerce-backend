package org.example.ecommerc_shop.repository;

import org.aspectj.weaver.ast.Or;
import org.example.ecommerc_shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserIdAndDeletedFalse(String id, Pageable pageable);
    Order findByIdAndDeletedFalse(String id);
    Optional<Order> findByIdAndUserUsernameAndDeletedFalse(String id, String username);
}
