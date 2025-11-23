package com.bsu.transactions.patterns;


import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.model.TransactionType;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public class TransactionFactory {
    public static Transaction createDeposit(UUID accountId, BigDecimal amount) {
        return new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.DEPOSIT, amount, accountId, null);
    }


    public static Transaction createWithdraw(UUID accountId, BigDecimal amount) {
        return new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.WITHDRAW, amount, accountId, null);
    }


    public static Transaction createFreeze(UUID accountId) {
        return new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.FREEZE, null, accountId, null);
    }


    public static Transaction createTransfer(UUID from, UUID to, BigDecimal amount) {
        return new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.TRANSFER, amount, from, to);
    }
}