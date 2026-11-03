package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.loan.dto.LoanApplyRequest;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.user.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponse> apply(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody LoanApplyRequest request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.apply(userId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<LoanResponse> myLoans(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return loanService.myLoans(userId);
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        return loanService.getLoan(id, userId, role);
    }
}