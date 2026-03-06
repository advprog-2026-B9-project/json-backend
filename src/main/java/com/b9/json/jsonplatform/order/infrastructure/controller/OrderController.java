package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.application.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public Order checkout(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @GetMapping("/history/{titiperId}")
    public List<Order> history(@PathVariable Long titiperId) {
        return orderService.getTitiperHistory(titiperId);
    }
}