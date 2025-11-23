package com.bsu.transactions.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public class Transaction {
    private final UUID id;
    private final Instant timestamp;
    private final TransactionType type;
    private final BigDecimal amount;
    private final UUID accountId;
    private final UUID targetAccountId;
    private String note;


    public Transaction(UUID id, Instant timestamp, TransactionType type, BigDecimal amount, UUID accountId, UUID targetAccountId) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.accountId = accountId;
        this.targetAccountId = targetAccountId;
    }


    public UUID getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getAccountId() { return accountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }


    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", amount=" + amount +
                ", accountId=" + accountId +
                ", targetAccountId=" + targetAccountId +
                '}';
    }
}

