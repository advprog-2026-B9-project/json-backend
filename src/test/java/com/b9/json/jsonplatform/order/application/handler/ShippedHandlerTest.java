package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShippedHandlerTest {

    private final ShippedHandler handler = new ShippedHandler();

    @Test
    void testHandle_Success() {
        Order order = new Order();
        order.setStatus("PURCHASED");

        OrderStatusContext context = OrderStatusContext.builder()
                .trackingNumber("JNE-12345")
                .build();

        handler.handle(order, context);

        assertEquals("SHIPPED", order.getStatus());
        assertEquals("JNE-12345", order.getTrackingNumber());
    }

    @Test
    void testHandle_NotPurchased_ShouldThrow() {
        Order order = new Order();
        order.setStatus("PAID");

        assertThrows(IllegalStateException.class, () ->
                handler.handle(order, OrderStatusContext.builder().trackingNumber("JNE-12345").build())
        );
    }

    @Test
    void testHandle_MissingTrackingNumber_ShouldThrow() {
        Order order = new Order();
        order.setStatus("PURCHASED");

        assertThrows(IllegalArgumentException.class, () ->
                handler.handle(order, OrderStatusContext.builder().build())
        );
    }

    @Test
    void testSupportedStatus() {
        assertEquals("SHIPPED", handler.supportedStatus());
    }
}