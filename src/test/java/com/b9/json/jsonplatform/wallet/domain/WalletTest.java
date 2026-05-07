package com.b9.json.jsonplatform.wallet.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletTest {
    @Test
    void TestInitializationZeroBalance() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }
}