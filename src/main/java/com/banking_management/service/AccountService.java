package com.banking_management.service;

import com.banking_management.model.dto.request.ChangePinRequest;
import com.banking_management.model.dto.response.AccountResponseDto;

import java.util.List;

public interface AccountService {
    AccountResponseDto getBalance(Long accountId);
    List<AccountResponseDto> getMyAccounts(String username);
    List<AccountResponseDto> getAccountsByUserId(Long userId);
    void changePin(ChangePinRequest request, String username);
}