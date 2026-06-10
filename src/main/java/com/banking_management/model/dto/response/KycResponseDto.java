package com.banking_management.model.dto.response;

import com.banking_management.model.entity.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycResponseDto {
    private Long id;
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private String sex;
    private String address;
    private String idCardFrontUrl;
    private Status status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private String username;
}