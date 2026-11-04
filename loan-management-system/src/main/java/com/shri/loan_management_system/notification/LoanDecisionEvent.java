package com.shri.loan_management_system.notification;

import com.shri.loan_management_system.loan.LoanStatus;

import java.math.BigDecimal;

public record LoanDecisionEvent(
        String customerEmail,
        String customerName,
        String loanNo,
        String productName,
        BigDecimal amount,
        int tenureMonths,
        LoanStatus decision,
        BigDecimal interestRate,
        BigDecimal emiAmount,
        String rejectionReason
) {}