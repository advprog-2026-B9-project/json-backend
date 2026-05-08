package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.application.TransactionServiceImpl;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final TransactionServiceImpl transactionService;
    private final ProductService productService; 

    public OrderService(OrderRepository orderRepository,
                        WalletService walletService,
                        TransactionServiceImpl transactionService,
                        ProductService productService) { 
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.productService = productService;
    }

    @Transactional
    public Order createOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Jumlah barang harus lebih dari 0");
        }

        Product product = productService.getProductById(order.getProductId());

        Wallet buyerWallet = walletService.getWalletByUserId(order.getTitiperId());
        Wallet sellerWallet = walletService.getWalletByUserId(order.getJastiperId());

        if (buyerWallet.getBalance().compareTo(order.getTotalPrice()) < 0) {
            throw new IllegalStateException("Saldo Wallet tidak mencukupi");
        }

        Transaction payment = transactionService.createPayment(
                buyerWallet.getId(),
                sellerWallet.getId(),
                order.getTotalPrice()
        );
        transactionService.markSuccess(payment.getId());
        
        productService.deductProductStock(order.getProductId(), order.getQuantity());

        order.setStatus("PAID");
        return orderRepository.save(order);
    }

    public long getTotalSuccessfulOrdersByJastiper(UUID jastiperId) {
        return orderRepository.countByJastiperIdAndStatus(jastiperId, "COMPLETED");
    }

    public List<Order> getTitiperHistory(UUID titiperId) {
        return orderRepository.findByTitiperId(titiperId);
    }

    public Order updateStatusToPurchased(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
        
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalStateException("Hanya pesanan berstatus PAID yang bisa diproses");
        }
        
        order.setStatus("PURCHASED");
        return orderRepository.save(order);
    }

    public Order updateStatusToShipped(UUID orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
        
        if (!"PURCHASED".equals(order.getStatus())) {
            throw new IllegalStateException("Pesanan harus berstatus PURCHASED sebelum dikirim");
        }
        
        order.setStatus("SHIPPED");
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    public Order updateStatusToCompleted(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
        
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Pesanan belum dikirim, tidak bisa diselesaikan");
        }
        
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }
}