package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.application.dto.ProductDetailResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product createProduct(Product product, String ownerUsername);
    Product updateProduct(UUID id, Product product, String ownerUsername);
    List<Product> getAllProducts();
    List<Product> getMyProducts(String ownerUsername);
    void deleteProduct(UUID id, String ownerUsername);
    List<ProductDetailResponse> getAllProductsWithDetails(String name, String jastiper);
    Product getProductById(UUID id);
    void deductProductStock(UUID id, Integer quantity);
    void increaseProductStock(UUID id, Integer quantity);
    void adminDeleteProduct(UUID id);
    Product adminUpdateProduct(UUID id, Product updatedProduct);
    void addProductRating(UUID id, Integer ratingScore);
}