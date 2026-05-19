package com.b9.json.jsonplatform.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private UUID titiperId;
    private UUID jastiperId;

    private UUID productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String shippingAddress;
    
    @Column(nullable = false)
    private String status; 

    @Column(name = "tracking_number")
    private String trackingNumber;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @Column
    private Integer ratingScore;
}