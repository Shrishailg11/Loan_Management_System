package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.schedule.EmiCalculator;
import com.shri.loan_management_system.schedule.InstallmentPlan;
import com.shri.loan_management_system.schedule.RepaymentSchedule;
import com.shri.loan_management_system.schedule.RepaymentScheduleRepository;
import com.shri.loan_management_system.transaction.LoanTransaction;
import com.shri.loan_management_system.transaction.LoanTransactionRepository;
import com.shri.loan_management_system.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanDisbursementService {

    private final LoanRepository loanRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanTransactionRepository transactionRepository;

    @Transactional
    public LoanResponse disburse(Long loanId) {
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        if (!loan.getStatus().canTransitionTo(LoanStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "Loan is " + loan.getStatus() + "; only APPROVED loans can be disbursed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate firstDueDate = now.toLocalDate().plusMonths(1);

        List<InstallmentPlan> plans = EmiCalculator.generateSchedule(
                loan.getPrincipal(), loan.getInterestRate(), loan.getTenureMonths(), firstDueDate);
        scheduleRepository.saveAll(plans.stream().map(plan -> toRow(loan, plan)).toList());

        LoanTransaction disbursement = new LoanTransaction();
        disbursement.setLoan(loan);
        disbursement.setType(TransactionType.DISBURSEMENT);
        disbursement.setAmount(loan.getPrincipal());
        disbursement.setReferenceNo("DISB-" + loan.getLoanNo());
        transactionRepository.save(disbursement);

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(now);

        return LoanResponse.from(loan);
    }

    private RepaymentSchedule toRow(Loan loan, InstallmentPlan plan) {
        RepaymentSchedule row = new RepaymentSchedule();
        row.setLoan(loan);
        row.setInstallmentNo(plan.installmentNo());
        row.setDueDate(plan.dueDate());
        row.setPrincipalComponent(plan.principalComponent());
        row.setInterestComponent(plan.interestComponent());
        row.setEmiAmount(plan.emiAmount());
        return row;
    }
}