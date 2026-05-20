package com.b9.json.jsonplatform.wallet.application.handler;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class PaymentHandler implements TransactionHandler {

    private final WalletService walletService;

    public PaymentHandler(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public TransactionType supportedType() {
        return TransactionType.PAYMENT;
    }

    @Override
    public void execute(Transaction transaction) {
        if (transaction.getTargetWalletId() == null) {
            throw new IllegalStateException("Target wallet is required for PAYMENT");
        }
        walletService.decreaseBalance(transaction.getWalletId(), transaction.getAmount());
        walletService.increaseBalance(transaction.getTargetWalletId(), transaction.getAmount());
    }
}
