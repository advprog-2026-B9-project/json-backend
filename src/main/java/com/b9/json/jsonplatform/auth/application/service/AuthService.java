package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.User;
import java.util.List;

public interface AuthService {
    User registerUser(User user);
    User loginUser(String email, String password);
    User updateProfile(String email, User updatedUser);
    User findByEmail(String email);
    List<User> findAllUsers();
    User submitKyc(String email, String fullName, String nikKtp, String ktpImageUrl);
    List<User> findPendingKyc();
    User reviewKyc(String email, boolean approved);
    User demoteJastiper(String email);
    User banUser(String email);
}