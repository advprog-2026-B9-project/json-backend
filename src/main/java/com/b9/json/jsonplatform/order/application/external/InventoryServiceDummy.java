package com.b9.json.jsonplatform.order.application.external;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class InventoryServiceDummy {
    public boolean isStockAvailable(UUID productId, Integer quantity) {
        return true;
    }

    public void reserveStock(UUID productId, Integer quantity) {
        System.out.println("Dummy: Memotong stok untuk produk ID " + productId + " sebanyak " + quantity);
    }
}