package com.shri.loan_management_system.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PaymentRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9-]{8,36}$",
                message = "Reference must be 8 to 36 letters, digits or hyphens")
        String referenceNo
) {}