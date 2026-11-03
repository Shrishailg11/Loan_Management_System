package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.officer.LoanOfficer;
import com.shri.loan_management_system.officer.LoanOfficerRepository;
import com.shri.loan_management_system.schedule.EmiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApprovalService {

    private final LoanRepository loanRepository;
    private final LoanOfficerRepository loanOfficerRepository;

    @Transactional(readOnly = true)
    public List<LoanResponse> listPending() {
        return loanRepository.findByStatusWithProduct(LoanStatus.PENDING)
                .stream().map(LoanResponse::from).toList();
    }

    @Transactional
    public LoanResponse approve(Long loanId, Long officerUserId) {
        LoanOfficer officer = findOfficer(officerUserId);
        Loan loan = findForDecision(loanId);
        requireTransition(loan, LoanStatus.APPROVED);

        if (loan.getPrincipal().compareTo(officer.getApprovalLimit()) > 0) {
            throw new BusinessRuleException("Loan amount " + loan.getPrincipal()
                    + " exceeds your approval limit of " + officer.getApprovalLimit());
        }

        // snapshot: the agreed rate is fixed now, whatever the product rate becomes later
        BigDecimal rate = loan.getProduct().getInterestRate();
        loan.setInterestRate(rate);
        loan.setEmiAmount(EmiCalculator.calculateEmi(loan.getPrincipal(), rate, loan.getTenureMonths()));

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(officer);
        loan.setDecidedAt(LocalDateTime.now());

        return LoanResponse.from(loan);
    }

    @Transactional
    public LoanResponse reject(Long loanId, Long officerUserId, String reason) {
        LoanOfficer officer = findOfficer(officerUserId);
        Loan loan = findForDecision(loanId);
        requireTransition(loan, LoanStatus.REJECTED);

        loan.setStatus(LoanStatus.REJECTED);
        loan.setApprovedBy(officer);          // records who made the decision
        loan.setRejectionReason(reason.trim());
        loan.setDecidedAt(LocalDateTime.now());

        return LoanResponse.from(loan);
    }

    private LoanOfficer findOfficer(Long userId) {
        return loanOfficerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan officer profile not found for this user"));
    }

    private Loan findForDecision(Long loanId) {
        return loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));
    }

    private void requireTransition(Loan loan, LoanStatus target) {
        if (!loan.getStatus().canTransitionTo(target)) {
            throw new BusinessRuleException("Loan is " + loan.getStatus() + " and cannot be moved to " + target);
        }
    }
}