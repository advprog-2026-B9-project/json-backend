package com.b9.json.jsonplatform.wallet.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WalletTest {

    @Test
    void TestInitializationZeroBalance() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void testConstructor_assignsUserId() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(userId);
        assertEquals(userId, wallet.getUserId());
    }

    @Test
    void testSetBalance_acceptsZero() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        wallet.setBalance(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void testSetBalance_acceptsVeryLargeValue() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        BigDecimal large = new BigDecimal("99999999999.99");
        wallet.setBalance(large);
        assertEquals(large, wallet.getBalance());
    }

    @Test
    void testSetBalance_preservesScale() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        BigDecimal scaled = new BigDecimal("100.00");
        wallet.setBalance(scaled);
        assertEquals(2, wallet.getBalance().scale());
    }

    @Test
    void testSetId_andGetId_roundTrip() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        UUID id = UUID.randomUUID();
        wallet.setId(id);
        assertEquals(id, wallet.getId());
    }

    @Test
    void testSetUserId_overridesConstructorUserId() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        UUID newUserId = UUID.randomUUID();
        wallet.setUserId(newUserId);
        assertEquals(newUserId, wallet.getUserId());
    }

    @Test
    void testConstructor_balanceIsNotNull() {
        Wallet wallet = new Wallet(UUID.randomUUID());
        assertNotNull(wallet.getBalance());
    }
}
