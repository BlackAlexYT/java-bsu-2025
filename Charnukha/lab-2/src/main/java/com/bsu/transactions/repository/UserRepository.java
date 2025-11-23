package com.bsu.transactions.repository;


import com.bsu.transactions.model.User;


import java.util.Optional;
import java.util.UUID;


public interface UserRepository {
    void save(User user);
    Optional<User> findById(UUID id);
}