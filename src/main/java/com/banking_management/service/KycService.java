package com.banking_management.service;

import com.banking_management.model.dto.request.KycRequest;
import com.banking_management.model.dto.response.KycResponseDto;
import com.banking_management.model.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface KycService {
    KycResponseDto submitKyc(KycRequest request, MultipartFile idCardFile, String username);
    KycResponseDto reviewKyc(Long kycId, Status decision);
    Page<KycResponseDto> getPendingKyc(Pageable pageable);
    KycResponseDto getMyKyc(String username);
}