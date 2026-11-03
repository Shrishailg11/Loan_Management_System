package com.shri.loan_management_system.transaction;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.loan.Loan;
import com.shri.loan_management_system.loan.LoanRepository;
import com.shri.loan_management_system.loan.LoanService;
import com.shri.loan_management_system.loan.LoanStatus;
import com.shri.loan_management_system.schedule.RepaymentSchedule;
import com.shri.loan_management_system.schedule.RepaymentScheduleRepository;
import com.shri.loan_management_system.transaction.dto.PaymentResponse;
import com.shri.loan_management_system.transaction.dto.TransactionResponse;
import com.shri.loan_management_system.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanPaymentService {

    private final LoanRepository loanRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanTransactionRepository transactionRepository;
    private final LoanService loanService;

    @Transactional
    public PaymentResponse pay(Long loanId, Long userId, String clientReference) {
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));
        loanService.checkAccess(loan, userId, Role.CUSTOMER);

        String reference = "PAY-" + loanId + "-" + clientReference;

        // idempotency: a repeated request returns the original result and charges nothing
        Optional<LoanTransaction> existing = transactionRepository.findByReferenceNo(reference);
        if (existing.isPresent()) {
            return replay(loan, existing.get(), clientReference);
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Payments are only accepted for ACTIVE loans; this loan is " + loan.getStatus());
        }

        RepaymentSchedule installment = scheduleRepository
                .findFirstByLoanIdAndPaidAtIsNullOrderByInstallmentNoAsc(loanId)
                .orElseThrow(() -> new BusinessRuleException("No unpaid installments remain"));
        long unpaidBefore = scheduleRepository.countByLoanIdAndPaidAtIsNull(loanId);

        LocalDateTime now = LocalDateTime.now();
        installment.setPaidAt(now);

        LoanTransaction payment = new LoanTransaction();
        payment.setLoan(loan);
        payment.setSchedule(installment);
        payment.setType(TransactionType.PAYMENT);
        payment.setAmount(installment.getEmiAmount());
        payment.setReferenceNo(reference);
        transactionRepository.save(payment);

        if (unpaidBefore == 1) {
            loan.setStatus(LoanStatus.COMPLETED);   // that was the last installment
        }

        return new PaymentResponse(clientReference, installment.getInstallmentNo(),
                installment.getEmiAmount(), now, loan.getStatus(), unpaidBefore - 1, false);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(Long loanId, Long userId, Role role) {
        Loan loan = loanService.findAccessibleLoan(loanId, userId, role);
        return transactionRepository.findHistoryByLoanId(loan.getId())
                .stream().map(TransactionResponse::from).toList();
    }

    private PaymentResponse replay(Loan loan, LoanTransaction tx, String clientReference) {
        long remaining = scheduleRepository.countByLoanIdAndPaidAtIsNull(loan.getId());
        return new PaymentResponse(clientReference, tx.getSchedule().getInstallmentNo(),
                tx.getAmount(), tx.getCreatedAt(), loan.getStatus(), remaining, true);
    }
}