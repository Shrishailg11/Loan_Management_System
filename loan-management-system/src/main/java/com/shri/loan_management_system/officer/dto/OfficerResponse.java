package com.shri.loan_management_system.officer.dto;

import java.math.BigDecimal;

public record OfficerResponse(
        Long userId,
        Long officerId,
        String name,
        String email,
        String employeeId,
        BigDecimal approvalLimit
) {}