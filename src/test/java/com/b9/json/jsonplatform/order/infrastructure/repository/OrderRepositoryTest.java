package com.b9.json.jsonplatform.order.infrastructure.repository;

import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testFindByTitiperId() {
        UUID titiperId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(titiperId);
        order.setQuantity(2);
        orderRepository.save(order);

        List<Order> result = orderRepository.findByTitiperId(titiperId);

        assertFalse(result.isEmpty());
        assertEquals(titiperId, result.get(0).getTitiperId());
    }

    @Test
    void testFindByJastiperId() {
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setJastiperId(jastiperId);
        order.setQuantity(1);
        orderRepository.save(order);

        List<Order> result = orderRepository.findByJastiperId(jastiperId);

        assertFalse(result.isEmpty());
        assertEquals(jastiperId, result.get(0).getJastiperId());
    }

    @Test
    void testCountByJastiperIdAndStatus() {
        UUID jastiperId = UUID.randomUUID();

        Order order = new Order();
        order.setJastiperId(jastiperId);
        order.setStatus("COMPLETED");
        order.setQuantity(1);
        orderRepository.save(order);

        long count = orderRepository.countByJastiperIdAndStatus(jastiperId, "COMPLETED");

        assertEquals(1, count);
    }
}