package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private UUID productId;
    private String owner;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        owner = "user1";
        sampleProduct = Product.builder()
                .id(productId)
                .name("produknya user 1")
                .description("beli aja")
                .price(new BigDecimal("5"))
                .stock(5)
                .ownerUsername(owner)
                .build();
    }

    @Test
    void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product created = productService.createProduct(sampleProduct, owner);

        assertNotNull(created);
        assertEquals(owner, created.getOwnerUsername());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        Product updatedInfo = Product.builder()
                .name("user2")
                .price(new BigDecimal("50"))
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.updateProduct(productId, updatedInfo, owner);

        assertEquals("user2", result.getName());
        assertEquals(new BigDecimal("50"), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Failure_NotOwner() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(productId, sampleProduct, "user2");
        });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId, owner));

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProduct_Failure_NotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(productId, owner);
        });

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteProduct_Failure_NotOwner() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(productId, "user2");
        });

        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void testGetAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> productList = productService.getAllProducts();
        assertEquals(sampleProduct, productList.getFirst());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetMyProducts_Success() {
        when(productRepository.findByOwner(owner)).thenReturn(List.of(sampleProduct));

        List<Product> productList = productService.getMyProducts(owner);
        assertEquals(sampleProduct, productList.getFirst());
        verify(productRepository, times(1)).findByOwner(owner);
    }
}