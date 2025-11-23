package com.bsu.transactions.patterns.observer;


import com.bsu.transactions.model.Account;


public interface AccountObserver {
    void onAccountChanged(Account account, String event);
}