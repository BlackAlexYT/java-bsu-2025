package com.bsu.transactions.patterns.strategy;


import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;


public class TransferProcessor implements TransactionProcessorStrategy {
    @Override
    public void process(Transaction tx, Account account, Account maybeTarget) {
        if (account.isFrozen() || (maybeTarget != null && maybeTarget.isFrozen())) throw new IllegalStateException("One of accounts is frozen");
        account.setBalance(account.getBalance().subtract(tx.getAmount()));
        if (maybeTarget != null) maybeTarget.setBalance(maybeTarget.getBalance().add(tx.getAmount()));
    }
}

