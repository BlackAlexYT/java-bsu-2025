package com.bsu.transactions.service;


import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.model.TransactionType;
import com.bsu.transactions.patterns.observer.AccountSubject;
import com.bsu.transactions.patterns.strategy.DepositProcessor;
import com.bsu.transactions.patterns.strategy.TransactionProcessorStrategy;
import com.bsu.transactions.patterns.strategy.WithdrawProcessor;
import com.bsu.transactions.patterns.strategy.TransferProcessor;


import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public record TransactionService(AccountService accountService, AccountSubject accountSubject) {


    public CompletableFuture<Transaction> processTransactionAsync(Transaction tx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                processTransaction(tx);
                return tx;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void processTransaction(Transaction tx) throws Exception {
        if (tx.getAmount() != null && tx.getAmount().compareTo(BigDecimal.ZERO) <= 0
                && tx.getType() != TransactionType.FREEZE) { // FREEZE может быть без суммы
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        Optional<Account> maybe = accountService.findById(tx.getAccountId());
        if (maybe.isEmpty()) throw new IllegalArgumentException("Account not found: " + tx.getAccountId());
        Account account = maybe.get();
        Account target = null;

        if (tx.getType() == TransactionType.TRANSFER) {
            target = accountService.findById(tx.getTargetAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

            // --- проверка баланса для TRANSFER ---
            if (account.getBalance().compareTo(tx.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient balance on source account " + account.getId());
            }
        }

        TransactionProcessorStrategy processor = switch (tx.getType()) {
            case DEPOSIT -> new DepositProcessor();
            case WITHDRAW -> {
                // проверка баланса для WITHDRAW
                if (account.getBalance().compareTo(tx.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient balance on account " + account.getId());
                }
                yield new WithdrawProcessor();
            }
            case TRANSFER -> new TransferProcessor();
            case FREEZE -> (t, a, t2) -> a.setFrozen(true);
            default -> throw new IllegalStateException("Unknown type");
        };

        processor.process(tx, account, target);

        accountService.save(account);
        if (target != null) accountService.save(target);

        accountSubject.notify(account, "CHANGED");
    }

}

