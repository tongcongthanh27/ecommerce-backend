package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.common.NotificationStatus;
import org.example.ecommerc_shop.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @EntityGraph(attributePaths = {"user"})
    List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status, Pageable pageable);
}