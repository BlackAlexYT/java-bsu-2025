package com.bsu.transactions.patterns.singleton;


import java.sql.Connection;

public class DatabaseConnection {
    private static volatile DatabaseConnection INSTANCE;
    private final Connection connection;


    private DatabaseConnection() {
        try {
            this.connection = null; // stub
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static DatabaseConnection getInstance() {
        if (INSTANCE == null) {
            synchronized (DatabaseConnection.class) {
                if (INSTANCE == null) INSTANCE = new DatabaseConnection();
            }
        }
        return INSTANCE;
    }


    public Connection getConnection() {
        return connection;
    }
}