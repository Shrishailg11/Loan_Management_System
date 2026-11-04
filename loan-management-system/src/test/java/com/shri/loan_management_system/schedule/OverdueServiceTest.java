package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.loan.Loan;
import com.shri.loan_management_system.loan.LoanStatus;
import com.shri.loan_management_system.schedule.dto.OverdueInstallmentResponse;
import com.shri.loan_management_system.schedule.dto.OverdueSummary;
import com.shri.loan_management_system.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OverdueServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private final RepaymentScheduleRepository repository = mock(RepaymentScheduleRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-10T06:00:00Z"), ZONE);
    private final OverdueService service = new OverdueService(repository, clock);

    private RepaymentSchedule installment(long loanId, String loanNo, int no,
                                          LocalDate due, String emi) {
        User user = new User();
        user.setName("Asha Rao");
        user.setPhone("9876543210");

        Customer customer = new Customer();
        customer.setUser(user);

        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setLoanNo(loanNo);
        loan.setCustomer(customer);

        RepaymentSchedule s = new RepaymentSchedule();
        s.setLoan(loan);
        s.setInstallmentNo(no);
        s.setDueDate(due);
        s.setEmiAmount(new BigDecimal(emi));
        return s;
    }

    @Test
    void asksRepositoryForActiveLoansUsingTheClocksToday() {
        when(repository.findOverdue(any(), any())).thenReturn(List.of());

        service.listOverdue();

        verify(repository).findOverdue(eq(TODAY), eq(LoanStatus.ACTIVE));
    }

    @Test
    void calculatesDaysOverdueFromDueDate() {
        when(repository.findOverdue(TODAY, LoanStatus.ACTIVE)).thenReturn(
                List.of(installment(1L, "LN1", 3, LocalDate.of(2026, 11, 5), "9400.00")));

        List<OverdueInstallmentResponse> result = service.listOverdue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).daysOverdue()).isEqualTo(5);
        assertThat(result.get(0).customerName()).isEqualTo("Asha Rao");
        assertThat(result.get(0).loanNo()).isEqualTo("LN1");
    }

    @Test
    void summaryCountsInstallmentsDistinctLoansAndTotal() {
        when(repository.findOverdue(TODAY, LoanStatus.ACTIVE)).thenReturn(List.of(
                installment(1L, "LN1", 3, LocalDate.of(2026, 10, 5), "9400.00"),
                installment(1L, "LN1", 4, LocalDate.of(2026, 11, 5), "9400.00"),
                installment(2L, "LN2", 1, LocalDate.of(2026, 11, 9), "5000.50")));

        OverdueSummary summary = service.summarize();

        assertThat(summary.overdueInstallments()).isEqualTo(3);
        assertThat(summary.affectedLoans()).isEqualTo(2);
        assertThat(summary.totalOverdueAmount()).isEqualByComparingTo("23800.50");
    }

    @Test
    void summaryIsZeroWhenNothingIsOverdue() {
        when(repository.findOverdue(TODAY, LoanStatus.ACTIVE)).thenReturn(List.of());

        OverdueSummary summary = service.summarize();

        assertThat(summary.overdueInstallments()).isZero();
        assertThat(summary.affectedLoans()).isZero();
        assertThat(summary.totalOverdueAmount()).isEqualByComparingTo("0");
    }
}