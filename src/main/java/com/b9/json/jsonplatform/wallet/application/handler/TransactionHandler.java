package com.b9.json.jsonplatform.wallet.application.handler;

import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;

public interface TransactionHandler {
    TransactionType supportedType();
    void execute(Transaction transaction);
}
