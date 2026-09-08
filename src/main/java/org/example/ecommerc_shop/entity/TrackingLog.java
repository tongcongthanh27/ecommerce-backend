package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String note;
}
