package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.application.service.OrderService;
import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetHistoryReturnsOk() throws Exception {
        UUID titiperId = UUID.randomUUID();
        Order order = new Order();
        order.setTitiperId(titiperId);

        when(orderService.getTitiperHistory(titiperId)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/history/" + titiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titiperId").value(titiperId.toString()));
    }

    @Test
    void testGetAllOrdersAdminReturnsOk() throws Exception {
        Order order = new Order();
        order.setStatus("PAID");

        when(orderService.getAllOrdersForAdmin()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    void testGetJastiperOrdersReturnsOk() throws Exception {
        UUID jastiperId = UUID.randomUUID();
        Order order = new Order();
        order.setJastiperId(jastiperId);

        when(orderService.getOrdersByJastiper(jastiperId)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/jastiper/" + jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jastiperId").value(jastiperId.toString()));
    }
}