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

    @Override
    @Transactional
    public Transaction createTransaction(
            UUID userId,
            TransactionType type,
            BigDecimal amount,
            String description
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Transaction transaction = new Transaction(
                userId,
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

        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction markFailed(UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        transaction.setStatus(TransactionStatus.FAILED);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getUserTransactions(UUID userId) {
        return transactionRepository.findByUserId(userId);
    }
}