package com.bsu.transactions.model;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String nickname;
    private final List<UUID> accountIds = new ArrayList<>();


    public User(UUID id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }


    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }


    public List<UUID> getAccountIds() { return accountIds; }
    public void addAccountId(UUID accountId) { this.accountIds.add(accountId); }
    public void removeAccountId(UUID accountId) { this.accountIds.remove(accountId); }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", accountIds=" + accountIds +
                '}';
    }
}