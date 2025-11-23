package com.bsu.transactions.patterns.observer;


import com.bsu.transactions.model.Account;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class AccountSubject {
    private final List<AccountObserver> observers = new CopyOnWriteArrayList<>();


    public void addObserver(AccountObserver o) { observers.add(o); }
    public void removeObserver(AccountObserver o) { observers.remove(o); }
    public void notify(Account account, String event) {
        for (AccountObserver o : observers) o.onAccountChanged(account, event);
    }
}

