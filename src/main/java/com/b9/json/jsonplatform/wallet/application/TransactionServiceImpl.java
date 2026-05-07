package com.b9.json.jsonplatform.wallet.application;

import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionStatus;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import com.b9.json.jsonplatform.wallet.domain.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletService walletService;

    @Override
    public Transaction createTransaction(
            UUID walletId,
            TransactionType type,
            BigDecimal amount,
            String description
    ) {
        return createTransaction(walletId, null, type, amount, description);
        }

        private Transaction createTransaction(
            UUID walletId,
            UUID targetWalletId,
            TransactionType type,
            BigDecimal amount,
            String description
        ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Transaction transaction = new Transaction(
                walletId,
            targetWalletId,
                type,
                amount,
                description
        );

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction markSuccess(UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transaction can be updated");
        }

        if (transaction.getType() == TransactionType.TOP_UP) {
            walletService.increaseBalance(transaction.getWalletId(), transaction.getAmount());
        }

        if (transaction.getType() == TransactionType.WITHDRAWAL) {
            walletService.decreaseBalance(transaction.getWalletId(), transaction.getAmount());
        }

        if (transaction.getType() == TransactionType.PAYMENT) {
            if (transaction.getTargetWalletId() == null) {
                throw new IllegalStateException("Target wallet is required for PAYMENT");
            }

            walletService.decreaseBalance(transaction.getWalletId(), transaction.getAmount());
            walletService.increaseBalance(transaction.getTargetWalletId(), transaction.getAmount());
        }

        if (transaction.getType() == TransactionType.REFUND) {
            if (transaction.getTargetWalletId() == null) {
                throw new IllegalStateException("Target wallet is required for REFUND");
            }

            walletService.increaseBalance(transaction.getWalletId(), transaction.getAmount());
            walletService.decreaseBalance(transaction.getTargetWalletId(), transaction.getAmount());
        }

        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction markFailed(UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transaction can be updated");
        }

        transaction.setStatus(TransactionStatus.FAILED);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getWalletTransactions(UUID walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

    @Override
    @Transactional
    public Transaction createTopUp(UUID walletId, BigDecimal amount) {
        return createTransaction(walletId, TransactionType.TOP_UP, amount, "Top Up");
    }

    @Override
    @Transactional
    public Transaction createWithdrawal(UUID walletId, BigDecimal amount) {
        return createTransaction(walletId, TransactionType.WITHDRAWAL, amount, "Withdrawal");
    }

    @Override
    @Transactional
    public Transaction createPayment(UUID walletId, UUID targetWalletId, BigDecimal amount) {
        return createTransaction(walletId, targetWalletId, TransactionType.PAYMENT, amount, "Payment");
    }

    @Override
    @Transactional
    public Transaction createRefund(UUID walletId, UUID targetWalletId, BigDecimal amount) {
        return createTransaction(walletId, targetWalletId, TransactionType.REFUND, amount, "Refund");
    }
}