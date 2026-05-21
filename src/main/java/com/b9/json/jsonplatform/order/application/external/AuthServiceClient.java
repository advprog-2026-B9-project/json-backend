package com.b9.json.jsonplatform.order.application.external;

import java.util.UUID;

public interface AuthServiceClient {
    String findEmailById(UUID userId);
    void addRating(String email, int rating);
    UUID findUserIdByUsername(String username);
}