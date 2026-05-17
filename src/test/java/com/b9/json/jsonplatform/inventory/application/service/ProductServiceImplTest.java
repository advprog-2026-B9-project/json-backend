package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.inventory.application.exception.InsufficientStockException;
import com.b9.json.jsonplatform.inventory.application.exception.InvalidStockQuantityException;
import com.b9.json.jsonplatform.inventory.application.exception.ProductNotFoundException;
import com.b9.json.jsonplatform.inventory.application.exception.ProductOwnershipException;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;
import com.b9.json.jsonplatform.inventory.application.dto.ProductDetailResponse;

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

    @Mock
    private AuthService authService;

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
                .stock(10)
                .build();

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.updateProduct(productId, updatedInfo, owner);

        assertEquals("user2", result.getName());
        assertEquals(new BigDecimal("50"), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Failure_NotOwner() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));

        assertThrows(ProductOwnershipException.class, () -> productService.updateProduct(productId, sampleProduct, "user2"));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Failure_NotFound() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, sampleProduct, owner));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId, owner));

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProduct_Failure_NotFound() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId, owner));

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteProduct_Failure_NotOwner() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));

        assertThrows(ProductOwnershipException.class, () -> productService.deleteProduct(productId, "user2"));

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

    @Test
    void testGetAllProductsWithDetails_Success() {
        User mockUser = new User();
        mockUser.setUsername(owner);
        mockUser.setFullName("User 1");
        mockUser.setPhoneNumber("08123456789");

        when(productRepository.searchProducts("produknya", owner)).thenReturn(List.of(sampleProduct));
        when(authService.findByUsername(owner)).thenReturn(mockUser);

        List<ProductDetailResponse> result = productService.getAllProductsWithDetails("produknya", owner);

        assertEquals(1, result.size());
        assertEquals("User 1", result.getFirst().getJastiperFullName());
        verify(productRepository, times(1)).searchProducts("produknya", owner);
        verify(authService, times(1)).findByUsername(owner);
    }

    @Test
    void testGetAllProductsWithDetails_UserNotFound_Success() {
        when(productRepository.searchProducts("produknya", owner)).thenReturn(List.of(sampleProduct));
        when(authService.findByUsername(owner)).thenReturn(null);

        List<ProductDetailResponse> result = productService.getAllProductsWithDetails("produknya", owner);

        assertEquals(1, result.size());
        assertEquals("Anonim", result.getFirst().getJastiperFullName());
        assertEquals("-", result.getFirst().getJastiperPhoneNumber());
        verify(productRepository, times(1)).searchProducts("produknya", owner);
        verify(authService, times(1)).findByUsername(owner);
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById_Failure_NotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testDeductProductStock_Success() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        assertDoesNotThrow(() -> productService.deductProductStock(productId, 2));

        assertEquals(3, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void testDeductProductStock_Failure_InvalidQuantity() {
        assertThrows(InvalidStockQuantityException.class, () -> productService.deductProductStock(productId, 0));
        assertThrows(InvalidStockQuantityException.class, () -> productService.deductProductStock(productId, -5));
        assertThrows(InvalidStockQuantityException.class, () -> productService.deductProductStock(productId, null));

        verify(productRepository, never()).findByIdForUpdate(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testDeductProductStock_Failure_NotFound() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deductProductStock(productId, 2));

        verify(productRepository, never()).save(any());
    }

    @Test
    void testDeductProductStock_Failure_InsufficientStock() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));

        assertThrows(InsufficientStockException.class, () -> productService.deductProductStock(productId, 10));

        verify(productRepository, never()).save(any());
    }

    @Test
    void testIncreaseProductStock_Success() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        assertDoesNotThrow(() -> productService.increaseProductStock(productId, 3));

        assertEquals(8, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void testIncreaseProductStock_Failure_InvalidQuantity() {
        assertThrows(InvalidStockQuantityException.class, () -> productService.increaseProductStock(productId, 0));
        assertThrows(InvalidStockQuantityException.class, () -> productService.increaseProductStock(productId, -2));
        assertThrows(InvalidStockQuantityException.class, () -> productService.increaseProductStock(productId, null));

        verify(productRepository, never()).findByIdForUpdate(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testIncreaseProductStock_Failure_NotFound() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.increaseProductStock(productId, 2));

        verify(productRepository, never()).save(any());
    }
}