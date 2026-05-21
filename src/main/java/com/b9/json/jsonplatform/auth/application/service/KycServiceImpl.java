package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KycServiceImpl implements KycService {

    private final UserRepository userRepository;

    public KycServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User submitKyc(String email, String fullName, String nikKtp, String ktpImageUrl) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setFullName(fullName);
            user.setNikKtp(nikKtp);
            user.setKtpImageUrl(ktpImageUrl);
            user.setKycStatus(KycStatus.PENDING_VERIFICATION);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public List<User> findPendingKyc() {
        return userRepository.findAll().stream()
                .filter(u -> KycStatus.PENDING_VERIFICATION.equals(u.getKycStatus()))
                .toList();
    }

    @Override
    public User reviewKyc(String email, boolean approved) {
        User user = userRepository.findByEmail(email);

        if (user != null && KycStatus.PENDING_VERIFICATION.equals(user.getKycStatus())) {
            if (approved) {
                user.setKycStatus(KycStatus.VERIFIED);
                user.setRole(UserRole.JASTIPER);
            } else {
                user.setKycStatus(KycStatus.UNVERIFIED);
            }
            return userRepository.save(user);
        }
        return null;
    }
}