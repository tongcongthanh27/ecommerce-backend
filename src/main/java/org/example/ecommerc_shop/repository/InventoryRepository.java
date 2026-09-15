package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByProductVariantId(String productVariantId);

    List<Inventory> findByProductVariantIdIn(List<String> productVariantIds);
}
