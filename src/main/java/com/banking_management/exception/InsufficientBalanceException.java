package com.banking_management.exception;

import java.math.BigDecimal;

/**
 * UC-04: Ném ra khi số dư tài khoản nguồn không đủ để thực hiện giao dịch.
 * GlobalExceptionHandler -> HTTP 409 Conflict.
 */
public class InsufficientBalanceException extends RuntimeException {

    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format(
                "Insufficient balance. Available: %.2f VND, Required: %.2f VND",
                currentBalance, requiredAmount
        ));
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public InsufficientBalanceException(String message) {
        super(message);
        this.currentBalance = null;
        this.requiredAmount = null;
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getRequiredAmount() { return requiredAmount; }
}