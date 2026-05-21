package com.b9.json.jsonplatform.order.application.external;

import java.util.UUID;

public interface InventoryServiceClient {
    String getProductName(UUID productId);
    String getProductOwnerUsername(UUID productId);
    void deductStock(UUID productId, int quantity);
    void increaseStock(UUID productId, int quantity);
    void addProductRating(UUID productId, int rating);
}