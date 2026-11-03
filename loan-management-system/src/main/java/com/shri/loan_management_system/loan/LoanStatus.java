package com.shri.loan_management_system.loan;

public enum LoanStatus {
    PENDING,
    APPROVED,
    ACTIVE,
    REJECTED,
    COMPLETED;

    public boolean canTransitionTo(LoanStatus next) {
        return switch (this) {
            case PENDING  -> next == APPROVED || next == REJECTED;
            case APPROVED -> next == ACTIVE;
            case ACTIVE   -> next == COMPLETED;
            case REJECTED, COMPLETED -> false;   // terminal states
        };
    }
}