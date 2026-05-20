package com.b9.json.jsonplatform.wallet.infrastructure;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    private UUID walletId;
    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
        wallet = new Wallet(userId);
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("250.00"));
    }

    @Test
    void testCreateWallet_returnsCreatedWallet() throws Exception {
        when(walletService.createWallet(any(UUID.class))).thenReturn(wallet);

        mockMvc.perform(post("/wallets/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.balance").value(250.00));

        verify(walletService).createWallet(userId);
    }

    @Test
    void testGetWalletById_found_returnsWallet() throws Exception {
        when(walletService.getWalletById(walletId)).thenReturn(wallet);

        mockMvc.perform(get("/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(250.00));
    }

    @Test
    void testGetWalletById_notFound_propagatesError() {
        when(walletService.getWalletById(walletId))
                .thenThrow(new IllegalArgumentException("Wallet not found"));

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/wallets/{walletId}", walletId)));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void testGetWalletByUserId_found_returnsWallet() throws Exception {
        when(walletService.getWalletByUserId(userId)).thenReturn(wallet);

        mockMvc.perform(get("/wallets/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void testGetWalletByUserId_notFound_propagatesError() {
        when(walletService.getWalletByUserId(userId))
                .thenThrow(new IllegalArgumentException("Wallet not found"));

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/wallets/users/{id}", userId)));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }
}
