package com.b9.json.jsonplatform.wallet.application.handler;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentHandlerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private PaymentHandler handler;

    private UUID walletId;
    private UUID targetWalletId;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        targetWalletId = UUID.randomUUID();
    }

    @Test
    void supportedType_returnsPayment() {
        assertEquals(TransactionType.PAYMENT, handler.supportedType());
    }

    @Test
    void execute_decreasesSourceAndIncreasesTarget() {
        Transaction transaction = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.PAYMENT,
                new BigDecimal("75"),
                "Payment"
        );

        handler.execute(transaction);

        verify(walletService, times(1)).decreaseBalance(walletId, new BigDecimal("75"));
        verify(walletService, times(1)).increaseBalance(targetWalletId, new BigDecimal("75"));
    }

    @Test
    void execute_withNullTargetWalletId_throwsIllegalStateException() {
        Transaction transaction = new Transaction(
                walletId,
                TransactionType.PAYMENT,
                new BigDecimal("25"),
                "Payment"
        );

        assertThrows(IllegalStateException.class, () -> handler.execute(transaction));

        verify(walletService, never()).decreaseBalance(any(), any());
        verify(walletService, never()).increaseBalance(any(), any());
    }
}
