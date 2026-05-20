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
class RefundHandlerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private RefundHandler handler;

    private UUID walletId;
    private UUID targetWalletId;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        targetWalletId = UUID.randomUUID();
    }

    @Test
    void supportedType_returnsRefund() {
        assertEquals(TransactionType.REFUND, handler.supportedType());
    }

    @Test
    void execute_increasesSourceAndDecreasesTarget() {
        Transaction transaction = new Transaction(
                walletId,
                targetWalletId,
                TransactionType.REFUND,
                new BigDecimal("30"),
                "Refund"
        );

        handler.execute(transaction);

        verify(walletService, times(1)).increaseBalance(walletId, new BigDecimal("30"));
        verify(walletService, times(1)).decreaseBalance(targetWalletId, new BigDecimal("30"));
    }

    @Test
    void execute_withNullTargetWalletId_throwsIllegalStateException() {
        Transaction transaction = new Transaction(
                walletId,
                TransactionType.REFUND,
                new BigDecimal("25"),
                "Refund"
        );

        assertThrows(IllegalStateException.class, () -> handler.execute(transaction));

        verify(walletService, never()).increaseBalance(any(), any());
        verify(walletService, never()).decreaseBalance(any(), any());
    }
}
