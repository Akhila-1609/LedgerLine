
package com.ledger.ledgerline.service;

import com.ledger.ledgerline.entity.Account;
import com.ledger.ledgerline.entity.Transaction;
import com.ledger.ledgerline.repository.AccountRepository;
import com.ledger.ledgerline.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("ACC1001", "Akhila");
        account.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void depositShouldIncreaseBalance() {

        when(accountRepository.findByAccountNumberForUpdate("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByReferenceId("DEP1001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = accountService.deposit(
                "ACC1001",
                new BigDecimal("200.00"),
                "DEP1001"
        );

        assertEquals(new BigDecimal("700.00"), account.getBalance());
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
    }

    @Test
    void withdrawShouldDecreaseBalance() {

        when(accountRepository.findByAccountNumberForUpdate("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByReferenceId("WDR1001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = accountService.withdraw(
                "ACC1001",
                new BigDecimal("100.00"),
                "WDR1001"
        );

        assertEquals(new BigDecimal("400.00"), account.getBalance());
        assertEquals(Transaction.TransactionType.WITHDRAWAL, result.getType());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }

    @Test
    void withdrawShouldRejectInsufficientBalance() {

        when(accountRepository.findByAccountNumberForUpdate("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByReferenceId("WDR1002"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.withdraw(
                        "ACC1001",
                        new BigDecimal("1000.00"),
                        "WDR1002"
                )
        );

        assertEquals("Insufficient balance", exception.getMessage());
        assertEquals(new BigDecimal("500.00"), account.getBalance());

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferShouldUpdateBothAccountBalances() {

        Account receiver = new Account("ACC1002", "Receiver");
        receiver.setBalance(new BigDecimal("300.00"));

        when(accountRepository.findByAccountNumberForUpdate("ACC1001"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("ACC1002"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository.findByReferenceId("TRF1001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = accountService.transfer(
                "ACC1001",
                "ACC1002",
                new BigDecimal("100.00"),
                "TRF1001"
        );

        assertEquals(new BigDecimal("400.00"), account.getBalance());
        assertEquals(new BigDecimal("400.00"), receiver.getBalance());
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());

        verify(accountRepository, times(2)).save(any(Account.class));
    }
}
