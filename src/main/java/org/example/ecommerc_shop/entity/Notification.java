package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerc_shop.common.NotificationStatus;
import org.example.ecommerc_shop.common.NotificationType;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
