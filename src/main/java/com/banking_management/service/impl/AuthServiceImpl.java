package com.banking_management.service.impl;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.LoginRequest;
import com.banking_management.model.dto.request.RefreshTokenRequest;
import com.banking_management.model.dto.request.RegisterRequest;
import com.banking_management.model.dto.response.AuthResponse;
import com.banking_management.model.entity.RefreshToken;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.TokenBlackList;
import com.banking_management.model.entity.User;
import com.banking_management.repository.RefreshTokenRepository;
import com.banking_management.repository.RoleRepository;
import com.banking_management.repository.TokenBlackListRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    /**
     * UC-01: Đăng nhập - AuthenticationManager đối chiếu DB
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().getName())
                .build();
    }

    /**
     * UC-02: Xoay vòng RefreshToken
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.getRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired or revoked. Please login again.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().getName())
                .build();
    }

    /**
     * UC-03: Đăng xuất - đưa AccessToken vào Blacklist
     */
    @Override
    @Transactional
    public void logout(String accessToken) {
        Date expiry = jwtUtils.getExpirationFromToken(accessToken);
        TokenBlackList blackList = TokenBlackList.builder()
                .accessToken(accessToken)
                .expiryAt(expiry.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .build();
        tokenBlackListRepository.save(blackList);
    }

    /**
     * UC-04 (Register): Đăng ký tài khoản mới với role CUSTOMER
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String roleName = "CUSTOMER";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin && "STAFF".equalsIgnoreCase(request.getRole())) {
            roleName = "STAFF";
        }

        final String finalRoleName = roleName;

        Role assignedRole = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + finalRoleName + " not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isKyc(false)
                .role(assignedRole)
                .build();

        userRepository.save(user);
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(old -> {
                    refreshTokenRepository.delete(old);
                    refreshTokenRepository.flush();
                });

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }
}