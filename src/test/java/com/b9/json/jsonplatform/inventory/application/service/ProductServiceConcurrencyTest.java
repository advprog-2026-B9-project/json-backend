package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ProductServiceConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setName("Test");
        product.setDescription("This is just a test");
        product.setPrice(new BigDecimal("67"));
        product.setStock(10);
        product.setOriginCountry("Indonesia");
        product.setArrivalDate(LocalDate.now().plusDays(7));
        product.setOwnerUsername("test");

        testProduct = productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(testProduct.getId());
    }

    @Test
    void deductProductStock_ConcurrencyTest_ShouldPreventOverselling() throws InterruptedException {
        int numberOfThreads = 50;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    try {
                        productService.deductProductStock(testProduct.getId(), 1);
                        successCount.incrementAndGet();
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        failedCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(10, successCount.get(), "Hanya 10 transaksi yang boleh berhasil");
        assertEquals(40, failedCount.get(), "40 transaksi lainnya harus ditolak");
        assertEquals(0, updatedProduct.getStock(), "Stok akhir harus 0, tidak boleh negatif");
    }
}