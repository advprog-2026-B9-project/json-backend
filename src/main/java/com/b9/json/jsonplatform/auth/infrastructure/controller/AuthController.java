    package com.b9.json.jsonplatform.auth.infrastructure.controller;
    
    import com.b9.json.jsonplatform.auth.application.dto.UserInternalResponse;
    import com.b9.json.jsonplatform.auth.application.service.AuthService;
    import com.b9.json.jsonplatform.auth.application.service.KycService;
    import com.b9.json.jsonplatform.auth.domain.User;
    import com.b9.json.jsonplatform.auth.domain.UserRole;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    
    @RestController
    @RequestMapping("/auth")
    public class AuthController {
    
        @Autowired
        private AuthService authService;
    
        @Autowired
        private KycService kycService;
    
        @PostMapping("/register")
        public ResponseEntity<?> registerUser(@RequestBody User user) {
            try {
                User savedUser = authService.registerUser(user);
                return ResponseEntity.ok(savedUser);
            }
            catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
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
        public ResponseEntity<?> listUsers(@RequestParam(required = false) String status) {
            try {
                return ResponseEntity.ok(authService.findAllUsers(status));
            }
            catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @GetMapping("/user")
        public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
            User user = authService.findByEmail(email);
            if (user != null) {
                PublicProfileResponse response = new PublicProfileResponse();
                response.setUsername(user.getUsername());
                response.setFullName(user.getFullName());
                response.setEmail(user.getEmail());
                response.setRole(user.getRole().name());
                response.setKycStatus(user.getKycStatus().name());
                response.setBanned(user.isBanned());
                response.setRating(user.getRating());
                response.setTotalReviews(user.getTotalReviews());

                if (UserRole.JASTIPER.equals(user.getRole())) {
                    response.setTotalSuccessfulTransactions(
                            authService.countSuccessfulTransactions(email)
                    );
                }
                else {
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
        public ResponseEntity<?> getPendingKyc(@RequestParam String requesterEmail) {
            User requester = authService.findByEmail(requesterEmail);
            if (requester == null || !UserRole.ADMIN.equals(requester.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak. Hanya Admin yang dapat mengakses fitur ini.");
            }
            return ResponseEntity.ok(kycService.findPendingKyc());
        }
    
        @PostMapping("/admin/kyc/review")
        public ResponseEntity<?> reviewKyc(
                @RequestParam String requesterEmail,
                @RequestBody KycReviewRequest request) {
            User requester = authService.findByEmail(requesterEmail);
            if (requester == null || !UserRole.ADMIN.equals(requester.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak. Hanya Admin yang dapat mengakses fitur ini.");
            }
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
        public ResponseEntity<?> demoteUser(
                @RequestParam String requesterEmail,
                @RequestParam String email) {
            User requester = authService.findByEmail(requesterEmail);
            if (requester == null || !UserRole.ADMIN.equals(requester.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak. Hanya Admin yang dapat mengakses fitur ini.");
            }
            User result = authService.demoteJastiper(email);
            if (result != null) {
                return ResponseEntity.ok("User berhasil di-demote menjadi TITIPERS.");
            }
            return ResponseEntity.badRequest().body("Gagal demote. Pastikan user adalah JASTIPER.");
        }
    
        @PostMapping("/admin/ban")
        public ResponseEntity<?> banUser(
                @RequestParam String requesterEmail,
                @RequestParam String email) {
            User requester = authService.findByEmail(requesterEmail);
            if (requester == null || !UserRole.ADMIN.equals(requester.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak. Hanya Admin yang dapat mengakses fitur ini.");
            }
            User result = authService.banUser(email);
            if (result != null) {
                return ResponseEntity.ok("User berhasil di-banned.");
            }
            return ResponseEntity.badRequest().body("Gagal melakukan banned.");
        }
    
        @PostMapping("/rating")
        public ResponseEntity<?> addRating(
                @RequestParam String jastiperEmail,
                @RequestParam int ratingScore) {
            try {
                User updated = authService.addRating(jastiperEmail, ratingScore);
                return ResponseEntity.ok(updated);
            }
            catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @GetMapping("/internal/user")
        public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
            User user = authService.findByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(new UserInternalResponse(
                        user.getUsername(),
                        user.getFullName(),
                        user.getPhoneNumber()
                ));
            }
            return ResponseEntity.notFound().build();
        }
    }