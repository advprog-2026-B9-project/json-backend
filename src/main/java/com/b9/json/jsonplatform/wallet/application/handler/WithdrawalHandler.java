package com.b9.json.jsonplatform.wallet.application.handler;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalHandler implements TransactionHandler {

    private final WalletService walletService;

    public WithdrawalHandler(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public TransactionType supportedType() {
        return TransactionType.WITHDRAWAL;
    }

    @Override
    public void execute(Transaction transaction) {
        walletService.decreaseBalance(transaction.getWalletId(), transaction.getAmount());
    }
}
