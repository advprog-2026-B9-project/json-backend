package com.b9.json.jsonplatform.inventory.infrastructure.repository;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository springDataRepository;

    @Override
    public Product save(Product product) {
        return springDataRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<Product> findByOwner(String ownerUsername) {
        return springDataRepository.findByOwnerUsername(ownerUsername);
    }

    @Override
    public List<Product> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<Product> searchProducts(String name, String jastiper) {
        return springDataRepository.searchProducts(name, jastiper);
    }

    @Override
    public boolean deductStock(UUID id, Integer quantity) {
        return springDataRepository.deductStockIfAvailable(id, quantity) > 0;
    }

    @Override
    public Optional<Product> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }
}