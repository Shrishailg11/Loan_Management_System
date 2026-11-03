package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.loan.Loan;
import com.shri.loan_management_system.loan.LoanService;
import com.shri.loan_management_system.schedule.dto.InstallmentResponse;
import com.shri.loan_management_system.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final LoanService loanService;
    private final RepaymentScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<InstallmentResponse> getSchedule(Long loanId, Long userId, Role role) {
        Loan loan = loanService.findAccessibleLoan(loanId, userId, role);
        return scheduleRepository.findByLoanIdOrderByInstallmentNo(loan.getId())
                .stream().map(InstallmentResponse::from).toList();
    }
}