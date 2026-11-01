package com.shri.loan_management_system.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import main.java.com.shri.loan_management_system.loan.LoanStatus;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatus(LoanStatus status);
}