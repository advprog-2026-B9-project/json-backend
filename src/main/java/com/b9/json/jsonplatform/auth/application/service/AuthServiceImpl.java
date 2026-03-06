package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    private String resolveUsername(String requestedUsername, String email) {
        if (email == null) {
            return (requestedUsername != null && !requestedUsername.trim().isEmpty())
                    ? requestedUsername
                    : "user_tanpa_email";
        }

        if (requestedUsername == null || requestedUsername.trim().isEmpty()) {
            return email.split("@")[0];
        }
        return requestedUsername;
    }

    @Override
    public User registerUser(User user) {
        user.setUsername(resolveUsername(user.getUsername(), user.getEmail()));
        return userRepository.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User updateProfile(String email, User updatedUser) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            existingUser.setFullName(updatedUser.getFullName());

            existingUser.setUsername(resolveUsername(updatedUser.getUsername(), existingUser.getEmail()));
            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}