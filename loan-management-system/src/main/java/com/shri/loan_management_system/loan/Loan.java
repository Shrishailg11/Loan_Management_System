package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.officer.LoanOfficer;
import com.shri.loan_management_system.product.LoanProduct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.shri.loan_management_system.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_no", nullable = false, unique = true, length = 30)
    private String loanNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private LoanOfficer approvedBy;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    // frozen at approval, null while PENDING
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    // calculated at approval, null while PENDING
    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status = LoanStatus.PENDING;

    @CreationTimestamp
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;
}