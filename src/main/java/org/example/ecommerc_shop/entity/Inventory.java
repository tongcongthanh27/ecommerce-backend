package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "inventories")
@Getter
@Setter
public class Inventory extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_variant_id",
            nullable = false,
            unique = true
    )
    private ProductVariant productVariant;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;
}
