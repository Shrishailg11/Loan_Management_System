package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.loan.Loan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "repayment_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"loan_id", "installment_no"}))
@Getter
@Setter
@NoArgsConstructor
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "installment_no", nullable = false)
    private int installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_component", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalComponent;

    @Column(name = "interest_component", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestComponent;

    @Column(name = "emi_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    // null = unpaid, set = paid
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public boolean isPaid() {
        return paidAt != null;
    }
}