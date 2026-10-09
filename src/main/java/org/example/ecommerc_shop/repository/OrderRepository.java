package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    Page<Order> findAllByUserIdAndDeletedFalse(String id, Pageable pageable);

    Optional<Order> findByIdAndDeletedFalse(String id);

    Optional<Order> findByIdAndUserUsernameAndDeletedFalse(String id, String username);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByDeletedFalse(Pageable pageable);
}