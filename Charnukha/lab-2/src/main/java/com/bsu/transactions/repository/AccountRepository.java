package com.bsu.transactions.repository;


import com.bsu.transactions.model.Account;


import java.util.Optional;
import java.util.UUID;


public interface AccountRepository {
    void save(Account account);
    Optional<Account> findById(UUID id);
    void clear();
}