package com.b9.json.jsonplatform.wallet.infrastructure;

import com.b9.json.jsonplatform.wallet.application.TransactionService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("")
    public Transaction createTransaction(
            @RequestParam UUID walletId,
            @RequestParam TransactionType type,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description
    ) {
        return transactionService.createTransaction(walletId, type, amount, description);
    }

    @PostMapping("/topup")
    public Transaction createTopUp(
            @RequestParam UUID walletId,
            @RequestParam BigDecimal amount
    ) {
        return transactionService.createTopUp(walletId, amount);
    }

    @PostMapping("/withdrawal")
    public Transaction createWithdrawal(
            @RequestParam UUID walletId,
            @RequestParam BigDecimal amount
    ) {
        return transactionService.createWithdrawal(walletId, amount);
    }

    @PostMapping("/payment")
    public Transaction createPayment(
            @RequestParam UUID walletId,
            @RequestParam UUID targetWalletId,
            @RequestParam BigDecimal amount
    ) {
        return transactionService.createPayment(walletId, targetWalletId, amount);
    }

    @PostMapping("/refund")
    public Transaction createRefund(
            @RequestParam UUID walletId,
            @RequestParam UUID targetWalletId,
            @RequestParam BigDecimal amount
    ) {
        return transactionService.createRefund(walletId, targetWalletId, amount);
    }

    @PostMapping("/{transactionId}/success")
    public Transaction markSuccess(@PathVariable UUID transactionId) {
        return transactionService.markSuccess(transactionId);
    }

    @PostMapping("/{transactionId}/failed")
    public Transaction markFailed(@PathVariable UUID transactionId) {
        return transactionService.markFailed(transactionId);
    }

    @GetMapping("/wallets/{walletId}")
    public List<Transaction> getWalletTransactions(@PathVariable UUID walletId) {
        return transactionService.getWalletTransactions(walletId);
    }
}
