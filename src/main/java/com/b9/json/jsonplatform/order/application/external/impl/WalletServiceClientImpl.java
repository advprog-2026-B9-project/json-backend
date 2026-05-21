package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import com.b9.json.jsonplatform.wallet.application.TransactionService;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletServiceClientImpl implements WalletServiceClient {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @Override
    public UUID getWalletId(UUID userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return wallet.getId();
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return wallet.getBalance();
    }

    @Override
    public void createPayment(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        var payment = transactionService.createPayment(buyerWalletId, sellerWalletId, amount);
        transactionService.markSuccess(payment.getId());
    }

    @Override
    public void createRefund(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        var refund = transactionService.createRefund(sellerWalletId, buyerWalletId, amount);
        transactionService.markSuccess(refund.getId());
    }
}