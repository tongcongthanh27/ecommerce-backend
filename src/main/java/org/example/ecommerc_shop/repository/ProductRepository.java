package org.example.ecommerc_shop.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.example.ecommerc_shop.entity.Category;
import org.example.ecommerc_shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    boolean existsByNameAndDeletedFalse(String name);
    Optional<Product> findByIdAndDeletedFalse(String id);
    Page<Product> findAllByDeletedFalse(Pageable pageable);
    Page<Product> findByCategoryIdInAndDeletedFalse(List<String> categoryIds, Pageable pageable);
    boolean existsByNameAndDeletedFalseAndIdNot(String name, String id);
}
