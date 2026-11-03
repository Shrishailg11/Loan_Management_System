package com.shri.loan_management_system.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {

    List<LoanTransaction> findByLoanIdOrderByCreatedAtDesc(Long loanId);

    boolean existsByReferenceNo(String referenceNo);

    Optional<LoanTransaction> findByReferenceNo(String referenceNo);

    @Query("select t from LoanTransaction t left join fetch t.schedule " +
            "where t.loan.id = :loanId order by t.createdAt desc, t.id desc")
    List<LoanTransaction> findHistoryByLoanId(@Param("loanId") Long loanId);
}