package com.bsu.transactions.repository;


import com.bsu.transactions.model.Account;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class InMemoryAccountRepository implements AccountRepository {
    private final Map<UUID, Account> store = new ConcurrentHashMap<>();

    @Override
    public void save(Account account) {
        store.put(account.getId(), account);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void clear() {
        store.clear();
    }
}
