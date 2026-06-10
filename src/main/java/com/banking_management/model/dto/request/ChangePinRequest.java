package com.banking_management.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePinRequest {

    @NotBlank(message = "Old PIN is required")
    @Size(min = 6, max = 6, message = "PIN must be 6 digits")
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    @Size(min = 6, max = 6, message = "PIN must be 6 digits")
    private String newPin;
}