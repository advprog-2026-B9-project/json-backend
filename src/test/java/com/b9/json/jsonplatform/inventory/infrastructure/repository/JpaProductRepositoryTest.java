package com.b9.json.jsonplatform.inventory.infrastructure.repository;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProductRepositoryTest {

    @Mock
    private SpringDataProductRepository springDataRepository;

    @InjectMocks
    private JpaProductRepository jpaProductRepository;

    private Product sampleProduct;
    private UUID productId;
    private String ownerUsername;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        ownerUsername = "user1";

        sampleProduct = Product.builder()
                .id(productId)
                .name("produk1")
                .description("beli bang")
                .price(new BigDecimal("50000"))
                .stock(3)
                .originCountry("Sawit")
                .arrivalDate(LocalDate.now().plusDays(7))
                .ownerUsername(ownerUsername)
                .build();
    }

    @Test
    void testSave() {
        when(springDataRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = jpaProductRepository.save(sampleProduct);

        assertNotNull(result);
        assertEquals(sampleProduct.getName(), result.getName());
        verify(springDataRepository, times(1)).save(sampleProduct);
    }

    @Test
    void testFindById() {
        when(springDataRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        Optional<Product> result = jpaProductRepository.findById(productId);

        assertTrue(result.isPresent());
        assertEquals(productId, result.get().getId());
        verify(springDataRepository, times(1)).findById(productId);
    }

    @Test
    void testFindByOwner() {
        List<Product> productList = List.of(sampleProduct);
        when(springDataRepository.findByOwnerUsername(ownerUsername)).thenReturn(productList);

        List<Product> result = jpaProductRepository.findByOwner(ownerUsername);

        assertEquals(1, result.size());
        assertEquals(ownerUsername, result.getFirst().getOwnerUsername());
        verify(springDataRepository, times(1)).findByOwnerUsername(ownerUsername);
    }

    @Test
    void testFindAll() {
        List<Product> productList = List.of(sampleProduct);
        when(springDataRepository.findAll()).thenReturn(productList);

        List<Product> result = jpaProductRepository.findAll();

        assertEquals(1, result.size());
        verify(springDataRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        doNothing().when(springDataRepository).deleteById(productId);

        assertDoesNotThrow(() -> jpaProductRepository.deleteById(productId));

        verify(springDataRepository, times(1)).deleteById(productId);
    }
}