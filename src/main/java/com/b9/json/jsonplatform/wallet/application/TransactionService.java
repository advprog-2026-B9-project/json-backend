package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(
            UUID userId,
            TransactionType type,
            BigDecimal amount,
            String description
    );
    Transaction markSuccess(UUID transactionId);
    Transaction markFailed(UUID transactionId);
    List<Transaction> getUserTransactions(UUID userId);
}