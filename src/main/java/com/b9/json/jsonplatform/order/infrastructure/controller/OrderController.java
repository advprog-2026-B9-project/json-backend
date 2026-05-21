package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.application.handler.OrderStatusContext;
import com.b9.json.jsonplatform.order.application.service.OrderService;
import com.b9.json.jsonplatform.order.domain.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping("/history/{titiperId}")
    public ResponseEntity<List<Order>> history(@PathVariable UUID titiperId) {
        return ResponseEntity.ok(orderService.getTitiperHistory(titiperId));
    }

    @GetMapping("/jastiper/{jastiperId}")
    public ResponseEntity<List<Order>> getJastiperOrders(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.getOrdersByJastiper(jastiperId));
    }

    @GetMapping("/jastiper/{jastiperId}/stats")
    public ResponseEntity<Long> getJastiperStats(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.getTotalSuccessfulOrdersByJastiper(jastiperId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Order>> getAllOrdersAdmin() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    @PatchMapping("/{orderId}/purchased")
    public ResponseEntity<Order> markPurchased(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, "PURCHASED", OrderStatusContext.builder().build()));
    }

    @PatchMapping("/{orderId}/shipped")
    public ResponseEntity<Order> markShipped(@PathVariable UUID orderId,
                                             @RequestParam String trackingNumber) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, "SHIPPED",
                OrderStatusContext.builder().trackingNumber(trackingNumber).build()));
    }

    @PatchMapping("/{orderId}/completed")
    public ResponseEntity<Order> markCompleted(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, "COMPLETED", OrderStatusContext.builder().build()));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelAndRefundOrder(orderId));
    }

    @PostMapping("/{orderId}/rate")
    public ResponseEntity<Order> rateOrder(@PathVariable UUID orderId,
                                           @RequestParam Integer jastiperRating,
                                           @RequestParam Integer productRating) {
        return ResponseEntity.ok(orderService.giveRating(orderId, jastiperRating, productRating));
    }
}