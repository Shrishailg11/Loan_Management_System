package com.shri.loan_management_system.common.exception;

/** A valid request that breaks a business rule, e.g. approving an already rejected loan. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}