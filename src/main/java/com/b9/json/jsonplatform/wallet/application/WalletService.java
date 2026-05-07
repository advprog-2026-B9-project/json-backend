package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    Wallet createWallet(UUID userId);
    Wallet getWalletById(UUID walletId);
    Wallet getWalletByUserId(UUID userId);
    void increaseBalance(UUID userId, BigDecimal amount);
    void decreaseBalance(UUID userId, BigDecimal amount);
}