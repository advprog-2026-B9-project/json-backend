package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import com.b9.json.jsonplatform.wallet.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
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
    @Transactional
    public User registerUser(User user) {
        user.setUsername(resolveUsername(user.getUsername(), user.getEmail()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // Every newly registered user should have exactly one wallet.
        if (walletRepository.findByUserId(savedUser.getId()).isEmpty()) {
            walletRepository.save(new Wallet(savedUser.getId()));
        }

        return savedUser;
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
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
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setAddress(updatedUser.getAddress());
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

    @Override
    public User demoteJastiper(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null && UserRole.JASTIPER.equals(user.getRole())) {
            user.setRole(UserRole.TITIPERS);
            user.setKycStatus(KycStatus.UNVERIFIED);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User banUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setBanned(true);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public long countSuccessfulTransactions(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return 0;

        Wallet wallet = walletRepository.findByUserId(user.getId()).orElse(null);
        if (wallet == null) return 0;

        // TODO: hubungkan ke Order jika fitur Order sudah selesai,
        //       idealnya hitung dari Order dengan status COMPLETED milik Jastiper
        return transactionRepository.findByWalletId(wallet.getId())
                .stream()
                .filter(t -> TransactionStatus.SUCCESS.equals(t.getStatus())
                        && TransactionType.PAYMENT.equals(t.getType()))
                .count();
    }
}