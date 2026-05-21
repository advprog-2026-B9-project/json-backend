package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import com.b9.json.jsonplatform.order.application.handler.OrderStatusContext;
import com.b9.json.jsonplatform.order.application.handler.OrderStatusHandler;
import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletServiceClient walletServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final AuthServiceClient authServiceClient;
    private final Map<String, OrderStatusHandler> statusHandlers;

    @Transactional
    public Order createOrder(Order order) {
        validateOrder(order);

        String ownerUsername = inventoryServiceClient.getProductOwnerUsername(order.getProductId());
        String productName   = inventoryServiceClient.getProductName(order.getProductId());
        UUID jastiperId      = authServiceClient.findUserIdByUsername(ownerUsername);

        order.setProductName(productName);
        order.setJastiperId(jastiperId);
        order.setJastiperUsername(ownerUsername);

        if (walletServiceClient.getBalance(order.getTitiperId()).compareTo(order.getTotalPrice()) < 0) {
            throw new IllegalStateException("Saldo Wallet tidak mencukupi");
        }

        UUID buyerWalletId  = walletServiceClient.getWalletId(order.getTitiperId());
        UUID sellerWalletId = walletServiceClient.getWalletId(order.getJastiperId());

        walletServiceClient.createPayment(buyerWalletId, sellerWalletId, order.getTotalPrice());
        inventoryServiceClient.deductStock(order.getProductId(), order.getQuantity());

        order.setStatus("PAID");
        return orderRepository.save(order);
    }

    public Order updateStatus(UUID orderId, String targetStatus, OrderStatusContext context) {
        Order order = findOrderById(orderId);
        OrderStatusHandler handler = statusHandlers.get(targetStatus);

        if (handler == null) {
            throw new IllegalArgumentException("Status tidak valid: " + targetStatus);
        }

        handler.handle(order, context);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelAndRefundOrder(UUID orderId) {
        Order order = findOrderById(orderId);

        String currentStatus = order.getStatus();
        if ("SHIPPED".equals(currentStatus) || "COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Pesanan tidak dapat dibatalkan pada status ini");
        }

        UUID buyerWalletId  = walletServiceClient.getWalletId(order.getTitiperId());
        UUID sellerWalletId = walletServiceClient.getWalletId(order.getJastiperId());

        walletServiceClient.createRefund(buyerWalletId, sellerWalletId, order.getTotalPrice());
        inventoryServiceClient.increaseStock(order.getProductId(), order.getQuantity());

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order giveRating(UUID orderId, Integer jastiperRating, Integer productRating) {
        Order order = findOrderById(orderId);

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new IllegalStateException("Hanya pesanan berstatus COMPLETED yang bisa diberi rating");
        }

        validateRating(jastiperRating, "Jastiper");
        validateRating(productRating, "Produk");

        order.setJastiperRating(jastiperRating);
        order.setProductRating(productRating);

        inventoryServiceClient.addProductRating(order.getProductId(), productRating);

        String jastiperEmail = authServiceClient.findEmailById(order.getJastiperId());
        authServiceClient.addRating(jastiperEmail, jastiperRating);

        return orderRepository.save(order);
    }

    public List<Order> getTitiperHistory(UUID titiperId) {
        return orderRepository.findByTitiperId(titiperId);
    }

    public List<Order> getOrdersByJastiper(UUID jastiperId) {
        return orderRepository.findByJastiperId(jastiperId);
    }

    public long getTotalSuccessfulOrdersByJastiper(UUID jastiperId) {
        return orderRepository.countByJastiperIdAndStatus(jastiperId, "COMPLETED");
    }

    public List<Order> getAllOrdersForAdmin() {
        return orderRepository.findAll();
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
    }

    private void validateOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Jumlah barang harus lebih dari 0");
        }
        if (order.getShippingAddress() != null && order.getShippingAddress().matches(".*[<>{}\\$].*")) {
            throw new IllegalArgumentException("Alamat pengiriman mengandung karakter tidak valid");
        }
    }

    private void validateRating(Integer rating, String label) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating " + label + " harus di antara 1 dan 5");
        }
    }
}