package com.banking_management.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "Source account ID is required")
    private Long fromAccountId;

    @NotNull(message = "Target account ID is required")
    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotBlank(message = "Transaction PIN is required")
    @Size(min = 6, max = 6, message = "PIN must be 6 digits")
    private String pin;
}