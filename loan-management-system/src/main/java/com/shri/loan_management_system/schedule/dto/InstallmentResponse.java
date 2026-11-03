package com.shri.loan_management_system.schedule.dto;

import com.shri.loan_management_system.schedule.RepaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InstallmentResponse(
        int installmentNo,
        LocalDate dueDate,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal emiAmount,
        boolean paid,
        LocalDateTime paidAt,
        boolean overdue
) {
    public static InstallmentResponse from(RepaymentSchedule s) {
        boolean overdue = !s.isPaid() && s.getDueDate().isBefore(LocalDate.now());
        return new InstallmentResponse(s.getInstallmentNo(), s.getDueDate(),
                s.getPrincipalComponent(), s.getInterestComponent(), s.getEmiAmount(),
                s.isPaid(), s.getPaidAt(), overdue);
    }
}