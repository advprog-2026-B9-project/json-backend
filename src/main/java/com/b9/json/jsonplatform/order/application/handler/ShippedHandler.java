package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class ShippedHandler implements OrderStatusHandler {

    @Override
    public String supportedStatus() {
        return "SHIPPED";
    }

    @Override
    public Order handle(Order order, OrderStatusContext context) {
        if (!"PURCHASED".equals(order.getStatus())) {
            throw new IllegalStateException("Pesanan harus berstatus PURCHASED sebelum dikirim");
        }
        if (context.getTrackingNumber() == null || context.getTrackingNumber().isBlank()) {
            throw new IllegalArgumentException("Nomor resi wajib diisi");
        }
        order.setStatus("SHIPPED");
        order.setTrackingNumber(context.getTrackingNumber());
        return order;
    }
}