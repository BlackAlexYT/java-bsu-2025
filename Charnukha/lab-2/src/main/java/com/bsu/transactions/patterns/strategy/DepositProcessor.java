package com.bsu.transactions.patterns.strategy;


import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;


import java.math.BigDecimal;


public class DepositProcessor implements TransactionProcessorStrategy {
    @Override
    public void process(Transaction tx, Account account, Account maybeTarget) {
        if (account.isFrozen()) throw new IllegalStateException("Account is frozen");
        BigDecimal newBal = account.getBalance().add(tx.getAmount());
        account.setBalance(newBal);
    }
}

