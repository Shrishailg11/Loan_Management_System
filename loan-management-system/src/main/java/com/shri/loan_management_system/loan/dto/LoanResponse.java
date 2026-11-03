package com.shri.loan_management_system.loan.dto;

import com.shri.loan_management_system.loan.Loan;
import com.shri.loan_management_system.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        String loanNo,
        String productName,
        BigDecimal principal,
        int tenureMonths,
        BigDecimal interestRate,
        BigDecimal emiAmount,
        LoanStatus status,
        LocalDateTime appliedAt,
        LocalDateTime decidedAt,
        LocalDateTime disbursedAt
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(loan.getId(), loan.getLoanNo(), loan.getProduct().getName(),
                loan.getPrincipal(), loan.getTenureMonths(), loan.getInterestRate(),
                loan.getEmiAmount(), loan.getStatus(), loan.getAppliedAt(),
                loan.getDecidedAt(), loan.getDisbursedAt());
    }
}