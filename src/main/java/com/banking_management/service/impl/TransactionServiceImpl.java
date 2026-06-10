package com.banking_management.service.impl;

import com.banking_management.exception.InsufficientBalanceException;
import com.banking_management.exception.InvalidPinException;
import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.TransactionResponseDto;
import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.Transaction;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.TransactionRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * UC-04: Chuyển tiền với @Transactional + Pessimistic Lock chống Double-spending.
     * AOP sẽ tự động ghi log @AfterReturning / @AfterThrowing.
     */
    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequest request, String username) {
        // Lấy tài khoản nguồn với PESSIMISTIC LOCK
        Account fromAccount = accountRepository.findByIdWithLock(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        // Xác minh quyền sở hữu
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!fromAccount.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You don't own this account");
        }

        // Xác minh PIN giao dịch
        if (!passwordEncoder.matches(request.getPin(), fromAccount.getTransactionPin())) {
            throw new InvalidPinException("Invalid transaction PIN");
        }

        // Lấy tài khoản đích với PESSIMISTIC LOCK
        Account toAccount = accountRepository.findByIdWithLock(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));

        // Kiểm tra số dư
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + fromAccount.getBalance() + " VND");
        }

        // Thực hiện chuyển tiền
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Tạo bản ghi giao dịch
        String txCode = "TX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Transaction transaction = Transaction.builder()
                .transactionCode(txCode)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved, null); // AOP sẽ đọc kết quả này
    }

    /**
     * UC-06: Xem sao kê - tự động tính DEBIT/CREDIT theo accountId
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getStatement(Long accountId, String username, Pageable pageable) {
        // Xác minh quyền sở hữu tài khoản
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!account.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You don't have access to this account");
        }

        return transactionRepository.findAllByAccountId(accountId, pageable)
                .map(tx -> mapToDto(tx, accountId));
    }

    private TransactionResponseDto mapToDto(Transaction tx, Long accountId) {
        // UC-06: Tính loại giao dịch DEBIT/CREDIT
        String type = null;
        if (accountId != null) {
            if (tx.getFromAccount() != null && tx.getFromAccount().getId().equals(accountId)) {
                type = "DEBIT";   // Trừ tiền
            } else if (tx.getToAccount() != null && tx.getToAccount().getId().equals(accountId)) {
                type = "CREDIT";  // Cộng tiền
            }
        }

        return TransactionResponseDto.builder()
                .id(tx.getId())
                .transactionCode(tx.getTransactionCode())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .status(tx.getStatus())
                .fromAccountNumber(tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : null)
                .createdAt(tx.getCreatedAt())
                .transactionType(type)
                .build();
    }
}