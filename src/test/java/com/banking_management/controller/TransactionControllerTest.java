package com.banking_management.controller;

import com.banking_management.exception.InsufficientBalanceException;
import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.TransactionResponseDto;
import com.banking_management.security.filter.JwtAuthenticationFilter;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TransactionService transactionService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "customer01", roles = "CUSTOMER")
    void transfer_ValidRequest_Returns200() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(100_000));
        request.setPin("123456");
        request.setDescription("Test transfer");

        TransactionResponseDto responseDto = TransactionResponseDto.builder()
                .id(1L)
                .transactionCode("TX123456")
                .amount(BigDecimal.valueOf(100_000))
                .status("SUCCESS")
                .fromAccountNumber("1000000001")
                .toAccountNumber("1000000002")
                .build();

        when(transactionService.transfer(any(), eq("customer01"))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionCode").value("TX123456"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "customer01", roles = "CUSTOMER")
    void transfer_InsufficientBalance_Returns409() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(99_999_999));
        request.setPin("123456");

        when(transactionService.transfer(any(), eq("customer01")))
                .thenThrow(new InsufficientBalanceException("Insufficient balance. Available: 5000000 VND"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void transfer_NoAuthentication_Returns401() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(100_000));
        request.setPin("123456");

        // Act & Assert - không có @WithMockUser
        mockMvc.perform(post("/api/v1/customer/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}