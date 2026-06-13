package com.banking_management.service;

import com.banking_management.exception.InvalidPinException;
import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.ChangePinRequest;
import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User mockUser;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        mockUser = User.builder().id(1L).username("customer01").role(role).build();

        mockAccount = Account.builder()
                .id(1L)
                .accountNumber("1000000001")
                .balance(BigDecimal.valueOf(5_000_000))
                .currency("VND")
                .transactionPin("encodedPin")
                .active(true)
                .user(mockUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getBalance_AccountExists_ReturnsDto() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));

        // Act
        AccountResponseDto dto = accountService.getBalance(1L);

        // Assert
        assertNotNull(dto);
        assertEquals("1000000001", dto.getAccountNumber());
        assertEquals(BigDecimal.valueOf(5_000_000), dto.getBalance());
        assertEquals("customer01", dto.getOwnerUsername());
    }

    @Test
    void getBalance_AccountNotFound_ThrowsException() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getBalance(99L));
        assertTrue(ex.getMessage().contains("Account not found"));
    }

    @Test
    void changePin_OldPinIncorrect_ThrowsInvalidPinException() {
        // Arrange
        ChangePinRequest request = new ChangePinRequest();
        request.setAccountId(1L);
        request.setOldPin("000000");
        request.setNewPin("654321");
        request.setConfirmPin("654321");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("000000", "encodedPin")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPinException.class,
                () -> accountService.changePin(request, "customer01"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void changePin_Success_UpdatesPin() {
        // Arrange
        ChangePinRequest request = new ChangePinRequest();
        request.setAccountId(1L);
        request.setOldPin("123456");
        request.setNewPin("654321");
        request.setConfirmPin("654321");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);
        when(passwordEncoder.matches("654321", "encodedPin")).thenReturn(false);
        when(passwordEncoder.encode("654321")).thenReturn("newEncodedPin");

        // Act
        accountService.changePin(request, "customer01");

        // Assert
        verify(accountRepository, times(1)).save(mockAccount);
        assertEquals("newEncodedPin", mockAccount.getTransactionPin());
    }
}