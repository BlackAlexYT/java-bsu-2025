package com.bsu.transactions.async;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.service.TransactionService;
import com.bsu.transactions.service.AccountService;
import java.util.UUID;



public class TransactionEngine {
    private final TransactionService transactionService;
    private final ConcurrentMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ExecutorService executor;


    public TransactionEngine(TransactionService transactionService, AccountService accountService, int threads) {
        this.transactionService = transactionService;
        this.executor = Executors.newFixedThreadPool(threads);
    }


    private ReentrantLock getLock(UUID accountId) {
        return locks.computeIfAbsent(accountId, id -> new ReentrantLock());
    }


    public CompletableFuture<Void> submit(Transaction tx) {
        return CompletableFuture.runAsync(() -> {
            if (tx.getType() == com.bsu.transactions.model.TransactionType.TRANSFER) {
                UUID a = tx.getAccountId();
                UUID b = tx.getTargetAccountId();

                ReentrantLock first = getLock(a.compareTo(b) <= 0 ? a : b);
                ReentrantLock second = getLock(a.compareTo(b) <= 0 ? b : a);
                first.lock();
                try {
                    second.lock();
                    try {
                        processSync(tx);
                    } finally {
                        second.unlock();
                    }
                } finally {
                    first.unlock();
                }
            } else {
                ReentrantLock lock = getLock(tx.getAccountId());
                lock.lock();
                try {
                    processSync(tx);
                } finally {
                    lock.unlock();
                }
            }
        }, executor);
    }


    private void processSync(Transaction tx) {
        try {
            transactionService.processTransaction(tx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void shutdown() {
        executor.shutdown();
    }
}

