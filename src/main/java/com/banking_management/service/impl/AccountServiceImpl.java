package com.banking_management.service.impl;

import com.banking_management.exception.InvalidPinException;
import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.ChangePinRequest;
import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * UC-06: Vấn tin số dư
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

    /**
     * FR-05: ADMIN/STAFF xem tài khoản của 1 user
     */
    @Override
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * FR-10: Đổi mã PIN giao dịch
     */
    @Override
    @Transactional
    public void changePin(ChangePinRequest request, String username) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!account.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You don't own this account");
        }

        if (!passwordEncoder.matches(request.getOldPin(), account.getTransactionPin())) {
            throw new InvalidPinException("Old PIN is incorrect");
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("New PIN and confirm PIN do not match");
        }

        if (passwordEncoder.matches(request.getNewPin(), account.getTransactionPin())) {
            throw new IllegalArgumentException("New PIN must be different from old PIN");
        }

        account.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);
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