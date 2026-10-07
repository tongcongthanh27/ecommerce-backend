package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.TrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingLogRepository extends JpaRepository<TrackingLog, String> {
    List<TrackingLog> findByOrderIdOrderByCreatedAtAsc(String orderId);
}