package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class PurchasedHandler implements OrderStatusHandler {

    @Override
    public String supportedStatus() {
        return "PURCHASED";
    }

    @Override
    public Order handle(Order order, OrderStatusContext context) {
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalStateException("Hanya pesanan berstatus PAID yang bisa diproses");
        }
        order.setStatus("PURCHASED");
        return order;
    }
}