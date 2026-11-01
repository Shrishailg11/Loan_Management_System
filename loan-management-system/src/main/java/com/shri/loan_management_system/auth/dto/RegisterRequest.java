package com.shri.loan_management_system.auth.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        String password,

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        String phone,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{5}[0-9]{4}[A-Za-z]$", message = "Invalid PAN format")
        String panNumber,

        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal monthlyIncome,

        @NotNull @Past
        LocalDate dateOfBirth
) {}