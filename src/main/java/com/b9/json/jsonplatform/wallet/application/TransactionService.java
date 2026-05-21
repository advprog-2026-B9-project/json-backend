package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(
            UUID walletId,
            TransactionType type,
            BigDecimal amount,
            String description
    );
    Transaction markSuccess(UUID transactionId);
    Transaction markFailed(UUID transactionId);
    List<Transaction> getWalletTransactions(UUID walletId);
    Transaction createTopUp(UUID walletId, BigDecimal amount);
    Transaction createWithdrawal(UUID walletId, BigDecimal amount);
    Transaction createPayment(UUID walletId, UUID targetWalletId, BigDecimal amount);
    Transaction createRefund(UUID walletId, UUID targetWalletId, BigDecimal amount);
}