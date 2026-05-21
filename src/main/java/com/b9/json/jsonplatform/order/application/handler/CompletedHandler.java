package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class CompletedHandler implements OrderStatusHandler {

    @Override
    public String supportedStatus() {
        return "COMPLETED";
    }

    @Override
    public Order handle(Order order, OrderStatusContext context) {
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Pesanan belum dikirim, tidak bisa diselesaikan");
        }
        order.setStatus("COMPLETED");
        return order;
    }
}