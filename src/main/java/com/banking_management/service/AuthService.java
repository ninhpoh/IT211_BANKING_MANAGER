package com.banking_management.service;

import com.banking_management.model.dto.request.LoginRequest;
import com.banking_management.model.dto.request.RefreshTokenRequest;
import com.banking_management.model.dto.request.RegisterRequest;
import com.banking_management.model.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken);
    void register(RegisterRequest request);
}