package com.shri.loan_management_system.auth.dto;

import com.shri.loan_management_system.user.Role;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Role role
) {}