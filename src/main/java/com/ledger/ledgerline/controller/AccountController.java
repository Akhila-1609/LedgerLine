
package com.ledger.ledgerline.controller;

import com.ledger.ledgerline.entity.Account;
import com.ledger.ledgerline.entity.Transaction;
import com.ledger.ledgerline.service.AccountService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    public record CreateAccountRequest(
            @NotBlank String accountNumber,
            @NotBlank String holderName) {}

    public record MoneyRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String referenceId) {}

    public record TransferRequest(
            @NotBlank String fromAccount,
            @NotBlank String toAccount,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String referenceId) {}

    // Create account
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(
                request.accountNumber(), request.holderName());
    }

    // Deposit money
    @PostMapping("/accounts/{accountNumber}/deposit")
    public Transaction deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody MoneyRequest request) {
        return accountService.deposit(
                accountNumber, request.amount(), request.referenceId());
    }

    // Withdraw money
    @PostMapping("/accounts/{accountNumber}/withdraw")
    public Transaction withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody MoneyRequest request) {
        return accountService.withdraw(
                accountNumber, request.amount(), request.referenceId());
    }

    // Transfer money
    @PostMapping("/transfers")
    public Transaction transfer(
            @Valid @RequestBody TransferRequest request) {
        return accountService.transfer(
                request.fromAccount(),
                request.toAccount(),
                request.amount(),
                request.referenceId());
    }

    // Get account details and balance
    @GetMapping("/accounts/{accountNumber}")
    public Account getAccountDetails(
            @PathVariable String accountNumber) {
        return accountService.getAccountDetails(accountNumber);
    }

    // Get account transaction history
    @GetMapping("/accounts/{accountNumber}/transactions")
    public List<Transaction> getTransactionHistory(
            @PathVariable String accountNumber) {
        return accountService.getTransactionHistory(accountNumber);
    }
}
