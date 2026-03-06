package com.b9.json.jsonplatform.order.infrastructure.repository;

import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTitiperId(Long titiperId);
}