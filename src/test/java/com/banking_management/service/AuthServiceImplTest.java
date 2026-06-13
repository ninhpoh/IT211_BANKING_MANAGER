package com.banking_management.service;

import com.banking_management.model.dto.request.LoginRequest;
import com.banking_management.model.dto.response.AuthResponse;
import com.banking_management.model.entity.RefreshToken;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.User;
import com.banking_management.repository.RefreshTokenRepository;
import com.banking_management.repository.RoleRepository;
import com.banking_management.repository.TokenBlackListRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenBlackListRepository tokenBlackListRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);

        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        mockUser = User.builder()
                .id(1L)
                .username("customer01")
                .password("encodedPassword")
                .role(role)
                .isActive(true)
                .build();
    }

    @Test
    void login_Success_ReturnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("customer01");
        request.setPassword("12345678");

        Authentication authentication = new UsernamePasswordAuthenticationToken(mockUser, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateAccessToken(mockUser)).thenReturn("mock-access-token");
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken rt = invocation.getArgument(0);
                    rt.setToken("mock-refresh-token");
                    return rt;
                });

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("customer01", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        com.banking_management.model.dto.request.RegisterRequest request =
                new com.banking_management.model.dto.request.RegisterRequest();
        request.setUsername("customer01");
        request.setPassword("12345678");
        request.setEmail("new@bank.com");

        when(userRepository.existsByUsername("customer01")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void logout_Success_SavesToBlacklist() {
        // Arrange
        String accessToken = "valid-token";
        when(jwtUtils.getExpirationFromToken(accessToken))
                .thenReturn(java.util.Date.from(Instant.now().plusSeconds(3600)));

        // Act
        authService.logout(accessToken);

        // Assert
        verify(tokenBlackListRepository, times(1)).save(any());
    }
}