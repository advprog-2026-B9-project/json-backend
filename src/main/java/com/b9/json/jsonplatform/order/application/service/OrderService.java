package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Jumlah barang harus lebih dari 0");
        }
        return orderRepository.save(order);
    }

    public Order updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        if (order.getStatus().equals("PAID") && newStatus.equals("COMPLETED")) {
            throw new IllegalStateException("Pesanan harus dikirim (SHIPPED) sebelum selesai");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getTitiperHistory(Long titiperId) {
        return orderRepository.findByTitiperId(titiperId);
    }
}