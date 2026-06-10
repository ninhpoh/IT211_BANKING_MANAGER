package com.banking_management.service.impl;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * UC-06: Vấn tin số dư - kiểm tra quyền sở hữu tài khoản
     */
    @Override
    public AccountResponseDto getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return mapToDto(account);
    }

    @Override
    public List<AccountResponseDto> getMyAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private AccountResponseDto mapToDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .active(account.getActive())
                .ownerUsername(account.getUser().getUsername())
                .createdAt(account.getCreatedAt())
                .build();
    }
}