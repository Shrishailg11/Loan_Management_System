package com.shri.loan_management_system.schedule;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmiCalculatorTest {

    private static final BigDecimal PRINCIPAL = new BigDecimal("500000");
    private static final BigDecimal RATE = new BigDecimal("12");
    private static final LocalDate FIRST_DUE = LocalDate.of(2026, 12, 5);

    @Test
    void calculatesEmiForKnownExample() {
        BigDecimal emi = EmiCalculator.calculateEmi(PRINCIPAL, RATE, 12);

        assertThat(emi).isEqualByComparingTo("44424.39");
    }

    @Test
    void scheduleHasOneRowPerMonth() {
        List<InstallmentPlan> schedule =
                EmiCalculator.generateSchedule(PRINCIPAL, RATE, 12, FIRST_DUE);

        assertThat(schedule).hasSize(12);
        assertThat(schedule.get(0).installmentNo()).isEqualTo(1);
        assertThat(schedule.get(11).installmentNo()).isEqualTo(12);
    }

    @Test
    void firstInstallmentChargesOneMonthInterestOnFullPrincipal() {
        InstallmentPlan first =
                EmiCalculator.generateSchedule(PRINCIPAL, RATE, 12, FIRST_DUE).get(0);

        assertThat(first.interestComponent()).isEqualByComparingTo("5000.00");
        assertThat(first.principalComponent()).isEqualByComparingTo("39424.39");
    }

    @Test
    void principalComponentsSumExactlyToPrincipal() {
        BigDecimal awkwardPrincipal = new BigDecimal("123457");
        BigDecimal awkwardRate = new BigDecimal("8.75");

        List<InstallmentPlan> schedule =
                EmiCalculator.generateSchedule(awkwardPrincipal, awkwardRate, 36, FIRST_DUE);

        BigDecimal totalPrincipal = schedule.stream()
                .map(InstallmentPlan::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalPrincipal).isEqualByComparingTo(awkwardPrincipal);
    }

    @Test
    void lastInstallmentOnlyDiffersFromEmiByRounding() {
        BigDecimal emi = EmiCalculator.calculateEmi(PRINCIPAL, RATE, 12);
        List<InstallmentPlan> schedule =
                EmiCalculator.generateSchedule(PRINCIPAL, RATE, 12, FIRST_DUE);

        BigDecimal last = schedule.get(11).emiAmount();

        assertThat(last.subtract(emi).abs()).isLessThan(BigDecimal.ONE);
    }

    @Test
    void dueDatesAdvanceMonthlyFromFirstDueDate() {
        List<InstallmentPlan> schedule =
                EmiCalculator.generateSchedule(PRINCIPAL, RATE, 12, FIRST_DUE);

        assertThat(schedule.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(schedule.get(1).dueDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(schedule.get(11).dueDate()).isEqualTo(LocalDate.of(2027, 11, 5));
    }

    @Test
    void endOfMonthDueDatesDoNotDrift() {
        List<InstallmentPlan> schedule =
                EmiCalculator.generateSchedule(PRINCIPAL, RATE, 4, LocalDate.of(2027, 1, 31));

        assertThat(schedule.get(1).dueDate()).isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(schedule.get(2).dueDate()).isEqualTo(LocalDate.of(2027, 3, 31));
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> EmiCalculator.calculateEmi(BigDecimal.ZERO, RATE, 12))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EmiCalculator.calculateEmi(PRINCIPAL, BigDecimal.ZERO, 12))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EmiCalculator.calculateEmi(PRINCIPAL, RATE, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}