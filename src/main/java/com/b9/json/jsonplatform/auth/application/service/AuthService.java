package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.User;
import java.util.List;
import java.util.UUID;

public interface AuthService {
    User registerUser(User user);
    User loginUser(String email, String password);
    User updateProfile(String email, User updatedUser);
    User findByEmail(String email);
    User findByUsername(String username);
    User findById(UUID id);
    List<User> findAllUsers(String status);
    User demoteJastiper(String email);
    User banUser(String email);
    long countSuccessfulTransactions(String email);
    User addRating(String email, int ratingScore);
}