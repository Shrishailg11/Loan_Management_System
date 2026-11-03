package com.shri.loan_management_system.transaction;

public enum TransactionType {
    DISBURSEMENT,   // bank -> customer, once per loan
    PAYMENT         // customer -> bank, once per EMI paid
}