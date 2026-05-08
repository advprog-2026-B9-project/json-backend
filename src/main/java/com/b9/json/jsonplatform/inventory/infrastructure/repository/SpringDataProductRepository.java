package com.b9.json.jsonplatform.inventory.infrastructure.repository;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByOwnerUsername(String ownerUsername);

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:jastiper IS NULL OR p.ownerUsername = :jastiper)")
    List<Product> searchProducts(@Param("name") String name, @Param("jastiper") String jastiper);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int deductStockIfAvailable(@Param("id") UUID id, @Param("quantity") Integer quantity);
}