package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.application.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public Order checkout(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @GetMapping("/history/{titiperId}")
    public List<Order> history(@PathVariable UUID titiperId) {
        return orderService.getTitiperHistory(titiperId);
    }
}