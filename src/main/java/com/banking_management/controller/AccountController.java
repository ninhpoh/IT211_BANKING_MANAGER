package com.banking_management.controller;

import com.banking_management.model.dto.request.ChangePinRequest;
import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.model.dto.response.ApiResponse;
import com.banking_management.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * FR-06: Lấy danh sách tài khoản của mình
     * GET /api/v1/customer/accounts
     */
    @GetMapping("/api/v1/customer/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AccountResponseDto> accounts = accountService.getMyAccounts(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved", accounts));
    }

    /**
     * FR-06: Xem số dư theo accountId
     * GET /api/v1/customer/accounts/{id}/balance
     */
    @GetMapping("/api/v1/customer/accounts/{id}/balance")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getBalance(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved", accountService.getBalance(id)));
    }

    /**
     * FR-05: ADMIN/STAFF xem tài khoản của 1 user
     * GET /api/v1/users/{userId}/accounts
     */
    @GetMapping("/api/v1/users/{userId}/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAccountsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved",
                accountService.getAccountsByUserId(userId)));
    }

    /**
     * FR-10: Đổi mã PIN giao dịch
     * PUT /api/v1/customer/accounts/change-pin
     */
    @PutMapping("/api/v1/customer/accounts/change-pin")
    public ResponseEntity<ApiResponse<Void>> changePin(
            @Valid @RequestBody ChangePinRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        accountService.changePin(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("PIN changed successfully"));
    }
}