package com.b9.json.jsonplatform.inventory.domain.repository;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    List<Product> findByOwner(String ownerUsername);
    void deleteById(UUID id);
    List<Product> searchProducts(String name, String jastiper);
    Optional<Product> findByIdForUpdate(UUID id);
}