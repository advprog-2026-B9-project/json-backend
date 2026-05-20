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

    @PatchMapping("/{orderId}/purchased")
    public ResponseEntity<Order> markPurchased(@PathVariable UUID orderId) {
        Order order = orderService.updateStatusToPurchased(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/shipped")
    public ResponseEntity<Order> markShipped(@PathVariable UUID orderId,
                                             @RequestParam String trackingNumber) {
        Order order = orderService.updateStatusToShipped(orderId, trackingNumber);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/completed")
    public ResponseEntity<Order> markCompleted(@PathVariable UUID orderId) {
        Order order = orderService.updateStatusToCompleted(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/jastiper/{jastiperId}/stats")
    public ResponseEntity<Long> getJastiperStats(@PathVariable UUID jastiperId) {
        long total = orderService.getTotalSuccessfulOrdersByJastiper(jastiperId);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/{orderId}/rate")
    public ResponseEntity<?> rateOrder(@PathVariable UUID orderId, 
                                       @RequestParam Integer jastiperRating, 
                                       @RequestParam Integer productRating) {
        try {
            Order updatedOrder = orderService.giveRating(orderId, jastiperRating, productRating);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Order>> getAllOrdersAdmin() {
        List<Order> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(orders);
    }
}