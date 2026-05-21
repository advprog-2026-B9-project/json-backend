package com.b9.json.jsonplatform.wallet.infrastructure;

import com.b9.json.jsonplatform.wallet.application.TransactionService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionStatus;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

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
                walletId, TransactionType.TOP_UP, new BigDecimal("100"), "Top Up");
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
    }

    @Test
    void testCreateTransaction_returnsCreated() throws Exception {
        when(transactionService.createTransaction(any(), any(), any(), any()))
                .thenReturn(transaction);

        mockMvc.perform(post("/transactions")
                        .param("walletId", walletId.toString())
                        .param("type", "TOP_UP")
                        .param("amount", "100")
                        .param("description", "Top Up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TOP_UP"))
                .andExpect(jsonPath("$.amount").value(100));

        verify(transactionService).createTransaction(
                eq(walletId), eq(TransactionType.TOP_UP), eq(new BigDecimal("100")), eq("Top Up"));
    }

    @Test
    void testCreateTransaction_missingAmount_returns400() throws Exception {
        mockMvc.perform(post("/transactions")
                        .param("walletId", walletId.toString())
                        .param("type", "TOP_UP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTopUp_returnsTransaction() throws Exception {
        when(transactionService.createTopUp(eq(walletId), any())).thenReturn(transaction);

        mockMvc.perform(post("/transactions/topup")
                        .param("walletId", walletId.toString())
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TOP_UP"));
    }

    @Test
    void testCreateWithdrawal_returnsTransaction() throws Exception {
        Transaction withdrawal = new Transaction(
                walletId, TransactionType.WITHDRAWAL, new BigDecimal("50"), "Withdrawal");
        when(transactionService.createWithdrawal(eq(walletId), any())).thenReturn(withdrawal);

        mockMvc.perform(post("/transactions/withdrawal")
                        .param("walletId", walletId.toString())
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));
    }

    @Test
    void testCreatePayment_returnsTransaction() throws Exception {
        Transaction payment = new Transaction(
                walletId, targetWalletId, TransactionType.PAYMENT, new BigDecimal("75"), "Payment");
        when(transactionService.createPayment(eq(walletId), eq(targetWalletId), any()))
                .thenReturn(payment);

        mockMvc.perform(post("/transactions/payment")
                        .param("walletId", walletId.toString())
                        .param("targetWalletId", targetWalletId.toString())
                        .param("amount", "75"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.targetWalletId").value(targetWalletId.toString()));
    }

    @Test
    void testCreatePayment_missingTargetWalletId_returns400() throws Exception {
        mockMvc.perform(post("/transactions/payment")
                        .param("walletId", walletId.toString())
                        .param("amount", "75"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRefund_returnsTransaction() throws Exception {
        Transaction refund = new Transaction(
                walletId, targetWalletId, TransactionType.REFUND, new BigDecimal("30"), "Refund");
        when(transactionService.createRefund(eq(walletId), eq(targetWalletId), any()))
                .thenReturn(refund);

        mockMvc.perform(post("/transactions/refund")
                        .param("walletId", walletId.toString())
                        .param("targetWalletId", targetWalletId.toString())
                        .param("amount", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("REFUND"));
    }

    @Test
    void testMarkSuccess_returnsUpdatedTransaction() throws Exception {
        transaction.setStatus(TransactionStatus.SUCCESS);
        when(transactionService.markSuccess(transactionId)).thenReturn(transaction);

        mockMvc.perform(post("/transactions/{transactionId}/success", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testMarkSuccess_notFound_propagatesError() {
        when(transactionService.markSuccess(transactionId))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/transactions/{transactionId}/success", transactionId)));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void testMarkFailed_returnsUpdatedTransaction() throws Exception {
        transaction.setStatus(TransactionStatus.FAILED);
        when(transactionService.markFailed(transactionId)).thenReturn(transaction);

        mockMvc.perform(post("/transactions/{transactionId}/failed", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void testMarkFailed_notFound_propagatesError() {
        when(transactionService.markFailed(transactionId))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/transactions/{transactionId}/failed", transactionId)));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }

    @Test
    void testGetWalletTransactions_populated_returnsList() throws Exception {
        Transaction tx2 = new Transaction(
                walletId, TransactionType.WITHDRAWAL, new BigDecimal("20"), "Withdrawal");
        when(transactionService.getWalletTransactions(walletId))
                .thenReturn(List.of(transaction, tx2));

        mockMvc.perform(get("/transactions/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("TOP_UP"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
    }

    @Test
    void testGetWalletTransactions_empty_returnsEmptyArray() throws Exception {
        when(transactionService.getWalletTransactions(walletId)).thenReturn(List.of());

        mockMvc.perform(get("/transactions/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
