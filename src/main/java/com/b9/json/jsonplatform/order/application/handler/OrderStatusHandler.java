package com.b9.json.jsonplatform.order.application.handler;

import com.b9.json.jsonplatform.order.domain.Order;

public interface OrderStatusHandler {
    String supportedStatus();
    Order handle(Order order, OrderStatusContext context);
}