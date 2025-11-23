package com.bsu.transactions.model;


import java.math.BigDecimal;
import java.util.UUID;


public class Account {
    private final UUID id;
    private volatile BigDecimal balance;
    private volatile boolean frozen;


    public Account(UUID id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
        this.frozen = false;
    }


    public UUID getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }


    public synchronized void apply(BigDecimal amount) {
        if (frozen) throw new UnsupportedOperationException("Аккаунт заморожен");
        balance = balance.add(amount);
    }


    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                ", frozen=" + frozen +
                '}';
    }
}