package com.shri.loan_management_system.schedule.dto;

import java.math.BigDecimal;

public record OverdueSummary(
        long overdueInstallments,
        long affectedLoans,
        BigDecimal totalOverdueAmount
) {}