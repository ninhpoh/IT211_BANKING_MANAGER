package com.banking_management.exception;

/**
 * Ném ra khi mã PIN giao dịch nhập vào không khớp (UC-04, FR-10).
 * GlobalExceptionHandler -> HTTP 401 Unauthorized.
 */
public class InvalidPinException extends RuntimeException {

    public InvalidPinException() {
        super("Invalid transaction PIN");
    }

    public InvalidPinException(String message) {
        super(message);
    }
}