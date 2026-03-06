package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Wallet;
import com.b9.json.jsonplatform.wallet.domain.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet createWallet(UUID userId) {
        Wallet wallet = new Wallet(userId);
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }

    @Override
    @Transactional
    public Wallet topUp(UUID userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet withdraw(UUID userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);

        return walletRepository.save(wallet);
    }
}