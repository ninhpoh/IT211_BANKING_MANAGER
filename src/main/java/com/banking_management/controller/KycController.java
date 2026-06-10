package com.banking_management.controller;

import com.banking_management.model.dto.request.KycRequest;
import com.banking_management.model.dto.response.ApiResponse;
import com.banking_management.model.dto.response.KycResponseDto;
import com.banking_management.model.entity.Status;
import com.banking_management.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    /**
     * FR-04: Nộp hồ sơ eKYC
     * POST /api/v1/kyc/upload
     */
    @PostMapping("/api/v1/kyc/upload")
    public ResponseEntity<ApiResponse<KycResponseDto>> submitKyc(
            @ModelAttribute KycRequest request,
            @RequestParam("idCardFile") MultipartFile idCardFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        KycResponseDto dto = kycService.submitKyc(request, idCardFile, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("KYC submitted successfully", dto));
    }

    /**
     * FR-04: Xem KYC của mình
     * GET /api/v1/customer/kyc/me
     */
    @GetMapping("/api/v1/customer/kyc/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<KycResponseDto>> getMyKyc(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("KYC retrieved",
                kycService.getMyKyc(userDetails.getUsername())));
    }

    /**
     * FR-09: Duyệt KYC - STAFF/ADMIN
     * PUT /api/v1/staff/kyc/{id}/review?decision=CONFIRM
     */
    @PutMapping("/api/v1/staff/kyc/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<KycResponseDto>> reviewKyc(
            @PathVariable Long id,
            @RequestParam Status decision) {
        return ResponseEntity.ok(ApiResponse.success("KYC reviewed",
                kycService.reviewKyc(id, decision)));
    }

    /**
     * FR-09: Danh sách KYC đang PENDING - STAFF/ADMIN
     * GET /api/v1/staff/kyc/pending
     */
    @GetMapping("/api/v1/staff/kyc/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<KycResponseDto>>> getPendingKyc(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Pending KYC list",
                kycService.getPendingKyc(pageable)));
    }
}