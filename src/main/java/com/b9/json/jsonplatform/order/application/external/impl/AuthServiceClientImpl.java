package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthServiceClientImpl implements AuthServiceClient {

    private final AuthService authService;

    @Override
    public String findEmailById(UUID userId) {
        User user = authService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan dengan id: " + userId);
        }
        return user.getEmail();
    }

    @Override
    public void addRating(String email, int rating) {
        authService.addRating(email, rating);
    }

    @Override
    public UUID findUserIdByUsername(String username) {
        User user = authService.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan dengan username: " + username);
        }
        return user.getId();
    }
}