package com.shri.loan_management_system.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    List<LoanProduct> findByActiveTrue();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}