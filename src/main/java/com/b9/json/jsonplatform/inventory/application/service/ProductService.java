package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.inventory.domain.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product createProduct(Product product, String ownerUsername);
    Product updateProduct(UUID id, Product product, String ownerUsername);
    List<Product> getAllProducts();
    List<Product> getMyProducts(String ownerUsername);
    void deleteProduct(UUID id, String ownerUsername);
}