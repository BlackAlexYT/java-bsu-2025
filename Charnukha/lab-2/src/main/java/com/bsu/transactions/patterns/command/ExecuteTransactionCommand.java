package com.bsu.transactions.patterns.command;


import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.service.TransactionService;


import java.util.concurrent.CompletableFuture;


public class ExecuteTransactionCommand implements TransactionCommand {
    private final Transaction tx;
    private final TransactionService service;


    public ExecuteTransactionCommand(Transaction tx, TransactionService service) {
        this.tx = tx;
        this.service = service;
    }


    @Override
    public CompletableFuture<Void> executeAsync() {
        return service.processTransactionAsync(tx).thenApply(r -> null);
    }
}

