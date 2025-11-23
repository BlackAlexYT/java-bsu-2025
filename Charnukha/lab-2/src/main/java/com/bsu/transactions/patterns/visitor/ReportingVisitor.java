package com.bsu.transactions.patterns.visitor;


import com.bsu.transactions.model.Transaction;


public class ReportingVisitor implements TransactionVisitor {
    @Override
    public void visit(Transaction tx) {
        System.out.println("REPORT: " + tx);
    }
}

