package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    boolean existsProductVariantByVariantNameAndDeletedFalse(String variantName);

    boolean existsProductVariantBySkuAndDeletedFalse(String sku);

    boolean existsProductVariantByVariantNameAndIdNotAndDeletedFalse(String variantName, String id);

    boolean existsProductVariantBySkuAndIdNotAndDeletedFalse(String sku, String id);

    ProductVariant findProductVariantByIdAndDeletedFalse(String id);

    List<ProductVariant> findAllByProduct_IdAndDeletedFalse(String productId);

    Page<ProductVariant> findAllByDeletedFalse(Pageable pageable);
}
