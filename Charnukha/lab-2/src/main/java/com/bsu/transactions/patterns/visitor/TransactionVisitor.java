package com.bsu.transactions.patterns.visitor;


import com.bsu.transactions.model.Transaction;


public interface TransactionVisitor {
    void visit(Transaction tx);
}