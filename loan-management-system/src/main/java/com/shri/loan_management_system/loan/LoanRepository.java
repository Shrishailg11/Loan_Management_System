package com.shri.loan_management_system.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shri.loan_management_system.loan.LoanStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatus(LoanStatus status);

    boolean existsByLoanNo(String loanNo);

    @Query("select l from Loan l join fetch l.product " +
            "where l.customer.id = :customerId order by l.appliedAt desc")
    List<Loan> findByCustomerIdWithProduct(@Param("customerId") Long customerId);
}