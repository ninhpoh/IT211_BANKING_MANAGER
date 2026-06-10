package com.banking_management.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isKyc;
    private String roleName;
    private LocalDateTime createdAt;
}