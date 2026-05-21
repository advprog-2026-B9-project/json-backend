package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryServiceClientImpl implements InventoryServiceClient {

    private final ProductService productService;

    @Override
    public String getProductName(UUID productId) {
        Product product = productService.getProductById(productId);
        return product.getName();
    }

    @Override
    public String getProductOwnerUsername(UUID productId) {
        Product product = productService.getProductById(productId);
        return product.getOwnerUsername();
    }

    @Override
    public void deductStock(UUID productId, int quantity) {
        productService.deductProductStock(productId, quantity);
    }

    @Override
    public void increaseStock(UUID productId, int quantity) {
        productService.increaseProductStock(productId, quantity);
    }

    @Override
    public void addProductRating(UUID productId, int rating) {
        productService.addProductRating(productId, rating);
    }
}