package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceClientImplTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryServiceClientImpl inventoryServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProductName_Success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setName("Sepatu Nike");

        when(productService.getProductById(productId)).thenReturn(product);

        assertEquals("Sepatu Nike", inventoryServiceClient.getProductName(productId));
    }

    @Test
    void testGetProductOwnerUsername_Success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setOwnerUsername("jastiper_budi");

        when(productService.getProductById(productId)).thenReturn(product);

        assertEquals("jastiper_budi", inventoryServiceClient.getProductOwnerUsername(productId));
    }

    @Test
    void testDeductStock_ShouldDelegate() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.deductStock(productId, 3);

        verify(productService).deductProductStock(productId, 3);
    }

    @Test
    void testIncreaseStock_ShouldDelegate() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.increaseStock(productId, 2);

        verify(productService).increaseProductStock(productId, 2);
    }

    @Test
    void testAddProductRating_ShouldDelegate() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.addProductRating(productId, 4);

        verify(productService).addProductRating(productId, 4);
    }
}