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
    @GeneratedValue
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

    @Column(name = "jastiper_rating")
    private Integer jastiperRating;

    @Column(name = "product_rating")
    private Integer productRating;

    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}