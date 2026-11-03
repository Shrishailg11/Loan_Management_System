package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.product.LoanProduct;
import com.shri.loan_management_system.schedule.RepaymentSchedule;
import com.shri.loan_management_system.schedule.RepaymentScheduleRepository;
import com.shri.loan_management_system.transaction.LoanTransaction;
import com.shri.loan_management_system.transaction.LoanTransactionRepository;
import com.shri.loan_management_system.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanDisbursementServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanTransactionRepository transactionRepository;
    @InjectMocks LoanDisbursementService service;

    @Captor ArgumentCaptor<List<RepaymentSchedule>> scheduleCaptor;
    @Captor ArgumentCaptor<LoanTransaction> transactionCaptor;

    private Loan loan;

    @BeforeEach
    void setUp() {
        LoanProduct product = new LoanProduct();
        product.setName("Personal Loan");

        loan = new Loan();
        loan.setId(7L);
        loan.setLoanNo("LN20261103000001");
        loan.setProduct(product);
        loan.setPrincipal(new BigDecimal("200000"));
        loan.setTenureMonths(12);
        loan.setInterestRate(new BigDecimal("12.00"));
        loan.setEmiAmount(new BigDecimal("17769.74"));
        loan.setStatus(LoanStatus.APPROVED);
    }

    @Test
    void disbursesApprovedLoanAndCreatesScheduleAndTransaction() {
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(loan));

        LoanResponse response = service.disburse(7L);

        assertThat(response.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getDisbursedAt()).isNotNull();

        verify(scheduleRepository).saveAll(scheduleCaptor.capture());
        List<RepaymentSchedule> rows = scheduleCaptor.getValue();
        assertThat(rows).hasSize(12);
        BigDecimal principalTotal = rows.stream()
                .map(RepaymentSchedule::getPrincipalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(principalTotal).isEqualByComparingTo("200000");
        assertThat(rows).allMatch(row -> row.getLoan() == loan);

        verify(transactionRepository).save(transactionCaptor.capture());
        LoanTransaction tx = transactionCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.DISBURSEMENT);
        assertThat(tx.getAmount()).isEqualByComparingTo("200000");
        assertThat(tx.getReferenceNo()).isEqualTo("DISB-LN20261103000001");
        assertThat(tx.getSchedule()).isNull();
    }

    @Test
    void cannotDisbursePendingLoan() {
        loan.setStatus(LoanStatus.PENDING);
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.disburse(7L))
                .isInstanceOf(BusinessRuleException.class);
        verify(scheduleRepository, never()).saveAll(anyList());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cannotDisburseTwice() {
        loan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.disburse(7L))
                .isInstanceOf(BusinessRuleException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void failsWhenLoanNotFound() {
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.disburse(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}