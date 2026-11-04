package com.shri.loan_management_system.schedule.dto;

import com.shri.loan_management_system.schedule.RepaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record OverdueInstallmentResponse(
        Long loanId,
        String loanNo,
        String customerName,
        String customerPhone,
        int installmentNo,
        LocalDate dueDate,
        BigDecimal emiAmount,
        long daysOverdue
) {
    public static OverdueInstallmentResponse from(RepaymentSchedule s, LocalDate today) {
        var loan = s.getLoan();
        var user = loan.getCustomer().getUser();
        return new OverdueInstallmentResponse(loan.getId(), loan.getLoanNo(), user.getName(),
                user.getPhone(), s.getInstallmentNo(), s.getDueDate(), s.getEmiAmount(),
                ChronoUnit.DAYS.between(s.getDueDate(), today));
    }
}