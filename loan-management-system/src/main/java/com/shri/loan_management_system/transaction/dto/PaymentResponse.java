package com.shri.loan_management_system.transaction.dto;

import com.shri.loan_management_system.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String referenceNo,
        int installmentNo,
        BigDecimal amountPaid,
        LocalDateTime paidAt,
        LoanStatus loanStatus,
        long remainingInstallments,
        boolean replayed
) {}