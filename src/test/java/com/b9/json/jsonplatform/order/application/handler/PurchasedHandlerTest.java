package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PurchasedHandlerTest {

    private final PurchasedHandler handler = new PurchasedHandler();

    @Test
    void testHandle_Success() {
        Order order = new Order();
        order.setStatus("PAID");

        handler.handle(order, OrderStatusContext.builder().build());

        assertEquals("PURCHASED", order.getStatus());
    }

    @Test
    void testHandle_NotPaid_ShouldThrow() {
        Order order = new Order();
        order.setStatus("PENDING");

        assertThrows(IllegalStateException.class, () ->
                handler.handle(order, OrderStatusContext.builder().build())
        );
    }

    @Test
    void testSupportedStatus() {
        assertEquals("PURCHASED", handler.supportedStatus());
    }
}