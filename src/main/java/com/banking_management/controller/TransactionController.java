package com.banking_management.controller;

import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.ApiResponse;
import com.banking_management.model.dto.response.TransactionResponseDto;
import com.banking_management.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * FR-07: Chuyển tiền
     */
    @PostMapping("/transactions/transfer")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponseDto dto = transactionService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", dto));
    }

    /**
     * FR-08: Xem sao kê
     */
    @GetMapping("/transactions/{accountId}/statement")
    public ResponseEntity<ApiResponse<Page<TransactionResponseDto>>> getStatement(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<TransactionResponseDto> statement = transactionService.getStatement(
                accountId, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Statement retrieved", statement));
    }
}