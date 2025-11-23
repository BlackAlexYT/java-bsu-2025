package com.bsu.transactions;

import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.model.TransactionType;
import com.bsu.transactions.model.User;
import com.bsu.transactions.repository.InMemoryAccountRepository;
import com.bsu.transactions.repository.InMemoryUserRepository;
import com.bsu.transactions.service.AccountService;
import com.bsu.transactions.service.TransactionService;
import com.bsu.transactions.patterns.observer.AccountSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {

    private AccountService accountService;
    private TransactionService txService;
    private UUID acc1;
    private UUID acc2;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryAccountRepository accountRepo = new InMemoryAccountRepository();
        accountService = new AccountService(accountRepo);
        AccountSubject subject = new AccountSubject();
        txService = new TransactionService(accountService, subject);

        // создаем пользователя и два счета
        User user = new User(UUID.randomUUID(), "alice");
        acc1 = UUID.randomUUID();
        acc2 = UUID.randomUUID();
        user.addAccountId(acc1);
        user.addAccountId(acc2);
        userRepo.save(user);

        accountRepo.save(new Account(acc1, BigDecimal.valueOf(1000)));
        accountRepo.save(new Account(acc2, BigDecimal.valueOf(500)));
    }

    @Test
    void testDeposit() throws Exception {
        Transaction deposit = new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.DEPOSIT,
                BigDecimal.valueOf(200), acc1, null);
        txService.processTransaction(deposit);

        BigDecimal balance = accountService.findById(acc1).get().getBalance();
        assertEquals(BigDecimal.valueOf(1200), balance);
    }

    @Test
    void testWithdraw() throws Exception {
        Transaction withdraw = new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.WITHDRAW,
                BigDecimal.valueOf(300), acc1, null);
        txService.processTransaction(withdraw);

        BigDecimal balance = accountService.findById(acc1).get().getBalance();
        assertEquals(BigDecimal.valueOf(700), balance);
    }

    @Test
    void testTransfer() throws Exception {
        Transaction transfer = new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.TRANSFER,
                BigDecimal.valueOf(400), acc1, acc2);
        txService.processTransaction(transfer);

        BigDecimal balance1 = accountService.findById(acc1).get().getBalance();
        BigDecimal balance2 = accountService.findById(acc2).get().getBalance();
        assertEquals(BigDecimal.valueOf(600), balance1);
        assertEquals(BigDecimal.valueOf(900), balance2);
    }

    @Test
    void testFreezeDoesNotChangeBalance() throws Exception {
        Transaction freeze = new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.FREEZE,
                BigDecimal.ZERO, acc1, null);
        txService.processTransaction(freeze);

        BigDecimal balance = accountService.findById(acc1).get().getBalance();
        assertEquals(BigDecimal.valueOf(1000), balance); // баланс не изменился
        assertTrue(accountService.findById(acc1).get().isFrozen());
    }

    @Test
    void testInvalidAccountThrowsException() {
        UUID invalidId = UUID.randomUUID();
        Transaction tx = new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.DEPOSIT,
                BigDecimal.valueOf(100), invalidId, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> txService.processTransaction(tx));
        assertTrue(exception.getMessage().contains("Account not found"));
    }
}
