package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceDummy;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.application.TransactionServiceImpl;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionServiceImpl transactionService; 
    @Mock
    private InventoryServiceDummy inventoryService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrderWithZeroQuantityShouldThrowException() {
        Order order = new Order();
        order.setQuantity(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(order);
        });

        assertEquals("Jumlah barang harus lebih dari 0", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithValidQuantityShouldSuccess() {
        UUID titiperId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(1L);
        order.setQuantity(5);
        order.setTotalPrice(new BigDecimal("50000"));

        Wallet buyerWallet = mock(Wallet.class);
        when(buyerWallet.getId()).thenReturn(buyerWalletId);
        when(buyerWallet.getBalance()).thenReturn(new BigDecimal("100000")); // Saldo cukup

        Wallet sellerWallet = mock(Wallet.class);
        when(sellerWallet.getId()).thenReturn(sellerWalletId);

        Transaction dummyTx = mock(Transaction.class);
        when(dummyTx.getId()).thenReturn(transactionId);

        when(inventoryService.isStockAvailable(1L, 5)).thenReturn(true);
        when(walletService.getWalletByUserId(titiperId)).thenReturn(buyerWallet);
        when(walletService.getWalletByUserId(jastiperId)).thenReturn(sellerWallet);
        when(transactionService.createPayment(any(), any(), any())).thenReturn(dummyTx);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals("PAID", result.getStatus());
        verify(transactionService, times(1)).markSuccess(transactionId);
        verify(inventoryService, times(1)).reserveStock(1L, 5);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateStatusFromPaidToCompletedShouldThrowException() {
        Order order = new Order();
        order.setStatus("PAID");
        UUID dummyOrderId = UUID.randomUUID();

        when(orderRepository.findById(dummyOrderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.updateStatus(dummyOrderId, "COMPLETED");
        });

        assertEquals("Pesanan harus dikirim (SHIPPED) sebelum selesai", exception.getMessage());
    }

    @Test
    void testUpdateStatusToShippedShouldSuccess() {
        Order order = new Order();
        order.setStatus("PAID");
        UUID dummyOrderId = UUID.randomUUID();

        when(orderRepository.findById(dummyOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.updateStatus(dummyOrderId, "SHIPPED");

        assertEquals("SHIPPED", result.getStatus());
    }

    @Test
    void testGetTitiperHistory() {
        Order o1 = new Order();
        o1.setTitiperId(1L);
        List<Order> history = Arrays.asList(o1);

        when(orderRepository.findByTitiperId(1L)).thenReturn(history);

        List<Order> result = orderService.getTitiperHistory(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTitiperId());
    }
}