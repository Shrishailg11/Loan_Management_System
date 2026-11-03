package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.officer.LoanOfficer;
import com.shri.loan_management_system.officer.LoanOfficerRepository;
import com.shri.loan_management_system.product.LoanProduct;
import com.shri.loan_management_system.schedule.EmiCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApprovalServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock LoanOfficerRepository loanOfficerRepository;
    @InjectMocks LoanApprovalService service;

    private LoanOfficer officer;
    private Loan loan;

    @BeforeEach
    void setUp() {
        officer = new LoanOfficer();
        officer.setId(3L);
        officer.setApprovalLimit(new BigDecimal("500000"));

        LoanProduct product = new LoanProduct();
        product.setName("Personal Loan");
        product.setInterestRate(new BigDecimal("12.00"));

        loan = new Loan();
        loan.setId(7L);
        loan.setProduct(product);
        loan.setPrincipal(new BigDecimal("200000"));
        loan.setTenureMonths(24);          // status defaults to PENDING
    }

    private void givenOfficerAndLoan() {
        when(loanOfficerRepository.findByUserId(30L)).thenReturn(Optional.of(officer));
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(loan));
    }

    @Test
    void approvalSnapshotsRateAndEmiAndRecordsDecision() {
        givenOfficerAndLoan();

        LoanResponse response = service.approve(7L, 30L);

        BigDecimal expectedEmi = EmiCalculator.calculateEmi(
                new BigDecimal("200000"), new BigDecimal("12.00"), 24);
        assertThat(response.status()).isEqualTo(LoanStatus.APPROVED);
        assertThat(response.interestRate()).isEqualByComparingTo("12.00");
        assertThat(response.emiAmount()).isEqualByComparingTo(expectedEmi);
        assertThat(loan.getApprovedBy()).isSameAs(officer);
        assertThat(loan.getDecidedAt()).isNotNull();
    }

    @Test
    void rejectsApprovalAboveOfficerLimit() {
        loan.setPrincipal(new BigDecimal("600000"));
        givenOfficerAndLoan();

        assertThatThrownBy(() -> service.approve(7L, 30L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("approval limit");
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void cannotApproveAnAlreadyRejectedLoan() {
        loan.setStatus(LoanStatus.REJECTED);
        givenOfficerAndLoan();

        assertThatThrownBy(() -> service.approve(7L, 30L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectStoresReasonAndDecider() {
        givenOfficerAndLoan();

        LoanResponse response = service.reject(7L, 30L, "  Income documents missing ");

        assertThat(response.status()).isEqualTo(LoanStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Income documents missing");
        assertThat(loan.getApprovedBy()).isSameAs(officer);
    }

    @Test
    void cannotRejectAnAlreadyApprovedLoan() {
        loan.setStatus(LoanStatus.APPROVED);
        givenOfficerAndLoan();

        assertThatThrownBy(() -> service.reject(7L, 30L, "Changed my mind"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void failsWhenOfficerProfileMissing() {
        when(loanOfficerRepository.findByUserId(30L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(7L, 30L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void failsWhenLoanNotFound() {
        when(loanOfficerRepository.findByUserId(30L)).thenReturn(Optional.of(officer));
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(7L, 30L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}