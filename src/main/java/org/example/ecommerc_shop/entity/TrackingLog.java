package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerc_shop.common.OrderStatus;

import java.io.Serializable;

@Entity
@Table(name = "tracking_logs")
@Getter
@Setter
public class TrackingLog extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String note;
}
