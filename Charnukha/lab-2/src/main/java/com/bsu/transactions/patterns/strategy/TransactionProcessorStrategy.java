package com.bsu.transactions.patterns.strategy;


import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.model.Account;


public interface TransactionProcessorStrategy {
    void process(Transaction tx, Account account, Account maybeTarget) throws Exception;
}