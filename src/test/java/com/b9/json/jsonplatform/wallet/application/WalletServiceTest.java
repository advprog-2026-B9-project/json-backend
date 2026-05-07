package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Wallet;
import com.b9.json.jsonplatform.wallet.domain.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet(UUID.randomUUID());
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);
    }

    @Test
    void testGetWalletById_Exist() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletById(walletId);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
    }

    @Test
    void testGetWalletById_NotExist() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.getWalletById(walletId));
    }

    @Test
    void testIncreaseBalance() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        walletService.increaseBalance(walletId, new BigDecimal("100"));

        assertEquals(new BigDecimal("100"), wallet.getBalance());
    }

    @Test
    void testDecreaseBalance_Success() {
        wallet.setBalance(new BigDecimal("200"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        walletService.decreaseBalance(walletId, new BigDecimal("50"));

        assertEquals(new BigDecimal("150"), wallet.getBalance());
    }

    @Test
    void testDecreaseBalance_Insufficient() {
        wallet.setBalance(new BigDecimal("50"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.decreaseBalance(walletId, new BigDecimal("100")));
    }
}