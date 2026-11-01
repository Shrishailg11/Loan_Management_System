package com.shri.loan_management_system.schedule;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One planned installment, before it is saved to the database.
 */
public record InstallmentPlan(
        int installmentNo,
        LocalDate dueDate,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal emiAmount
) {}