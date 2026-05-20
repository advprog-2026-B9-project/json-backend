package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.application.TransactionServiceImpl;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionServiceImpl transactionService; 
    @Mock
    private ProductService productService;
    @Mock
    private AuthService authService;

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
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setQuantity(5);
        order.setTotalPrice(new BigDecimal("50000"));

        Wallet buyerWallet = mock(Wallet.class);
        when(buyerWallet.getId()).thenReturn(buyerWalletId);
        when(buyerWallet.getBalance()).thenReturn(new BigDecimal("100000"));

        Wallet sellerWallet = mock(Wallet.class);
        when(sellerWallet.getId()).thenReturn(sellerWalletId);

        Transaction dummyTx = mock(Transaction.class);
        when(dummyTx.getId()).thenReturn(transactionId);

        Product dummyProduct = new Product();
        dummyProduct.setId(productId);
        dummyProduct.setStock(10); 
        
        when(productService.getProductById(productId)).thenReturn(dummyProduct);

        when(walletService.getWalletByUserId(titiperId)).thenReturn(buyerWallet);
        when(walletService.getWalletByUserId(jastiperId)).thenReturn(sellerWallet);
        when(transactionService.createPayment(any(), any(), any())).thenReturn(dummyTx);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals("PAID", result.getStatus());
        verify(transactionService, times(1)).markSuccess(transactionId);
        verify(productService, times(1)).deductProductStock(productId, 5); // Verifikasi deduct product
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testGetTitiperHistory() {
        Order o1 = new Order();
        UUID titiperId = UUID.randomUUID();
        o1.setTitiperId(titiperId);
        List<Order> history = Arrays.asList(o1);

        when(orderRepository.findByTitiperId(titiperId)).thenReturn(history);

        List<Order> result = orderService.getTitiperHistory(titiperId);

        assertEquals(1, result.size());
        assertEquals(titiperId, result.get(0).getTitiperId());
    }

    @Test
    void testUpdateStatusToPurchased_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PAID");
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order updatedOrder = orderService.updateStatusToPurchased(orderId);

        assertEquals("PURCHASED", updatedOrder.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateStatusToPurchased_Failed_BecauseNotPaid() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PENDING");
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.updateStatusToPurchased(orderId);
        });

        assertEquals("Hanya pesanan berstatus PAID yang bisa diproses", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class)); 
    }

    @Test
    void testUpdateStatusToShipped_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PURCHASED");
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order updatedOrder = orderService.updateStatusToShipped(orderId, "RESI-JNE-12345");

        assertEquals("SHIPPED", updatedOrder.getStatus());
        assertEquals("RESI-JNE-12345", updatedOrder.getTrackingNumber());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateStatusToShipped_Failed_BecauseNotPurchased() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PAID"); 
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.updateStatusToShipped(orderId, "RESI-JNE-12345");
        });

        assertEquals("Pesanan harus berstatus PURCHASED sebelum dikirim", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class)); 
    }

    @Test
    void testUpdateStatusToCompleted_Success() {
        UUID orderId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus("SHIPPED");
        order.setJastiperId(jastiperId);

        User jastiper = new User();
        jastiper.setEmail("jastiper@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authService.findById(jastiperId)).thenReturn(jastiper);

        Order updatedOrder = orderService.updateStatusToCompleted(orderId, 5);

        assertEquals("COMPLETED", updatedOrder.getStatus());
        assertEquals(5, updatedOrder.getRatingScore());
        verify(authService, times(1)).addRating("jastiper@example.com", 5);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateStatusToCompleted_NullRating_ShouldNotCallAddRating() {
        UUID orderId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus("SHIPPED");
        order.setJastiperId(jastiperId);

        User jastiper = new User();
        jastiper.setEmail("jastiper@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authService.findById(jastiperId)).thenReturn(jastiper);

        Order updatedOrder = orderService.updateStatusToCompleted(orderId, null);

        assertEquals("COMPLETED", updatedOrder.getStatus());
        verify(authService, never()).addRating(any(), anyInt());
    }

    @Test
    void testUpdateStatusToCompleted_JastiperNotFound_ShouldNotCallAddRating() {
        UUID orderId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus("SHIPPED");
        order.setJastiperId(jastiperId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authService.findById(jastiperId)).thenReturn(null);

        Order updatedOrder = orderService.updateStatusToCompleted(orderId, 4);

        assertEquals("COMPLETED", updatedOrder.getStatus());
        verify(authService, never()).addRating(any(), anyInt());
    }

    @Test
    void testUpdateStatusToCompleted_Failed_BecauseNotShipped() {
        Order order = new Order();
        order.setStatus("PAID");
        UUID dummyOrderId = UUID.randomUUID();

        when(orderRepository.findById(dummyOrderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.updateStatusToCompleted(dummyOrderId, null);
        });

        assertEquals("Pesanan belum dikirim, tidak bisa diselesaikan", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class)); 
    }

    @Test
    void testCancelAndRefundOrder_Success() {
        UUID orderId = UUID.randomUUID();
        UUID titiperId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("PAID");
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("200000"));

        Wallet buyerWallet = mock(Wallet.class);
        when(buyerWallet.getId()).thenReturn(UUID.randomUUID());

        Wallet sellerWallet = mock(Wallet.class);
        when(sellerWallet.getId()).thenReturn(UUID.randomUUID());

        Transaction dummyRefundTx = mock(Transaction.class);
        when(dummyRefundTx.getId()).thenReturn(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(walletService.getWalletByUserId(titiperId)).thenReturn(buyerWallet);
        when(walletService.getWalletByUserId(jastiperId)).thenReturn(sellerWallet);
        
        when(transactionService.createRefund(any(), any(), any())).thenReturn(dummyRefundTx);
        
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order canceledOrder = orderService.cancelAndRefundOrder(orderId);

        assertEquals("CANCELLED", canceledOrder.getStatus());
        verify(transactionService, times(1)).markSuccess(dummyRefundTx.getId());
        
        verify(productService, times(1)).increaseProductStock(productId, 2);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testCancelAndRefundOrder_Failed_BecauseAlreadyShipped() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("SHIPPED");
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.cancelAndRefundOrder(orderId);
        });

        assertEquals("Pesanan tidak dapat dibatalkan pada status ini", exception.getMessage());
        
        verify(transactionService, never()).createRefund(any(), any(), any());
        verify(productService, never()).increaseProductStock(any(), any());
        verify(orderRepository, never()).save(any(Order.class)); 
    }
}