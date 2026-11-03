package com.shri.loan_management_system.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    List<RepaymentSchedule> findByLoanIdOrderByInstallmentNo(Long loanId);

    // the next EMI the customer has to pay
    Optional<RepaymentSchedule> findFirstByLoanIdAndPaidAtIsNullOrderByInstallmentNoAsc(Long loanId);

    // overdue = due before today and still unpaid
    List<RepaymentSchedule> findByDueDateBeforeAndPaidAtIsNull(LocalDate date);

    long countByLoanIdAndPaidAtIsNull(Long loanId);
}