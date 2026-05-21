package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompletedHandlerTest {

    private final CompletedHandler handler = new CompletedHandler();

    @Test
    void testHandle_Success() {
        Order order = new Order();
        order.setStatus("SHIPPED");

        handler.handle(order, OrderStatusContext.builder().build());

        assertEquals("COMPLETED", order.getStatus());
    }

    @Test
    void testHandle_NotShipped_ShouldThrow() {
        Order order = new Order();
        order.setStatus("PURCHASED");

        assertThrows(IllegalStateException.class, () ->
                handler.handle(order, OrderStatusContext.builder().build())
        );
    }

    @Test
    void testSupportedStatus() {
        assertEquals("COMPLETED", handler.supportedStatus());
    }
}