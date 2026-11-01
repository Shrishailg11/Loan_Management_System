package com.shri.loan_management_system.officer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanOfficerRepository extends JpaRepository<LoanOfficer, Long> {

    Optional<LoanOfficer> findByUserId(Long userId);
}