package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.wallet.application.TransactionService;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceClientImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletServiceClientImpl walletServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWalletId_Success() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Wallet wallet = mock(Wallet.class);
        when(wallet.getId()).thenReturn(walletId);
        when(walletService.getWalletByUserId(userId)).thenReturn(wallet);

        assertEquals(walletId, walletServiceClient.getWalletId(userId));
    }

    @Test
    void testGetBalance_Success() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = mock(Wallet.class);
        when(wallet.getBalance()).thenReturn(new BigDecimal("500000"));
        when(walletService.getWalletByUserId(userId)).thenReturn(wallet);

        assertEquals(new BigDecimal("500000"), walletServiceClient.getBalance(userId));
    }

    @Test
    void testCreatePayment_ShouldCreateAndMarkSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");

        Transaction tx = mock(Transaction.class);
        when(tx.getId()).thenReturn(txId);
        when(transactionService.createPayment(buyerWalletId, sellerWalletId, amount)).thenReturn(tx);

        walletServiceClient.createPayment(buyerWalletId, sellerWalletId, amount);

        verify(transactionService).createPayment(buyerWalletId, sellerWalletId, amount);
        verify(transactionService).markSuccess(txId);
    }

    @Test
    void testCreateRefund_ShouldSwapWalletOrderAndMarkSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200000");

        Transaction tx = mock(Transaction.class);
        when(tx.getId()).thenReturn(txId);
        when(transactionService.createRefund(sellerWalletId, buyerWalletId, amount)).thenReturn(tx);

        walletServiceClient.createRefund(buyerWalletId, sellerWalletId, amount);

        verify(transactionService).createRefund(sellerWalletId, buyerWalletId, amount);
        verify(transactionService).markSuccess(txId);
    }
}