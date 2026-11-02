package com.shri.loan_management_system.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal interestRate,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal minAmount,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal maxAmount,

        @NotNull @Positive
        Integer minTenureMonths,

        @NotNull @Positive
        Integer maxTenureMonths
) {}