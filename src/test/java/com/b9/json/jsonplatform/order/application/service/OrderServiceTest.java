package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import com.b9.json.jsonplatform.order.application.handler.CompletedHandler;
import com.b9.json.jsonplatform.order.application.handler.OrderStatusHandler;
import com.b9.json.jsonplatform.order.application.handler.PurchasedHandler;
import com.b9.json.jsonplatform.order.application.handler.ShippedHandler;
import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WalletServiceClient walletServiceClient;
    @Mock
    private InventoryServiceClient inventoryServiceClient;
    @Mock
    private AuthServiceClient authServiceClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<OrderStatusHandler> handlers = List.of(
                new PurchasedHandler(),
                new ShippedHandler(),
                new CompletedHandler()
        );
        Map<String, OrderStatusHandler> handlerMap = new java.util.HashMap<>();
        handlers.forEach(h -> handlerMap.put(h.supportedStatus(), h));

        orderService = new OrderService(
                orderRepository,
                walletServiceClient,
                inventoryServiceClient,
                authServiceClient,
                handlerMap
        );
    }

    @Test
    void testCreateOrderWithZeroQuantityShouldThrowException() {
        Order order = new Order();
        order.setQuantity(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(order)
        );

        assertEquals("Jumlah barang harus lebih dari 0", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithValidQuantityShouldSuccess() {
        UUID titiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        String ownerUsername = "jastiper_budi";
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(titiperId);
        order.setProductId(productId);
        order.setQuantity(5);
        order.setTotalPrice(new BigDecimal("50000"));

        when(inventoryServiceClient.getProductOwnerUsername(productId)).thenReturn(ownerUsername);
        when(inventoryServiceClient.getProductName(productId)).thenReturn("Sepatu Nike");
        when(authServiceClient.findUserIdByUsername(ownerUsername)).thenReturn(jastiperId);
        when(walletServiceClient.getBalance(titiperId)).thenReturn(new BigDecimal("100000"));
        when(walletServiceClient.getWalletId(titiperId)).thenReturn(buyerWalletId);
        when(walletServiceClient.getWalletId(jastiperId)).thenReturn(sellerWalletId);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals("PAID", result.getStatus());
        assertEquals("Sepatu Nike", result.getProductName());
        assertEquals(jastiperId, result.getJastiperId());
        verify(walletServiceClient).createPayment(buyerWalletId, sellerWalletId, new BigDecimal("50000"));
        verify(inventoryServiceClient).deductStock(productId, 5);
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrderInsufficientBalanceShouldThrowException() {
        UUID titiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(titiperId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("500000"));

        when(inventoryServiceClient.getProductOwnerUsername(productId)).thenReturn("jastiper_budi");
        when(inventoryServiceClient.getProductName(productId)).thenReturn("Tas Gucci");
        when(authServiceClient.findUserIdByUsername("jastiper_budi")).thenReturn(UUID.randomUUID());
        when(walletServiceClient.getBalance(titiperId)).thenReturn(new BigDecimal("10000"));

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testGetTitiperHistory() {
        UUID titiperId = UUID.randomUUID();
        Order order = new Order();
        order.setTitiperId(titiperId);

        when(orderRepository.findByTitiperId(titiperId)).thenReturn(List.of(order));

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
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.updateStatus(orderId, "PURCHASED",
                com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder().build());

        assertEquals("PURCHASED", result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateStatusToPurchased_Failed_BecauseNotPaid() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PENDING");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "PURCHASED",
                        com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder().build())
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusToShipped_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PURCHASED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        var context = com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder()
                .trackingNumber("RESI-JNE-12345")
                .build();

        Order result = orderService.updateStatus(orderId, "SHIPPED", context);

        assertEquals("SHIPPED", result.getStatus());
        assertEquals("RESI-JNE-12345", result.getTrackingNumber());
        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateStatusToShipped_Failed_BecauseNotPurchased() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PAID");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var context = com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder()
                .trackingNumber("RESI-JNE-12345")
                .build();

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "SHIPPED", context)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusToCompleted_Success() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("SHIPPED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.updateStatus(orderId, "COMPLETED",
                com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder().build());

        assertEquals("COMPLETED", result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateStatusToCompleted_Failed_BecauseNotShipped() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PAID");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "COMPLETED",
                        com.b9.json.jsonplatform.order.application.handler.OrderStatusContext.builder().build())
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCancelAndRefundOrder_Success() {
        UUID orderId = UUID.randomUUID();
        UUID titiperId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("PAID");
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("200000"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(walletServiceClient.getWalletId(titiperId)).thenReturn(buyerWalletId);
        when(walletServiceClient.getWalletId(jastiperId)).thenReturn(sellerWalletId);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.cancelAndRefundOrder(orderId);

        assertEquals("CANCELLED", result.getStatus());
        verify(walletServiceClient).createRefund(buyerWalletId, sellerWalletId, new BigDecimal("200000"));
        verify(inventoryServiceClient).increaseStock(productId, 2);
        verify(orderRepository).save(order);
    }

    @Test
    void testCancelAndRefundOrder_Failed_BecauseAlreadyShipped() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("SHIPPED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.cancelAndRefundOrder(orderId)
        );

        verify(walletServiceClient, never()).createRefund(any(), any(), any());
        verify(inventoryServiceClient, never()).increaseStock(any(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testGiveRating_Success() {
        UUID orderId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String jastiperEmail = "jastiper.bagoes@gmail.com";

        Order order = new Order();
        order.setId(orderId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setStatus("COMPLETED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authServiceClient.findEmailById(jastiperId)).thenReturn(jastiperEmail);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.giveRating(orderId, 5, 4);

        assertEquals(5, result.getJastiperRating());
        assertEquals(4, result.getProductRating());
        verify(inventoryServiceClient).addProductRating(productId, 4);
        verify(authServiceClient).addRating(jastiperEmail, 5);
    }

    @Test
    void testGiveRating_OrderNotCompleted_ShouldThrowException() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("SHIPPED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.giveRating(orderId, 5, 4)
        );

        verify(authServiceClient, never()).addRating(anyString(), anyInt());
        verify(inventoryServiceClient, never()).addProductRating(any(), anyInt());
    }

    @Test
    void testGiveRating_InvalidRating_ShouldThrowException() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("COMPLETED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.giveRating(orderId, 6, 4)
        );
    }
}