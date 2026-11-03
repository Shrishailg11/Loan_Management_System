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
import com.shri.loan_management_system.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanPaymentServiceTest {

    private static final String KEY = "ref-0001-abcd";
    private static final String STORED_REFERENCE = "PAY-7-" + KEY;

    @Mock LoanRepository loanRepository;
    @Mock RepaymentScheduleRepository scheduleRepository;
    @Mock LoanTransactionRepository transactionRepository;
    @Mock LoanService loanService;
    @InjectMocks LoanPaymentService service;

    @Captor ArgumentCaptor<LoanTransaction> transactionCaptor;

    private Loan loan;
    private RepaymentSchedule installment;

    @BeforeEach
    void setUp() {
        loan = new Loan();
        loan.setId(7L);
        loan.setLoanNo("LN20261103000001");
        loan.setStatus(LoanStatus.ACTIVE);

        installment = new RepaymentSchedule();
        installment.setInstallmentNo(1);
        installment.setEmiAmount(new BigDecimal("17769.74"));
    }

    private void givenLockedLoan() {
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(loan));
    }

    private void givenNextInstallmentWithUnpaidCount(long unpaidCount) {
        when(transactionRepository.findByReferenceNo(STORED_REFERENCE)).thenReturn(Optional.empty());
        when(scheduleRepository.findFirstByLoanIdAndPaidAtIsNullOrderByInstallmentNoAsc(7L))
                .thenReturn(Optional.of(installment));
        when(scheduleRepository.countByLoanIdAndPaidAtIsNull(7L)).thenReturn(unpaidCount);
    }

    @Test
    void paysNextInstallmentAndRecordsPayment() {
        givenLockedLoan();
        givenNextInstallmentWithUnpaidCount(3);

        PaymentResponse response = service.pay(7L, 1L, KEY);

        verify(loanService).checkAccess(loan, 1L, Role.CUSTOMER);
        verify(transactionRepository).save(transactionCaptor.capture());
        LoanTransaction tx = transactionCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(tx.getAmount()).isEqualByComparingTo("17769.74");
        assertThat(tx.getReferenceNo()).isEqualTo(STORED_REFERENCE);
        assertThat(tx.getSchedule()).isSameAs(installment);

        assertThat(installment.isPaid()).isTrue();
        assertThat(response.replayed()).isFalse();
        assertThat(response.remainingInstallments()).isEqualTo(2);
        assertThat(response.loanStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void finalPaymentCompletesTheLoan() {
        givenLockedLoan();
        givenNextInstallmentWithUnpaidCount(1);

        PaymentResponse response = service.pay(7L, 1L, KEY);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.COMPLETED);
        assertThat(response.loanStatus()).isEqualTo(LoanStatus.COMPLETED);
        assertThat(response.remainingInstallments()).isZero();
    }

    private LoanTransaction earlierPayment() {
        LoanTransaction tx = new LoanTransaction();
        tx.setLoan(loan);
        tx.setSchedule(installment);
        tx.setType(TransactionType.PAYMENT);
        tx.setAmount(new BigDecimal("17769.74"));
        tx.setReferenceNo(STORED_REFERENCE);
        tx.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        return tx;
    }

    @Test
    void repeatedReferenceReturnsOriginalResultWithoutChargingAgain() {
        givenLockedLoan();
        when(transactionRepository.findByReferenceNo(STORED_REFERENCE))
                .thenReturn(Optional.of(earlierPayment()));
        when(scheduleRepository.countByLoanIdAndPaidAtIsNull(7L)).thenReturn(5L);

        PaymentResponse response = service.pay(7L, 1L, KEY);

        assertThat(response.replayed()).isTrue();
        assertThat(response.installmentNo()).isEqualTo(1);
        assertThat(installment.isPaid()).isFalse();
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void retryOfFinalPaymentStillSucceedsAfterLoanCompleted() {
        loan.setStatus(LoanStatus.COMPLETED);
        givenLockedLoan();
        when(transactionRepository.findByReferenceNo(STORED_REFERENCE))
                .thenReturn(Optional.of(earlierPayment()));
        when(scheduleRepository.countByLoanIdAndPaidAtIsNull(7L)).thenReturn(0L);

        PaymentResponse response = service.pay(7L, 1L, KEY);

        assertThat(response.replayed()).isTrue();
        assertThat(response.loanStatus()).isEqualTo(LoanStatus.COMPLETED);
    }

    @Test
    void rejectsPaymentOnLoanThatIsNotActive() {
        loan.setStatus(LoanStatus.APPROVED);
        givenLockedLoan();
        when(transactionRepository.findByReferenceNo(STORED_REFERENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pay(7L, 1L, KEY))
                .isInstanceOf(BusinessRuleException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsPaymentWhenNoUnpaidInstallmentRemains() {
        givenLockedLoan();
        when(transactionRepository.findByReferenceNo(STORED_REFERENCE)).thenReturn(Optional.empty());
        when(scheduleRepository.findFirstByLoanIdAndPaidAtIsNullOrderByInstallmentNoAsc(7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pay(7L, 1L, KEY))
                .isInstanceOf(BusinessRuleException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void failsWhenLoanNotFound() {
        when(loanRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pay(7L, 1L, KEY))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void otherCustomersLoanIsRejectedBeforeAnythingElseHappens() {
        givenLockedLoan();
        doThrow(new ResourceNotFoundException("Loan", 7L))
                .when(loanService).checkAccess(loan, 2L, Role.CUSTOMER);

        assertThatThrownBy(() -> service.pay(7L, 2L, KEY))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(transactionRepository);
    }
}