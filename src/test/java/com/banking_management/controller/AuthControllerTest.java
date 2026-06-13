package com.banking_management.controller;

import com.banking_management.model.dto.request.LoginRequest;
import com.banking_management.model.dto.response.AuthResponse;
import com.banking_management.security.filter.JwtAuthenticationFilter;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    // Mock các bean SecurityConfig cần để load context
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtils jwtUtils;

    @Test
    void login_ValidCredentials_Returns200() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("customer01");
        request.setPassword("12345678");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-token")
                .refreshToken("mock-refresh")
                .tokenType("Bearer")
                .username("customer01")
                .role("CUSTOMER")
                .build();

        when(authService.login(any())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("mock-token"))
                .andExpect(jsonPath("$.data.username").value("customer01"));
    }

    @Test
    void login_MissingUsername_Returns400() throws Exception {
        // Arrange - thiếu username
        String invalidJson = """
                {
                    "password": "12345678"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private static org.mockito.ArgumentMatchers any() {
        return org.mockito.ArgumentMatchers.any();
    }
}