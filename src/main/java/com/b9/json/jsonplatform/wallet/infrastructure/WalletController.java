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

    @PostMapping("/{userId}")
    public Wallet createWallet(@PathVariable UUID userId) {
        return walletService.createWallet(userId);
    }

    @GetMapping("/{userId}")
    public Wallet getWalletByUserId(@PathVariable UUID userId) {
        return walletService.getWalletByUserId(userId);
    }

}