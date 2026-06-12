package com.banking_management.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePinRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotBlank(message = "Old PIN is required")
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "\\d{6}", message = "PIN must be exactly 6 digits")
    private String newPin;

    @NotBlank(message = "Confirm PIN is required")
    private String confirmPin;
}