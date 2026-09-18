package org.example.ecommerc_shop.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.example.ecommerc_shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsByNameAndDeletedFalse(String name);
    Optional<Product> findByIdAndDeletedFalse(String id);
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    boolean existsByNameAndDeletedFalseAndIdNot(String name, String id);
}
