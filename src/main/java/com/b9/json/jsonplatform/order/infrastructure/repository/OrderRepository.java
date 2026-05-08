package com.b9.json.jsonplatform.order.infrastructure.repository;

import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByTitiperId(UUID titiperId);
    long countByJastiperIdAndStatus(UUID jastiperId, String status);
}