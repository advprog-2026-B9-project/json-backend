package com.b9.json.jsonplatform.wallet.infrastructure;

import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/users/{id}")
    public Wallet createWallet(@PathVariable("id") UUID id) {
        return walletService.createWallet(id);
    }

    @GetMapping("/{walletId}")
    public Wallet getWalletById(@PathVariable UUID walletId) {
        return walletService.getWalletById(walletId);
    }

    @GetMapping("/users/{id}")
    public Wallet getWalletByUserId(@PathVariable("id") UUID id) {
        return walletService.getWalletByUserId(id);
    }
}