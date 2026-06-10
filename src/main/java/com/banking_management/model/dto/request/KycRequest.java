package com.banking_management.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class KycRequest {

    @NotBlank(message = "ID number is required")
    @Size(min = 9, max = 12, message = "ID number must be 9-12 characters")
    private String idNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotBlank(message = "Sex is required")
    private String sex;

    @NotBlank(message = "Address is required")
    private String address;
}