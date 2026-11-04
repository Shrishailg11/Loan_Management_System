package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.loan.LoanStatus;
import com.shri.loan_management_system.schedule.dto.OverdueInstallmentResponse;
import com.shri.loan_management_system.schedule.dto.OverdueSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverdueService {

    private final RepaymentScheduleRepository scheduleRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<OverdueInstallmentResponse> listOverdue() {
        LocalDate today = LocalDate.now(clock);
        return scheduleRepository.findOverdue(today, LoanStatus.ACTIVE).stream()
                .map(s -> OverdueInstallmentResponse.from(s, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public OverdueSummary summarize() {
        List<RepaymentSchedule> overdue =
                scheduleRepository.findOverdue(LocalDate.now(clock), LoanStatus.ACTIVE);

        long affectedLoans = overdue.stream().map(s -> s.getLoan().getId()).distinct().count();
        BigDecimal total = overdue.stream()
                .map(RepaymentSchedule::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OverdueSummary(overdue.size(), affectedLoans, total);
    }
}