package com.b9.json.jsonplatform.wallet.application.handler;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class RefundHandler implements TransactionHandler {

    private final WalletService walletService;

    public RefundHandler(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public TransactionType supportedType() {
        return TransactionType.REFUND;
    }

    @Override
    public void execute(Transaction transaction) {
        if (transaction.getTargetWalletId() == null) {
            throw new IllegalStateException("Target wallet is required for REFUND");
        }
        walletService.increaseBalance(transaction.getWalletId(), transaction.getAmount());
        walletService.decreaseBalance(transaction.getTargetWalletId(), transaction.getAmount());
    }
}
