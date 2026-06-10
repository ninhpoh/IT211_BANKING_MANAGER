package com.banking_management.service;

import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.TransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponseDto transfer(TransferRequest request, String username);
    Page<TransactionResponseDto> getStatement(Long accountId, String username, Pageable pageable);
}