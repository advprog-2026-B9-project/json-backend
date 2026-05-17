package com.b9.json.jsonplatform.inventory.application.exception;

public class InvalidStockQuantityException extends RuntimeException {
    public InvalidStockQuantityException(String message) {
        super(message);
    }
}