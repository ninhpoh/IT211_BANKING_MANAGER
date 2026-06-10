package com.banking_management.model.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponseDto {
    private Long id;
    private String transactionCode;
    private BigDecimal amount;
    private String description;
    private String status;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;

    // DEBIT = trừ tiền, CREDIT = cộng tiền
    private String transactionType;
}