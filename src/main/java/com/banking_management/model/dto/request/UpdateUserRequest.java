package com.banking_management.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Email invalid format")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number invalid format")
    private String phoneNumber;

    private Boolean isActive;

    private Long roleId;
}