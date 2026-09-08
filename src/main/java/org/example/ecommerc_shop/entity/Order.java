package org.example.ecommerc_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerc_shop.common.OrderStatus;
import org.example.ecommerc_shop.common.PaymentMethod;
import org.example.ecommerc_shop.common.PaymentStatus;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "grand_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal grandTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String city;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;
}
