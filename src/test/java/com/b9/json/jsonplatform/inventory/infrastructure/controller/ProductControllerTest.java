package com.b9.json.jsonplatform.inventory.infrastructure.controller;

import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product sampleProduct;
    private UUID productId;
    private String ownerUsername;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        ownerUsername = "user1";

        sampleProduct = Product.builder()
                .id(productId)
                .name("produk 1")
                .description("beli bang")
                .price(new BigDecimal("50000"))
                .stock(3)
                .originCountry("Indo")
                .arrivalDate(LocalDate.now().plusDays(7))
                .ownerUsername(ownerUsername)
                .build();
    }

    @Test
    void createProduct() throws Exception {
        when(productService.createProduct(any(Product.class), eq(ownerUsername))).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", ownerUsername)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).createProduct(any(Product.class), eq(ownerUsername));
    }

    @Test
    void getAllProducts() throws Exception {
        ProductDetailResponse response = new ProductDetailResponse(sampleProduct, "User 1", "08123456789");
        List<ProductDetailResponse> responses = Collections.singletonList(response);

        when(productService.getAllProductsWithDetails(null, null)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value(sampleProduct.getName()))
                .andExpect(jsonPath("$[0].jastiperFullName").value("User 1"));

        verify(productService, times(1)).getAllProductsWithDetails(null, null);
    }

    @Test
    void getMyProducts() throws Exception {
        List<Product> products = Collections.singletonList(sampleProduct);
        when(productService.getMyProducts(ownerUsername)).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/me")
                        .header("X-User-Name", ownerUsername)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].ownerUsername").value(ownerUsername));

        verify(productService, times(1)).getMyProducts(ownerUsername);
    }

    @Test
    void updateProduct() throws Exception {
        when(productService.updateProduct(eq(productId), any(Product.class), eq(ownerUsername))).thenReturn(sampleProduct);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", ownerUsername)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).updateProduct(eq(productId), any(Product.class), eq(ownerUsername));
    }

    @Test
    void deleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(productId, ownerUsername);

        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                        .header("X-User-Name", ownerUsername))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId, ownerUsername);
    }
}