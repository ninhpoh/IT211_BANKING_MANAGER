package com.banking_management.controller;

import com.banking_management.model.dto.response.AccountResponseDto;
import com.banking_management.model.dto.response.ApiResponse;
import com.banking_management.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AccountResponseDto> accounts = accountService.getMyAccounts(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved", accounts));
    }

    @GetMapping("/accounts/{id}/balance")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getBalance(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved", accountService.getBalance(id)));
    }
}