package com.shri.loan_management_system.officer.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateOfficerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits") String phone,
        @NotBlank @Size(max = 30) String employeeId,
        @NotNull @PositiveOrZero BigDecimal approvalLimit
) {}