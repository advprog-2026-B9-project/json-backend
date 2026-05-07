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
        UUID dummyTitiperId = UUID.randomUUID(); // Buat UUID dummy

        Order order = new Order();
        order.setTitiperId(dummyTitiperId); // Gunakan UUID
        order.setQuantity(2);
        orderRepository.save(order);

        List<Order> found = orderRepository.findByTitiperId(dummyTitiperId); // Gunakan UUID untuk pencarian

        assertFalse(found.isEmpty());
        assertEquals(dummyTitiperId, found.get(0).getTitiperId()); // Pastikan hasil pengecekan menggunakan UUID
    }
}