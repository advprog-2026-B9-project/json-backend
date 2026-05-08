package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.application.service.OrderService;

import org.springframework.http.ResponseEntity;
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

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        Order canceledOrder = orderService.cancelAndRefundOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    @GetMapping("/jastiper/{jastiperId}")
    public ResponseEntity<List<Order>> getJastiperOrders(@PathVariable UUID jastiperId) {
        List<Order> orders = orderService.getOrdersByJastiper(jastiperId);
        return ResponseEntity.ok(orders);
    }
}