package com.b9.json.jsonplatform.order.application.external;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletServiceClient {
    UUID getWalletId(UUID userId);
    BigDecimal getBalance(UUID userId);
    void createPayment(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount);
    void createRefund(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount);
}