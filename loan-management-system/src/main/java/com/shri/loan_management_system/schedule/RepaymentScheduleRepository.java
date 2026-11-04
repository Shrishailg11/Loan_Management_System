package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.loan.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select s from RepaymentSchedule s " +
            "join fetch s.loan l join fetch l.customer c join fetch c.user " +
            "where s.paidAt is null and s.dueDate < :today and l.status = :status " +
            "order by s.dueDate asc, s.id asc")
    List<RepaymentSchedule> findOverdue(@Param("today") LocalDate today,
                                        @Param("status") LoanStatus status);
}