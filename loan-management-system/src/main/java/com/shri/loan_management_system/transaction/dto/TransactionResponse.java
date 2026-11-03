package com.shri.loan_management_system.transaction.dto;

import com.shri.loan_management_system.transaction.LoanTransaction;
import com.shri.loan_management_system.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String referenceNo,
        Integer installmentNo,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(LoanTransaction t) {
        Integer installmentNo = t.getSchedule() == null ? null : t.getSchedule().getInstallmentNo();
        return new TransactionResponse(t.getId(), t.getType(), t.getAmount(),
                t.getReferenceNo(), installmentNo, t.getCreatedAt());
    }
}