package com.shri.loan_management_system.product.dto;

import com.shri.loan_management_system.product.LoanProduct;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal interestRate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        int minTenureMonths,
        int maxTenureMonths,
        boolean active
) {
    public static ProductResponse from(LoanProduct p) {
        return new ProductResponse(p.getId(), p.getName(), p.getInterestRate(),
                p.getMinAmount(), p.getMaxAmount(), p.getMinTenureMonths(),
                p.getMaxTenureMonths(), p.isActive());
    }
}