package com.b9.json.jsonplatform.wallet.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // TOP_UP, WITHDRAWAL, PAYMENT, REFUND

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String description;

    protected Transaction() {}

    public Transaction(UUID walletId, TransactionType type, BigDecimal amount, String description) {
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }
}