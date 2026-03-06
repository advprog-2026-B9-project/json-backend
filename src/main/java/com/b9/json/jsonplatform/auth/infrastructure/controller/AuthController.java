package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User savedUser = authService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {
        User loggedInUser = authService.loginUser(loginData.getEmail(), loginData.getPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        }
        return ResponseEntity.badRequest().body("Email atau password salah!");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam String email, @RequestBody User updatedUser) {
        User savedUser = authService.updateProfile(email, updatedUser);
        if (savedUser != null) {
            return ResponseEntity.ok(savedUser);
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(authService.findAllUsers());
    }
}