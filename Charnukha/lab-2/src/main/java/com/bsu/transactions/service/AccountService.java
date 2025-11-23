package com.bsu.transactions.service;


import com.bsu.transactions.model.Account;
import com.bsu.transactions.repository.AccountRepository;


import java.util.Optional;
import java.util.UUID;


public record AccountService(AccountRepository accountRepository) {


    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }


    public void save(Account account) {
        accountRepository.save(account);
    }

    public void clear() {
        accountRepository.clear();
    }

}

