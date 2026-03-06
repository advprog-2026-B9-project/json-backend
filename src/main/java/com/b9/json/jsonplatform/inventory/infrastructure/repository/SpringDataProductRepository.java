package com.b9.json.jsonplatform.inventory.infrastructure.repository;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByOwnerUsername(String ownerUsername);
}
