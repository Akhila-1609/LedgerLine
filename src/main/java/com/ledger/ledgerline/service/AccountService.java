
package com.ledger.ledgerline.service;

import com.ledger.ledgerline.entity.Account;
import com.ledger.ledgerline.entity.Transaction;
import com.ledger.ledgerline.repository.AccountRepository;
import com.ledger.ledgerline.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Create a new account
    @Transactional
    public Account createAccount(String accountNumber, String holderName) {

        if (accountNumber == null || accountNumber.isBlank()
                || holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number and holder name are required");
        }

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("Account already exists");
        }

        return accountRepository.save(
                new Account(accountNumber, holderName));
    }

    // Deposit money
    @Transactional
    public Transaction deposit(
            String accountNumber, BigDecimal amount, String referenceId) {

        validateAmount(amount);
        validateReference(referenceId);

        Account account = getLockedAccount(accountNumber);
        checkDuplicate(referenceId);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                referenceId,
                account.getId(),
                null,
                Transaction.TransactionType.DEPOSIT,
                amount
        );

        transaction.setStatus(
                Transaction.TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    // Withdraw money
    @Transactional
    public Transaction withdraw(
            String accountNumber, BigDecimal amount, String referenceId) {

        validateAmount(amount);
        validateReference(referenceId);

        Account account = getLockedAccount(accountNumber);
        checkDuplicate(referenceId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                referenceId,
                account.getId(),
                null,
                Transaction.TransactionType.WITHDRAWAL,
                amount
        );

        transaction.setStatus(
                Transaction.TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    // Transfer money between accounts
    @Transactional
    public Transaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String referenceId) {

        validateAmount(amount);
        validateReference(referenceId);

        if (fromAccount.equals(toAccount)) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account");
        }

        // Lock accounts in a consistent order
        Account first;
        Account second;

        if (fromAccount.compareTo(toAccount) < 0) {
            first = getLockedAccount(fromAccount);
            second = getLockedAccount(toAccount);
        } else {
            first = getLockedAccount(toAccount);
            second = getLockedAccount(fromAccount);
        }

        checkDuplicate(referenceId);

        Account sender = first.getAccountNumber().equals(fromAccount)
                ? first : second;

        Account receiver = first.getAccountNumber().equals(toAccount)
                ? first : second;

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction(
                referenceId,
                sender.getId(),
                receiver.getId(),
                Transaction.TransactionType.TRANSFER,
                amount
        );

        transaction.setStatus(
                Transaction.TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    // Get account details and current balance
    @Transactional(readOnly = true)
    public Account getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));
    }

    // Get transaction history for an account
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        return transactionRepository
                .findByAccountIdOrTargetAccountIdOrderByCreatedAtDesc(
                        account.getId(), account.getId());
    }

    private Account getLockedAccount(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));
    }

    private void checkDuplicate(String referenceId) {
        if (transactionRepository.findByReferenceId(referenceId).isPresent()) {
            throw new IllegalArgumentException(
                    "Duplicate transaction reference");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
    }

    private void validateReference(String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction reference is required");
        }
    }
}
