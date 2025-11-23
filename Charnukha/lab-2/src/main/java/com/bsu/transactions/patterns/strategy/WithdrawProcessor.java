package com.bsu.transactions.patterns.strategy;


import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;


import java.math.BigDecimal;


public class WithdrawProcessor implements TransactionProcessorStrategy {
    @Override
    public void process(Transaction tx, Account account, Account maybeTarget) {
        if (account.isFrozen()) throw new IllegalStateException("Account is frozen");
        BigDecimal amount = tx.getAmount();
        if (account.getBalance().compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");
        account.setBalance(account.getBalance().subtract(amount));
    }
}