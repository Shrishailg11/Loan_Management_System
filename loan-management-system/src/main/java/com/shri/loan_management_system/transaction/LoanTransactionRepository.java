package com.shri.loan_management_system.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {

    List<LoanTransaction> findByLoanIdOrderByCreatedAtDesc(Long loanId);

    boolean existsByReferenceNo(String referenceNo);
}