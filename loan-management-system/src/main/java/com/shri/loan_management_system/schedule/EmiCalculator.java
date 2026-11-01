package com.shri.loan_management_system.schedule;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EmiCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private EmiCalculator() {}

    /**
     * EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     * P = principal, r = monthly rate, n = tenure in months
     */
    public static BigDecimal calculateEmi(BigDecimal principal,
                                          BigDecimal annualRatePercent,
                                          int tenureMonths) {
        validate(principal, annualRatePercent, tenureMonths);

        BigDecimal r = monthlyRate(annualRatePercent);
        BigDecimal growth = BigDecimal.ONE.add(r).pow(tenureMonths, MC);   // (1 + r)^n

        BigDecimal numerator = principal.multiply(r).multiply(growth);
        BigDecimal denominator = growth.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * Builds the full reducing-balance schedule.
     * The last installment absorbs any rounding difference so the balance ends at exactly zero.
     */
    public static List<InstallmentPlan> generateSchedule(BigDecimal principal,
                                                         BigDecimal annualRatePercent,
                                                         int tenureMonths,
                                                         LocalDate firstDueDate) {
        BigDecimal emi = calculateEmi(principal, annualRatePercent, tenureMonths);
        BigDecimal r = monthlyRate(annualRatePercent);

        List<InstallmentPlan> schedule = new ArrayList<>(tenureMonths);
        BigDecimal balance = principal;

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interest = balance.multiply(r).setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalPart;
            BigDecimal installmentAmount;
            if (i == tenureMonths) {
                principalPart = balance;                       // clear whatever is left
                installmentAmount = principalPart.add(interest);
            } else {
                principalPart = emi.subtract(interest);
                installmentAmount = emi;
            }

            balance = balance.subtract(principalPart);
            LocalDate dueDate = firstDueDate.plusMonths(i - 1);

            schedule.add(new InstallmentPlan(i, dueDate, principalPart, interest, installmentAmount));
        }
        return schedule;
    }

    private static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent.divide(MONTHS_IN_YEAR.multiply(HUNDRED), MC);
    }

    private static void validate(BigDecimal principal, BigDecimal rate, int tenureMonths) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero");
        }
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be at least one month");
        }
    }
}