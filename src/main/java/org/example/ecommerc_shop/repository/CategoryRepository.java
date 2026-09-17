package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByNameAndDeletedFalse(String name);
    boolean existsByNameAndDeletedFalseAndIdNot(String name, String id);
    Optional<Category> findByIdAndDeletedFalse(String id);
    Page<Category> findAllByDeletedFalse(Pageable pageable);
    List<Category> findAllByDeletedFalse();
}
