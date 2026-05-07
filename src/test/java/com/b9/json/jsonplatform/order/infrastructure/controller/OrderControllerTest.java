package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.application.service.OrderService;
import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetHistoryReturnsOk() throws Exception {
        UUID dummyTitiperId = UUID.randomUUID();

        Order order = new Order();
        order.setTitiperId(dummyTitiperId);

        when(orderService.getTitiperHistory(dummyTitiperId)).thenReturn(Arrays.asList(order));

        mockMvc.perform(get("/api/orders/history/" + dummyTitiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titiperId").value(dummyTitiperId.toString()));
    }
}