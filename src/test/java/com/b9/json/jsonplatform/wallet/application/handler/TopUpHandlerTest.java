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
class TopUpHandlerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TopUpHandler handler;

    private UUID walletId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        transaction = new Transaction(
                walletId,
                TransactionType.TOP_UP,
                new BigDecimal("100"),
                "Top Up"
        );
    }

    @Test
    void supportedType_returnsTopUp() {
        assertEquals(TransactionType.TOP_UP, handler.supportedType());
    }

    @Test
    void execute_callsIncreaseBalanceOnSourceWallet() {
        handler.execute(transaction);

        verify(walletService, times(1)).increaseBalance(walletId, new BigDecimal("100"));
    }
}
