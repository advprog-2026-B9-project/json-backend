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

    @GetMapping("/user")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        User user = authService.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }

    public static class KycRequest {
        private String email;
        private String fullName;
        private String nikKtp;
        private String ktpImageUrl;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getNikKtp() {
            return nikKtp;
        }

        public void setNikKtp(String nikKtp) {
            this.nikKtp = nikKtp;
        }

        public String getKtpImageUrl() {
            return ktpImageUrl;
        }

        public void setKtpImageUrl(String ktpImageUrl) {
            this.ktpImageUrl = ktpImageUrl;
        }
    }

    @PostMapping("/kyc/submit")
    public ResponseEntity<?> submitKyc(@RequestBody KycRequest request) {
        if (request.getNikKtp() == null || request.getNikKtp().isBlank()) {
            return ResponseEntity.badRequest().body("NIK KTP tidak boleh kosong");
        }

        User updatedUser = authService.submitKyc(
                request.getEmail(),
                request.getFullName(),
                request.getNikKtp(),
                request.getKtpImageUrl()
        );

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.badRequest().body("User dengan email tersebut tidak ditemukan");
    }

    public static class KycReviewRequest {
        private String email;
        private boolean approved;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }

    @GetMapping("/admin/kyc/pending")
    public ResponseEntity<List<User>> getPendingKyc() {
        return ResponseEntity.ok(authService.findPendingKyc());
    }

    @PostMapping("/admin/kyc/review")
    public ResponseEntity<?> reviewKyc(@RequestBody KycReviewRequest request) {
        User result = authService.reviewKyc(request.getEmail(), request.isApproved());

        if (result != null) {
            String message = request.isApproved() ?
                    "KYC Disetujui. Akun berhasil di-upgrade menjadi JASTIPER." :
                    "KYC Ditolak.";
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.badRequest().body("Gagal melakukan review. Pastikan email benar dan statusnya PENDING_VERIFICATION.");
    }
}