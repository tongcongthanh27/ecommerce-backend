package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.common.PaymentTransactionStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
}
