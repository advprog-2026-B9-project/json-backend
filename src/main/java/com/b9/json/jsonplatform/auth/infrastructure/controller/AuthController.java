package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.service.KycService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
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

    @Autowired
    private KycService kycService;

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
            PublicProfileResponse response = new PublicProfileResponse();
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().name());
            response.setKycStatus(user.getKycStatus().name());
            response.setBanned(user.isBanned());

            // Placeholder transaksi sukses (Milestone 75%)
            if (UserRole.JASTIPER.equals(user.getRole())) {
                response.setTotalSuccessfulTransactions(0);
            } else {
                response.setTotalSuccessfulTransactions(0);
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }


    @PostMapping("/kyc/submit")
    public ResponseEntity<?> submitKyc(@RequestBody KycRequest request) {
        if (request.getNikKtp() == null || request.getNikKtp().isBlank()) {
            return ResponseEntity.badRequest().body("NIK KTP tidak boleh kosong");
        }

        User updatedUser = kycService.submitKyc(
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

    @GetMapping("/admin/kyc/pending")
    public ResponseEntity<List<User>> getPendingKyc() {
        return ResponseEntity.ok(kycService.findPendingKyc());
    }

    @PostMapping("/admin/kyc/review")
    public ResponseEntity<?> reviewKyc(@RequestBody KycReviewRequest request) {
        User result = kycService.reviewKyc(request.getEmail(), request.isApproved());

        if (result != null) {
            String message = request.isApproved() ?
                    "KYC Disetujui. Akun berhasil di-upgrade menjadi JASTIPER." :
                    "KYC Ditolak.";
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.badRequest().body("Gagal melakukan review. Pastikan statusnya PENDING_VERIFICATION.");
    }

    @PostMapping("/admin/demote")
    public ResponseEntity<?> demoteUser(@RequestParam String email) {
        User result = authService.demoteJastiper(email);
        if (result != null) {
            return ResponseEntity.ok("User berhasil di-demote menjadi TITIPERS.");
        }
        return ResponseEntity.badRequest().body("Gagal demote. Pastikan user adalah JASTIPER.");
    }

    @PostMapping("/admin/ban")
    public ResponseEntity<?> banUser(@RequestParam String email) {
        User result = authService.banUser(email);
        if (result != null) {
            return ResponseEntity.ok("User berhasil di-banned.");
        }
        return ResponseEntity.badRequest().body("Gagal melakukan banned.");
    }
}