package com.shri.loan_management_system.notification;

import com.shri.loan_management_system.loan.LoanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LoanNotificationListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanDecision(LoanDecisionEvent event) {
        emailService.send(event.customerEmail(), subjectFor(event), bodyFor(event));
    }

    String subjectFor(LoanDecisionEvent e) {
        return e.decision() == LoanStatus.APPROVED
                ? "Your loan application " + e.loanNo() + " has been approved"
                : "Update on your loan application " + e.loanNo();
    }

    String bodyFor(LoanDecisionEvent e) {
        if (e.decision() == LoanStatus.APPROVED) {
            return """
                    Dear %s,

                    Your loan application %s for %s has been approved.

                    Loan amount : %s INR
                    Tenure      : %d months
                    Interest    : %s%% per annum
                    Monthly EMI : %s INR

                    Your loan will be disbursed shortly. Once the funds are released,
                    you can view your repayment schedule in your account.

                    Regards,
                    Loan Management System
                    """.formatted(e.customerName(), e.loanNo(), e.productName(),
                    e.amount().toPlainString(), e.tenureMonths(),
                    e.interestRate().toPlainString(), e.emiAmount().toPlainString());
        }
        return """
                Dear %s,

                We are sorry to inform you that your loan application %s for %s
                (%s INR) could not be approved.

                Reason: %s

                You are welcome to apply again once the above has been addressed.

                Regards,
                Loan Management System
                """.formatted(e.customerName(), e.loanNo(), e.productName(),
                e.amount().toPlainString(), e.rejectionReason());
    }
}