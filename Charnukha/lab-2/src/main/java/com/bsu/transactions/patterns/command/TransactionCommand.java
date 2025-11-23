package com.bsu.transactions.patterns.command;


import java.util.concurrent.CompletableFuture;


public interface TransactionCommand {
    CompletableFuture<Void> executeAsync();
}