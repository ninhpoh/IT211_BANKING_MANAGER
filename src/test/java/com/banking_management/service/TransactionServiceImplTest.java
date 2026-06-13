package com.banking_management.service;

import com.banking_management.exception.InsufficientBalanceException;
import com.banking_management.exception.InvalidPinException;
import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.TransactionResponseDto;
import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.Transaction;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.TransactionRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User mockUser;
    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        mockUser = User.builder().id(1L).username("customer01").role(role).build();

        fromAccount = Account.builder()
                .id(1L).accountNumber("1000000001")
                .balance(BigDecimal.valueOf(5_000_000))
                .currency("VND").transactionPin("encodedPin")
                .active(true).user(mockUser).build();

        toAccount = Account.builder()
                .id(2L).accountNumber("1000000002")
                .balance(BigDecimal.valueOf(1_000_000))
                .currency("VND").transactionPin("encodedPin")
                .active(true).user(mockUser).build();
    }

    @Test
    void transfer_Success_UpdatesBalancesAndCreatesTransaction() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(500_000));
        request.setPin("123456");
        request.setDescription("Test transfer");

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toAccount));
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction tx = invocation.getArgument(0);
                    tx.setId(1L);
                    return tx;
                });

        // Act
        TransactionResponseDto result = transactionService.transfer(request, "customer01");

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(4_500_000), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(1_500_000), toAccount.getBalance());
        assertEquals("SUCCESS", result.getStatus());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(10_000_000)); // > balance
        request.setPin("123456");

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toAccount));
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);

        // Act & Assert
        assertThrows(InsufficientBalanceException.class,
                () -> transactionService.transfer(request, "customer01"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_InvalidPin_ThrowsException() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(100_000));
        request.setPin("wrong-pin");

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromAccount));
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong-pin", "encodedPin")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPinException.class,
                () -> transactionService.transfer(request, "customer01"));
    }

    @Test
    void transfer_NotAccountOwner_ThrowsAccessDenied() {
        // Arrange
        User anotherUser = User.builder().id(2L).username("hacker").build();

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(100_000));
        request.setPin("123456");

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromAccount));
        when(userRepository.findByUsername("hacker")).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> transactionService.transfer(request, "hacker"));
    }
}