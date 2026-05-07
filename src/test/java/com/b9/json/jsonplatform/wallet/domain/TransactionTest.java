package com.b9.json.jsonplatform.wallet.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionTest {
    @Test
    void testConstructor() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");
        String description = "Top up via bank transfer";

        Transaction transaction = new Transaction(walletId, TransactionType.TOP_UP, amount, description);

        assertEquals(walletId, transaction.getWalletId());
        assertEquals(TransactionType.TOP_UP, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(description, transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
    }
}
