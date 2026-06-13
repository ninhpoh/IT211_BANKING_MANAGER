package com.banking_management.controller;

import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.security.filter.JwtAuthenticationFilter;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AccountService accountService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "customer01", roles = "CUSTOMER")
    void getMyAccounts_AuthenticatedCustomer_Returns200() throws Exception {
        // Arrange
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L).accountNumber("1000000001")
                .balance(BigDecimal.valueOf(5_000_000))
                .currency("VND").active(true)
                .ownerUsername("customer01")
                .build();

        when(accountService.getMyAccounts("customer01")).thenReturn(List.of(dto));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].accountNumber").value("1000000001"))
                .andExpect(jsonPath("$.data[0].balance").value(5000000));
    }

    @Test
    @WithMockUser(username = "customer01", roles = "CUSTOMER")
    void getBalance_AccountExists_Returns200() throws Exception {
        // Arrange
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L).accountNumber("1000000001")
                .balance(BigDecimal.valueOf(5_000_000))
                .currency("VND").active(true)
                .ownerUsername("customer01")
                .build();

        when(accountService.getBalance(1L)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(5000000))
                .andExpect(jsonPath("$.data.currency").value("VND"));
    }
}