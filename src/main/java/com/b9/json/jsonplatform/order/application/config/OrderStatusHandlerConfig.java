package com.b9.json.jsonplatform.order.application.config;

import com.b9.json.jsonplatform.order.application.handler.OrderStatusHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class OrderStatusHandlerConfig {

    @Bean
    public Map<String, OrderStatusHandler> statusHandlers(List<OrderStatusHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(OrderStatusHandler::supportedStatus, Function.identity()));
    }
}