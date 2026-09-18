package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
public class ProductVariant extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "sku")
    private String sku;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "weight_gram", nullable = false)
    private Integer weightGram;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_public_id")
    private String imagePublicId;

    @Column(name = "quantity_in_stock")
    private Integer quantityInStock;
}
