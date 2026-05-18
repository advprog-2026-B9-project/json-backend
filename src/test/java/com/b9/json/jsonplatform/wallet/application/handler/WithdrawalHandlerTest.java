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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WithdrawalHandlerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WithdrawalHandler handler;

    private UUID walletId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        transaction = new Transaction(
                walletId,
                TransactionType.WITHDRAWAL,
                new BigDecimal("50"),
                "Withdrawal"
        );
    }

    @Test
    void supportedType_returnsWithdrawal() {
        assertEquals(TransactionType.WITHDRAWAL, handler.supportedType());
    }

    @Test
    void execute_callsDecreaseBalanceOnSourceWallet() {
        handler.execute(transaction);

        verify(walletService, times(1)).decreaseBalance(walletId, new BigDecimal("50"));
    }
}
