package com.shri.loan_management_system.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplyRequest(

        @NotNull
        Long productId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount,

        @NotNull @Positive
        Integer tenureMonths
) {}