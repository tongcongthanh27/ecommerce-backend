package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.example.ecommerc_shop.repository.projection.ProductVariantStatisticsProjection;

import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String>, JpaSpecificationExecutor<ProductVariant> {
    @Query("""
            SELECT variant
            FROM ProductVariant variant
            JOIN FETCH variant.product product
            WHERE variant.deleted = false
              AND product.deleted = false
            """)
    Slice<ProductVariant> findForExport(Pageable pageable);

    boolean existsProductVariantByVariantNameAndDeletedFalse(String variantName);

    boolean existsProductVariantBySkuAndDeletedFalse(String sku);

    boolean existsProductVariantByVariantNameAndIdNotAndDeletedFalse(String variantName, String id);

    boolean existsByProduct_IdAndVariantNameAndDeletedFalse(String productId, String variantName);

    boolean existsByProduct_IdAndVariantNameAndDeletedFalseAndIdNot(String productId, String variantName, String id);

    boolean existsProductVariantBySkuAndIdNotAndDeletedFalse(String sku, String id);

    ProductVariant findProductVariantByIdAndDeletedFalse(String id);

    List<ProductVariant> findAllByProduct_IdAndDeletedFalse(String productId);

    Page<ProductVariant> findAllByDeletedFalse(Pageable pageable);

    @Query("""
            SELECT COUNT(v) AS totalVariants,
                   SUM(CASE WHEN COALESCE(v.quantityInStock, 0) > 0 THEN 1 ELSE 0 END) AS inStockVariants,
                   SUM(CASE WHEN COALESCE(v.quantityInStock, 0) BETWEEN 1 AND :lowStockThreshold THEN 1 ELSE 0 END) AS lowStockVariants,
                   SUM(CASE WHEN COALESCE(v.quantityInStock, 0) <= 0 THEN 1 ELSE 0 END) AS outOfStockVariants,
                   SUM(COALESCE(v.quantityInStock, 0)) AS totalQuantityInStock,
                   AVG(v.price) AS averagePrice
            FROM ProductVariant v
            WHERE v.deleted = false
              AND v.product.deleted = false
            """)
    ProductVariantStatisticsProjection getStatistics(@Param("lowStockThreshold") int lowStockThreshold);
}
