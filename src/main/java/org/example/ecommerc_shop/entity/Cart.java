package org.example.ecommerc_shop.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;
}
