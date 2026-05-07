package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionStatus;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import com.b9.json.jsonplatform.wallet.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID walletId;
    private UUID targetWalletId;
    private UUID transactionId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        targetWalletId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        transaction = new Transaction(
                walletId,
                TransactionType.TOP_UP,
                new BigDecimal("100"),
                "Top Up"
        );
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
    }

    @Test
    void testCreateTransaction_Success() {
        when(transactionRepository.save(any())).thenReturn(transaction);

        Transaction result = transactionService.createTransaction(
                walletId,
                TransactionType.TOP_UP,
                new BigDecimal("100"),
                "Top Up"
        );

        assertNotNull(result);
        assertEquals(walletId, result.getWalletId());
        assertEquals(TransactionType.TOP_UP, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreateTransaction_InvalidAmount_Zero() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        walletId,
                        TransactionType.TOP_UP,
                        BigDecimal.ZERO,
                        "Top Up"
                ));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testCreateTransaction_InvalidAmount_Negative() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        walletId,
                        TransactionType.WITHDRAWAL,
                        new BigDecimal("-50"),
                        "Withdrawal"
                ));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testMarkSuccess_TopUp() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        Transaction result = transactionService.markSuccess(transactionId);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        verify(walletService, times(1)).increaseBalance(walletId, new BigDecimal("100"));
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testMarkSuccess_Withdrawal() {
        Transaction withdrawal = new Transaction(
                walletId,
                TransactionType.WITHDRAWAL,
                new BigDecimal("50"),
                "Withdrawal"
        );
        withdrawal.setId(transactionId);
        withdrawal.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(withdrawal));
        when(transactionRepository.save(any())).thenReturn(withdrawal);

        Transaction result = transactionService.markSuccess(transactionId);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        verify(walletService, times(1)).decreaseBalance(walletId, new BigDecimal("50"));
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testMarkSuccess_Payment() {
        Transaction payment = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.PAYMENT,
                new BigDecimal("75"),
                "Payment"
        );
        payment.setId(transactionId);
        payment.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(payment));
        when(transactionRepository.save(any())).thenReturn(payment);

        Transaction result = transactionService.markSuccess(transactionId);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        verify(walletService, times(1)).decreaseBalance(walletId, new BigDecimal("75"));
        verify(walletService, times(1)).increaseBalance(targetWalletId, new BigDecimal("75"));
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testMarkSuccess_Refund() {
        Transaction refund = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.REFUND,
                new BigDecimal("30"),
                "Refund"
        );
        refund.setId(transactionId);
        refund.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(refund));
        when(transactionRepository.save(any())).thenReturn(refund);

        Transaction result = transactionService.markSuccess(transactionId);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        verify(walletService, times(1)).increaseBalance(walletId, new BigDecimal("30"));
        verify(walletService, times(1)).decreaseBalance(targetWalletId, new BigDecimal("30"));
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testMarkSuccess_Payment_TargetWalletMissing() {
        Transaction payment = new Transaction(
                walletId,
                TransactionType.PAYMENT,
                new BigDecimal("25"),
                "Payment"
        );
        payment.setId(transactionId);
        payment.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> transactionService.markSuccess(transactionId));

        verify(walletService, never()).increaseBalance(any(), any());
        verify(walletService, never()).decreaseBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testMarkSuccess_TransactionNotFound() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.markSuccess(transactionId));

        verify(walletService, never()).increaseBalance(any(), any());
        verify(walletService, never()).decreaseBalance(any(), any());
    }

    @Test
    void testMarkSuccess_NotPending() {
        transaction.setStatus(TransactionStatus.SUCCESS);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThrows(IllegalStateException.class,
                () -> transactionService.markSuccess(transactionId));

        verify(walletService, never()).increaseBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testMarkFailed_Success() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        Transaction result = transactionService.markFailed(transactionId);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(transactionRepository, times(1)).save(any());
        verify(walletService, never()).increaseBalance(any(), any());
        verify(walletService, never()).decreaseBalance(any(), any());
    }

    @Test
    void testMarkFailed_TransactionNotFound() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.markFailed(transactionId));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testMarkFailed_NotPending() {
        transaction.setStatus(TransactionStatus.FAILED);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThrows(IllegalStateException.class,
                () -> transactionService.markFailed(transactionId));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testGetWalletTransactions() {
        Transaction transaction2 = new Transaction(
                walletId,
                TransactionType.WITHDRAWAL,
                new BigDecimal("50"),
                "Withdrawal"
        );

        List<Transaction> transactions = List.of(transaction, transaction2);

        when(transactionRepository.findByWalletId(walletId)).thenReturn(transactions);

        List<Transaction> result = transactionService.getWalletTransactions(walletId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TransactionType.TOP_UP, result.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, result.get(1).getType());

        verify(transactionRepository, times(1)).findByWalletId(walletId);
    }

    @Test
    void testGetWalletTransactions_Empty() {
        when(transactionRepository.findByWalletId(walletId)).thenReturn(List.of());

        List<Transaction> result = transactionService.getWalletTransactions(walletId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository, times(1)).findByWalletId(walletId);
    }

    @Test
    void testCreateTopUp() {
        when(transactionRepository.save(any())).thenReturn(transaction);

        Transaction result = transactionService.createTopUp(walletId, new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(TransactionType.TOP_UP, result.getType());
        assertEquals(new BigDecimal("100"), result.getAmount());

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreateWithdrawal() {
        Transaction withdrawal = new Transaction(
                walletId,
                TransactionType.WITHDRAWAL,
                new BigDecimal("50"),
                "Withdrawal"
        );

        when(transactionRepository.save(any())).thenReturn(withdrawal);

        Transaction result = transactionService.createWithdrawal(walletId, new BigDecimal("50"));

        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(new BigDecimal("50"), result.getAmount());

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreatePayment() {
        Transaction payment = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.PAYMENT,
                new BigDecimal("80"),
                "Payment"
        );

        when(transactionRepository.save(any())).thenReturn(payment);

        Transaction result = transactionService.createPayment(walletId, targetWalletId, new BigDecimal("80"));

        assertNotNull(result);
        assertEquals(TransactionType.PAYMENT, result.getType());
        assertEquals(new BigDecimal("80"), result.getAmount());
        assertEquals(targetWalletId, result.getTargetWalletId());

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreateRefund() {
        Transaction refund = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.REFUND,
                new BigDecimal("40"),
                "Refund"
        );

        when(transactionRepository.save(any())).thenReturn(refund);

        Transaction result = transactionService.createRefund(walletId, targetWalletId, new BigDecimal("40"));

        assertNotNull(result);
        assertEquals(TransactionType.REFUND, result.getType());
        assertEquals(new BigDecimal("40"), result.getAmount());
        assertEquals(targetWalletId, result.getTargetWalletId());

        verify(transactionRepository, times(1)).save(any());
    }
}
