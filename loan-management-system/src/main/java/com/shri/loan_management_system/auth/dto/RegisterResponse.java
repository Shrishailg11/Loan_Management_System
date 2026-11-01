package com.shri.loan_management_system.auth.dto;

import com.shri.loan_management_system.user.Role;

public record RegisterResponse(
        Long userId,
        Long customerId,
        String name,
        String email,
        Role role
) {}