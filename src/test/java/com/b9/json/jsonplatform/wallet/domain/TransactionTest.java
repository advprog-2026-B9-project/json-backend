package com.b9.json.jsonplatform.wallet.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void testConstructor_singleWalletOverload_setsTargetWalletIdToNull() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(), TransactionType.WITHDRAWAL,
                new BigDecimal("10"), "Withdrawal");

        assertNull(transaction.getTargetWalletId());
    }

    @Test
    void testConstructor_dualWalletOverload_setsTargetWalletId() {
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        Transaction transaction = new Transaction(
                walletId, targetWalletId, TransactionType.PAYMENT,
                new BigDecimal("25"), "Payment");

        assertEquals(walletId, transaction.getWalletId());
        assertEquals(targetWalletId, transaction.getTargetWalletId());
        assertEquals(TransactionType.PAYMENT, transaction.getType());
    }

    @Test
    void testConstructor_dualWalletOverload_acceptsNullTargetWalletId() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(), null, TransactionType.TOP_UP,
                new BigDecimal("10"), "Top Up");

        assertNull(transaction.getTargetWalletId());
    }

    @Test
    void testConstructor_defaultsStatusToPending() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(), TransactionType.REFUND,
                new BigDecimal("5"), "Refund");

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    void testConstructor_timestampIsSetToNow() {
        LocalDateTime before = LocalDateTime.now();
        Transaction transaction = new Transaction(
                UUID.randomUUID(), TransactionType.TOP_UP,
                new BigDecimal("1"), "Top Up");
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(transaction.getTimestamp());
        // timestamp must fall within the [before, after] interval
        assertEquals(false, transaction.getTimestamp().isBefore(before));
        assertEquals(false, transaction.getTimestamp().isAfter(after));
    }

    @Test
    void testSetStatus_updatesField() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(), TransactionType.TOP_UP,
                new BigDecimal("1"), "Top Up");

        transaction.setStatus(TransactionStatus.SUCCESS);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
    }

    @Test
    void testSetId_andGetId_roundTrip() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(), TransactionType.TOP_UP,
                new BigDecimal("1"), "Top Up");
        UUID id = UUID.randomUUID();
        transaction.setId(id);
        assertEquals(id, transaction.getId());
    }
}
